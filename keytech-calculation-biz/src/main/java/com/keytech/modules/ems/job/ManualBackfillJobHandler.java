package com.keytech.modules.ems.job;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.common.collect.Lists;
import com.keytech.modules.ems.constants.*;
import com.keytech.modules.ems.dto.CollectBatchDto;
import com.keytech.modules.ems.entity.CollectPointStatus;
import com.keytech.modules.ems.entity.CollectTag;
import com.keytech.modules.ems.service.ICollectPointStatusService;
import com.keytech.modules.ems.service.impl.EmsCalculateService;
import com.keytech.modules.ems.util.CalcuDebugHelper;
import com.keytech.modules.ems.util.SplitUtils;
import com.keytech.modules.ems.vo.CalcuDebugResult;
import com.keytech.modules.ems.vo.CollectResultVo;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 手动补采任务
 * @author yepeng
 * @since 2025.05.06
 */
@Slf4j
@Component
public class ManualBackfillJobHandler {
    private final ICollectPointStatusService collectPointStatusService;
    private final EmsCalculateService calculateService;
    private final ExecutorService executorService;
    private final AggregationStatusGenerator aggregationStatusGenerator;
    private final RedisTemplate<String, String> redisTemplate;
    //批次点位数量从Redis中获取
    private int batchTagSize = 10;

