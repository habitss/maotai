package com.keytech.modules.ems.job;

import com.keytech.modules.ems.constants.CollectErrorTypeEnum;
import com.keytech.modules.ems.constants.EmsConstantsX;
import com.keytech.modules.ems.constants.GrainTypeEnum;
import com.keytech.modules.ems.constants.JobTypeEnum;
import com.keytech.modules.ems.dto.CollectBatchDto;
import com.keytech.modules.ems.entity.CollectPointStatus;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 失败重试任务处理器
 * 功能：处理采集失败的点位数据重试任务
 * @author yepeng
 * @since 2025.05.06
 */
@Slf4j
@Component
public class RetryJobHandler {
    private final ICollectPointStatusService collectPointStatusService;
    private final EmsCalculateService calculateService;
    private final ExecutorService executorService;
    private final AggregationStatusGenerator aggregationStatusGenerator;
    private final RedisTemplate<String, String> redisTemplate;

    //构造注入
    public RetryJobHandler(
            ICollectPointStatusService collectPointStatusService,
            EmsCalculateService calculateService,
            @Qualifier("commonExecutor") ExecutorService executorService,
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
     * 失败采集点位重试任务（XXL-JOB定时任务）
     * 执行频率：每10分钟执行一次（0 0/10 * * * ?）
     * @param params 任务参数
     * @return 执行结果
     */
    @XxlJob("retryFailedCollectJob")
    public ReturnT<String> retryFailedCollectJob(String params) {
        //判断是否开启调试模式，如果开启则找到调试的点位集合
        CalcuDebugResult debugResult = CalcuDebugHelper.getCalcuDebugResult(redisTemplate);
        if(debugResult.isDebug()) {
            log.warn("开启调试模式，只处理调试点位: {}", debugResult.getCTagIds());
            if (debugResult.getCTagIds().isEmpty()) {
                log.warn("无调试点位，跳过失败采集点位重试任务");
                return new ReturnT<>(ReturnT.FAIL_CODE, "无调试点位，跳过失败采集点位重试任务");
            }
        }

        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        log.info("[重试任务] 启动失败重试采集任务...");
        try {
            // 1. 查询待重试的点位
            List<CollectPointStatus> statusList = queryPointsToRetry();
            //调试模式开启，只处理调试点位
            if(debugResult.isDebug()) {
                statusList = statusList.stream().filter(s -> debugResult.getCTagIds().contains(s.getTagId())).toList();
            }

            if (statusList.isEmpty()) {
                log.info("没有可重试的采集点位，任务结束");
                return new ReturnT<>(ReturnT.FAIL_CODE, "没有可重试的采集点位，任务结束");
            }
            log.warn("[重试任务] 查询到可重试的采集点位数量: {}", statusList.size());

            // 2. 按采集粒度和时间分组，并拆分成批次
            List<CollectBatchDto> batchList = groupAndSplitIntoBatches(statusList);

            // 3. 执行重试任务
            List<CollectResultVo> successList = new ArrayList<>();
            List<CollectResultVo> failList = new ArrayList<>();
            executeRetryTasks(batchList, successList, failList);

            int successCount = successList.size();
            int failCount = failList.size();

            // 4. 处理重试结果
            processRetryResults(successList, failList);

            // 5. 更新采集点位状态
            updateCollectPointStatus(successList, failList);

            log.warn("重试采集完成，成功：{}，失败：{}", successCount, failCount);

            // 6. 生成聚合计算点位状态
            generateAggregationStatus(successList);

            return ReturnT.SUCCESS;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[重试任务] 子任务被中断", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "retryFailedCollectJob 执行中断: " + e.getMessage());
        } catch (Exception e) {
            log.error("retryFailedCollectJob 执行失败", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "retryFailedCollectJob 执行失败: " + e.getMessage());
        } finally {
            log.info("[重试任务] 结束失败重试采集任务...");
        }
    }

    /**
     * 查询待重试的点位
     * @return 待重试的点位列表
     */
    private List<CollectPointStatus> queryPointsToRetry() {
        return collectPointStatusService.queryPendingForRetry(3, null, 48);
    }

    /**
     * 将点位按采集粒度和时间分组，并拆分成批次
     * @param statusList 点位状态列表
     * @return 批次列表
     */
    private List<CollectBatchDto> groupAndSplitIntoBatches(List<CollectPointStatus> statusList) {
        // 按采集粒度（半小时/小时）分组
        Map<Integer, List<CollectPointStatus>> grainPointList = statusList.stream()
                .collect(Collectors.groupingBy(CollectPointStatus::getGrainType));

        // 按采集时间分组，并拆分成每批50个点位
        List<CollectBatchDto> batchList = new ArrayList<>();
        for (Map.Entry<Integer, List<CollectPointStatus>> grainEntry : grainPointList.entrySet()) {
            Integer grainType = grainEntry.getKey();
            Map<LocalDateTime, List<CollectPointStatus>> groupedByTime = grainEntry.getValue().stream()
                    .collect(Collectors.groupingBy(CollectPointStatus::getCollectTime));

            for (Map.Entry<LocalDateTime, List<CollectPointStatus>> entry : groupedByTime.entrySet()) {
                LocalDateTime collectTime = entry.getKey();
                List<CollectPointStatus> pStatusList = entry.getValue();
                List<List<CollectPointStatus>> subBatches = SplitUtils.splitList(pStatusList, EmsConstantsX.BATCH_TAG_NUM);
                // 创建批次对象
                for (List<CollectPointStatus> batch : subBatches) {
                    batchList.add(new CollectBatchDto(
                            grainType == GrainTypeEnum.HALFHOUR.getVal() ? collectTime.minusMinutes(30) : collectTime.minusHours(1),
                            collectTime,
                            batch
                    ));
                }
            }
        }
        return batchList;
    }

    /**
     * 执行重试任务
     * @param batchList 批次列表
     * @param successList 成功结果列表
     * @param failList 失败结果列表
     * @throws InterruptedException 中断异常
     */
    private void executeRetryTasks(List<CollectBatchDto> batchList, List<CollectResultVo> successList, List<CollectResultVo> failList) throws InterruptedException {
        // 构建任务
        List<Callable<List<CollectResultVo>>> tasks = new ArrayList<>();
        for (CollectBatchDto batch : batchList) {
            tasks.add(() -> calculateService.batchCollectPoints(batch.getPoints(), batch.getStartTime(), batch.getEndTime()));
        }

        // 执行任务
        List<Future<List<CollectResultVo>>> futures = executorService.invokeAll(tasks);

        // 汇总结果
        for (Future<List<CollectResultVo>> future : futures) {
            try {
                List<CollectResultVo> batchResult = future.get();
                for (CollectResultVo result : batchResult) {
                    if (result.isSuccess()) {
                        successList.add(result);
                    } else {
                        failList.add(result);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("重试采集子任务被中断", e);
            } catch (Exception e) {
                log.error("重试采集子任务执行异常: ", e);
            }
        }
    }

    /**
     * 处理重试结果
     * @param successList 成功结果列表
     * @param failList 失败结果列表
     */
    private void processRetryResults(List<CollectResultVo> successList, List<CollectResultVo> failList) {
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

    /**
     * 更新采集点位状态
     * 将成功和失败的采集结果合并后统一更新状态
     * @param successList 成功的采集结果列表
     * @param failList 失败的采集结果列表
     */
    private void updateCollectPointStatus(List<CollectResultVo> successList, List<CollectResultVo> failList) {
        // 合并成功和失败的结果列表
        List<CollectResultVo> allList = new ArrayList<>();
        allList.addAll(successList);
        allList.addAll(failList);

        // 调用服务更新所有采集点状态，标记为错误重试任务类型
        calculateService.updateCollectPointStatus(allList, JobTypeEnum.ERROR_RETRY_JOB.getVal());
    }

    /**
     * 生成聚合状态
     * 针对重试成功的采集点位，生成后续的聚合计算状态
     * @param successList 成功的采集结果列表
     */
    private void generateAggregationStatus(List<CollectResultVo> successList) {
        // 从成功结果中提取点位状态信息
        List<CollectPointStatus> successPointList = successList.stream()
                .map(CollectResultVo::getPointStatus)
                .toList();

        // 如果有成功点位，调用聚合状态生成器进行后续处理
        if (!successPointList.isEmpty()) {
            this.aggregationStatusGenerator.generateAggregationStatusAfterRetryBackfill(successPointList);
        }
    }

}