    //构造注入
    public ManualBackfillJobHandler(
            ICollectPointStatusService collectPointStatusService,
            EmsCalculateService calculateService,
            @Qualifier("backfillExecutor") ExecutorService executorService,
            AggregationStatusGenerator aggregationStatusGenerator,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.collectPointStatusService = collectPointStatusService;
        this.calculateService = calculateService;
        this.executorService = executorService;
        this.aggregationStatusGenerator = aggregationStatusGenerator;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 手动或自动补采任务（XxlJob中设置：每 2 分钟执行）
     * 手动补采是指用户手动设置指定时间区间及点位状态为未完成
     * 自动补采是指周期采集任务没有正常执行导致漏采的点位
     * 0 0/2 * * * ?
     */
    @XxlJob("manualBackfillCollectJob")
    public ReturnT<String> manualBackfillCollectJob(String params) {
        //判断是否开启调试模式，如果开启则找到调试的点位集合
        CalcuDebugResult debugResult = CalcuDebugHelper.getCalcuDebugResult(redisTemplate);
        if(debugResult.isDebug()) {
            log.warn("开启调试模式，只处理调试点位: {}", debugResult.getCTagIds());
            if (debugResult.getCTagIds().isEmpty()) {
                log.warn("无调试点位，跳过手动或自动补采任务");
                return new ReturnT<>(ReturnT.FAIL_CODE, "无调试点位，跳过手动或自动补采任务");
            }
        }

        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        log.warn("[补采任务] 启动手动补采任务...本轮子任务批次请求INFLUX数：{}", batchTagSize);
        try {
            //点位太多，分批处理，每批10个点位
            int batchSize = 10;
            int[] grainTypes = new int[] {GrainTypeEnum.HOUR.getVal(), GrainTypeEnum.HALFHOUR.getVal()};
            for (int grainType : grainTypes) {
                List<CollectTag> emsPointList = collectPointStatusService.getCollectPoints(grainType);
                if (CollectionUtils.isEmpty(emsPointList)) {
                    log.warn("无可用{}点位配置，跳过参考值初始化", GrainTypeEnum.getNameByVal(grainType));
                    continue;
                }
                List<List<CollectTag>> batchedPoints = Lists.partition(emsPointList, batchSize);
                for (List<CollectTag> batchPoints : batchedPoints) {
                    List<String> tagIds = batchPoints.stream().map(CollectTag::getTagId).toList();
                    log.info("[补采任务] 本次任务处理点位数量: {}", tagIds);
                    // 1. 查询待补采的点位
                    List<CollectPointStatus> statusList = queryPendingPoints(tagIds);
                    //调试模式开启，只处理调试点位
                    if(debugResult.isDebug()) {
                        statusList = statusList.stream().filter(s -> debugResult.getCTagIds().contains(s.getTagId())).toList();
                    }

                    // flux码为空的直接跳过并写错误日志
                    List<CollectPointStatus> emptyFluxList = statusList.stream().filter(p -> StringUtils.isBlank(p.getFluxCode())).sorted(Comparator.comparing(CollectPointStatus::getCollectTime)).toList();
                    if (!emptyFluxList.isEmpty()) {
                        //修改状态
                        UpdateWrapper<CollectPointStatus> failUpdateWrap = new UpdateWrapper<>();
                        failUpdateWrap.set("error_flag", CollectErrorFlagEnum.UNUSUAL.getVal()).set("update_time", LocalDateTime.now())
                                .in("id", emptyFluxList.stream().map(CollectPointStatus::getId).toList());
                        collectPointStatusService.update(failUpdateWrap);
                        //写错误日志(这类错误只需要记录首尾两条)
                        List<CollectResultVo> emptyFluxFailList = emptyFluxList.stream()
                                .collect(Collectors.groupingBy(CollectPointStatus::getTagId))
                                .values().stream()
                                .flatMap(groupedList ->
                                        // 为每组生成失败记录：首条和最后一条（如果有多条）
                                        IntStream.range(0, Math.min(2, groupedList.size())) // 最多处理2条
                                                .mapToObj(i -> i == 0 ? groupedList.get(0) : groupedList.get(groupedList.size() - 1))
                                                .map(item -> CollectResultVo.fail(item, "未配置Influx查询编码", CollectErrorTypeEnum.NOT_SET_FLUX_CODE.getVal()))
                                ).toList();
                        calculateService.batchInsertCollectLog(emptyFluxFailList);
                    }

                    statusList = statusList.stream().filter(p -> StringUtils.isNotBlank(p.getFluxCode())).toList();
                    if (statusList.isEmpty()) {
                        log.info("没有要补采的采集点位，任务结束");
                        continue;
                        //return new ReturnT<>(ReturnT.FAIL_CODE, "没有要补采的采集点位，任务结束");
                    }
                    int totalSize = statusList.size();
                    log.warn("[补采任务] 查询到要补采的采集点位数量: {}", totalSize);

                    // 2. 按采集粒度和时间分组，并拆分成批次
                    List<CollectBatchDto> batchList = groupAndSplitIntoBatches(statusList, batchTagSize);

                    // 3. 执行补采任务
                    List<CollectResultVo> successList = new ArrayList<>();
                    List<CollectResultVo> failList = new ArrayList<>();
                    executeBackfillTasks(batchList, successList, failList);
                    // 找出超时的点位再单个点位重新补采一次
                    if(successList.size() + failList.size() < totalSize) {
                        Set<Integer> statusIdSet = Stream.concat(successList.stream().map(s -> s.getPointStatus().getId()),
                                failList.stream().map(s -> s.getPointStatus().getId())).collect(Collectors.toSet());
                        if (batchTagSize == 1) {
                            //超时返回的，直接标记为失败
                            List<CollectResultVo> timeOutfailList = statusList.stream().filter(s -> !statusIdSet.contains(s.getId()))
                                    .map(s -> CollectResultVo.fail(s, "查询超时", CollectErrorTypeEnum.TIME_OUT_ERROR.getVal())).toList();
                            failList.addAll(timeOutfailList);
                        }else {
                            List<Integer> timeOutStatusIdList = statusList.stream().filter(s -> !statusIdSet.contains(s.getId()))
                                    .map(CollectPointStatus::getId).toList();
                            if(!timeOutStatusIdList.isEmpty()) {
                                log.warn("[补采任务] 本次任务有超时返回的点位，重新单独补采");
                                //重新补采
                                List<CollectPointStatus> timeOutStatusList = statusList.stream().filter(s -> timeOutStatusIdList.contains(s.getId())).toList();
                                List<CollectBatchDto> timeOutBatchList = groupAndSplitIntoBatches(timeOutStatusList, 1);
                                List<CollectResultVo> timeOutSuccessList = new ArrayList<>();
                                List<CollectResultVo> timeOutFailList = new ArrayList<>();
                                executeBackfillTasks(timeOutBatchList, timeOutSuccessList, timeOutFailList);
                                successList.addAll(timeOutSuccessList);
                                failList.addAll(timeOutFailList);
                                if (timeOutSuccessList.size() + timeOutFailList.size() < timeOutStatusList.size()) {
                                    Set<Integer> timeOUtstatusIdSet = Stream.concat(timeOutSuccessList.stream().map(s -> s.getPointStatus().getId()),
                                            timeOutFailList.stream().map(s -> s.getPointStatus().getId())).collect(Collectors.toSet());
                                    //超时返回的，直接标记为失败
                                    List<CollectResultVo> timeOutfailList = timeOutStatusList.stream().filter(s -> !timeOUtstatusIdSet.contains(s.getId()))
                                            .map(s -> CollectResultVo.fail(s, "查询超时", CollectErrorTypeEnum.TIME_OUT_ERROR.getVal())).toList();
                                    failList.addAll(timeOutfailList);
                                }
                            }
                        }
                    }
                    // 4. 处理补采结果
                    processBackfillResults(successList, failList);

                    // 5. 更新采集点位状态
                    updateCollectPointStatus(successList, failList);

                    log.warn("本批点位 补采完成，总数：{}, 成功：{}，失败：{}", totalSize, successList.size(), failList.size());

                    // 6. 生成聚合计算点位状态
                    generateAggregationStatus(successList);
                    //每批次执行完毕，休眠1s，防止CPU过载
                    Thread.sleep(1000);
                }
            }
            return ReturnT.SUCCESS;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[补采任务] 被中断", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "manualBackfillCollectJob 执行中断: " + e.getMessage());
        } catch (Exception e) {
            log.error("manualBackfillCollectJob 执行失败", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "manualBackfillCollectJob 执行失败: " + e.getMessage());
        } finally {
            log.info("[补采任务] 结束补采任务...");
        }
    }

    /**
     * 查询待补采的点位
     * @param tagIds 指定点位集合，为空时查询所有
     */
    private List<CollectPointStatus> queryPendingPoints(List<String> tagIds) {
        return collectPointStatusService.queryPendingForBackfill(null, null, LocalDateTime.now(), tagIds);
    }

    /**
     * 按采集粒度和时间分组，并拆分成批次
     * @param statusList 待补采的点位
     * @param batchTagSize 批次大小
     */
    private List<CollectBatchDto> groupAndSplitIntoBatches(List<CollectPointStatus> statusList, int batchTagSize) {
        // 按采集粒度拆分出半小时与小时点位
        Map<Integer, List<CollectPointStatus>> grainPointList = statusList.stream()
                .collect(Collectors.groupingBy(CollectPointStatus::getGrainType));

        // 按 collectTime 分组，同时按20个一组拆分
        List<CollectBatchDto> batchList = new ArrayList<>();
        for (Map.Entry<Integer, List<CollectPointStatus>> grainEntry : grainPointList.entrySet()) {
            Integer grainType = grainEntry.getKey();
            Map<LocalDateTime, List<CollectPointStatus>> groupedByTime = grainEntry.getValue().stream()
                    .collect(Collectors.groupingBy(CollectPointStatus::getCollectTime));

            for (Map.Entry<LocalDateTime, List<CollectPointStatus>> entry : groupedByTime.entrySet()) {
                // 其实是计算采集时段的起点
                LocalDateTime stime = entry.getKey();
                List<CollectPointStatus> pStatusList = entry.getValue();
                List<List<CollectPointStatus>> subBatches = SplitUtils.splitList(pStatusList, batchTagSize);

                for (List<CollectPointStatus> batch : subBatches) {
                    batchList.add(new CollectBatchDto(
                            stime,
                            grainType == GrainTypeEnum.HALFHOUR.getVal() ? stime.plusMinutes(30) : stime.plusHours(1),
                            batch
                    ));
                }
            }
        }
        return batchList;
    }

    /**
     * 并发执行补采任务
     */
    private void executeBackfillTasks(List<CollectBatchDto> batchList, List<CollectResultVo> successList, List<CollectResultVo> failList) throws InterruptedException {
        log.warn("[补采任务] 本轮执行补采数量：{}", batchList.size());
        // 构建任务
        CompletionService<List<CollectResultVo>> cs = new ExecutorCompletionService<>(executorService);
        int taskCount = 0;
        for (CollectBatchDto batch : batchList) {
            cs.submit(() -> calculateService.batchCollectPoints(batch.getPoints(), batch.getStartTime(), batch.getEndTime()));
            taskCount++;
        }
        log.warn("[补采任务] 已提交补采子任务数：{}", taskCount);
        // 单个子任务超时时间 5s
        long singleTaskTimeoutSec = 5;
        for (int i = 0; i < taskCount; i++) {
            try {
                // 阻塞直到有完成的任务
                Future<List<CollectResultVo>> future = cs.take();
                try {
                    // 设置超时获取结果
                    List<CollectResultVo> results = future.get(singleTaskTimeoutSec, TimeUnit.SECONDS);
                    // 统计成功/失败
                    for (CollectResultVo result : results) {
                        if (result.isSuccess()) {
                            successList.add(result);
                        } else {
                            failList.add(result);
                        }
                    }
                } catch (TimeoutException e) {
                    log.error("[补采任务] 子任务超时:", e);
                    // 尝试中断执行
                    future.cancel(true);
                }
            } catch (Exception e) {
                log.error("[补采任务] 子任务执行异常", e);
            }
        }
    }

    private void processBackfillResults(List<CollectResultVo> successList, List<CollectResultVo> failList) {
        // 落库处理(正常，异常跳小，异常跳大，手动清零，全量异常) 半小时点位与小时点位是混在一起的
        if (!successList.isEmpty()) {
            List<CollectResultVo> halfDataList = successList.stream()
                    .filter(s -> s.getPointStatus().getGrainType() == GrainTypeEnum.HALFHOUR.getVal())
                    .toList();
            List<CollectResultVo> hourDataList = successList.stream()
                    .filter(s -> s.getPointStatus().getGrainType() == GrainTypeEnum.HOUR.getVal())
                    .toList();

            if (!halfDataList.isEmpty()) {
                calculateService.batchInsertUsageDb(halfDataList, GrainTypeEnum.HALFHOUR.getVal());
            }
            if (!hourDataList.isEmpty()) {
                calculateService.batchInsertUsageDb(hourDataList, GrainTypeEnum.HOUR.getVal());
            }
        }

        // 所有非正常的记录全部入错误日志库
        List<CollectResultVo> errorList = new ArrayList<>();
        errorList.addAll(failList);
        errorList.addAll(successList.stream()
                .filter(s -> s.getErrorType() != CollectErrorTypeEnum.NORMAL.getVal())
                .toList());

        if (!errorList.isEmpty()) {
            calculateService.batchInsertCollectLog(errorList);
        }
    }

    private void updateCollectPointStatus(List<CollectResultVo> successList, List<CollectResultVo> failList) {
        List<CollectResultVo> allList = new ArrayList<>();
        allList.addAll(successList);
        allList.addAll(failList);
        if (!allList.isEmpty()) {
            calculateService.updateCollectPointStatus(allList, JobTypeEnum.MANUAL_BACK_JOB.getVal());
        }
    }

    private void generateAggregationStatus(List<CollectResultVo> successList) {
        List<CollectPointStatus> successPointList = successList.stream()
                .map(CollectResultVo::getPointStatus)
                .toList();

        if (!successPointList.isEmpty()) {
            this.aggregationStatusGenerator.generateAggregationStatusAfterRetryBackfill(successPointList);
        }
    }

}
