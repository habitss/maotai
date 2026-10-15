package com.keytech.modules.ems.job;

import com.keytech.modules.ems.constants.*;
import com.keytech.modules.ems.dto.CollectBatchDto;
import com.keytech.modules.ems.entity.CollectPointStatus;
import com.keytech.modules.ems.service.ICollectPointStatusService;
import com.keytech.modules.ems.service.impl.EmsCalculateService;
import com.keytech.modules.ems.vo.CollectResultVo;
import com.keytech.modules.ems.vo.CollectionResults;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import com.keytech.modules.ems.util.SplitUtils;

/**
 * 正常小时采集器
 * 负责执行周期性数据采集任务，包括小时级和半小时级的数据采集
 *
 * @author yepeng
 * @since 2025.05.05
 */
@Slf4j
@Component
public class CollectorJobHandler {
    private final ICollectPointStatusService collectPointStatusService; // 采集点位状态服务
    private final EmsCalculateService calculateService; // 计算服务
    private final ExecutorService executorService; // 线程池执行服务
    private final AggregationStatusGenerator aggregationStatusGenerator; // 聚合状态生成器

    /**
     * 构造函数，通过Spring依赖注入初始化服务组件
     *
     * @param collectPointStatusService 采集点位状态服务
     * @param calculateService 计算服务
     * @param executorService 线程池执行服务
     * @param aggregationStatusGenerator 聚合状态生成器
     */
    public CollectorJobHandler(
            ICollectPointStatusService collectPointStatusService,
            EmsCalculateService calculateService,
            @Qualifier("collectExecutor") ExecutorService executorService,
            AggregationStatusGenerator aggregationStatusGenerator
    ) {
        this.collectPointStatusService = collectPointStatusService;
        this.calculateService = calculateService;
        this.executorService = executorService;
        this.aggregationStatusGenerator = aggregationStatusGenerator;
    }

    /**
     * 采集小时点位任务
     * 通过XXL-JOB调度，每小时的第5分钟执行
     *
     * @param params 任务参数
     * @return 执行结果
     */
    @XxlJob("collectHourPointJob")
    public ReturnT<String> collectHourPointJob(String params) {
        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        log.info("[采集任务] 启动小时周期采集任务...");
        try {
            // 执行周期性采集，时间跨度为1小时
            return executePeriodicCollection(GrainTypeEnum.HOUR,
                    collectTime -> new CollectBatchDto(collectTime, collectTime.plusHours(1)));
        } catch (Exception e) {
            log.error("collectHourPointJob 任务执行失败", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "collectHourPointJob 执行失败：" + e.getMessage());
        } finally {
            log.info("[采集任务] 结束小时周期采集任务...");
        }
    }

    /**
     * 采集半小时点位任务
     * 通过XXL-JOB调度，每小时的第5分钟和第35分钟执行
     *
     * @param params 任务参数
     * @return 执行结果
     */
    @XxlJob("collectHalfHourPointJob")
    public ReturnT<String> collectHalfHourPointJob(String params) {
        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        log.info("[采集任务] 启动半小时周期采集任务...");
        try {
            // 执行周期性采集，时间跨度为30分钟
            return executePeriodicCollection(GrainTypeEnum.HALFHOUR,
                    collectTime -> new CollectBatchDto(collectTime, collectTime.plusMinutes(30)));
        } catch (Exception e) {
            log.error("collectHalfHourPointJob 任务执行失败", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "collectHalfHourPointJob 执行失败：" + e.getMessage());
        } finally {
            log.info("[采集任务] 结束半小时周期采集任务...");
        }
    }

    /**
     * 执行周期性采集的核心方法
     *
     * @param grainType 采集粒度类型（小时/半小时）
     * @param timeCalculator 批次时间计算器
     * @return 执行结果
     */
    private ReturnT<String> executePeriodicCollection(GrainTypeEnum grainType, BatchTimeCalculator timeCalculator) {
        // 1. 查询待采集点位
        List<CollectPointStatus> pointStatusList = collectPointStatusService.queryPendingForPeriodic(grainType.getVal());
        if (pointStatusList.isEmpty()) {
            log.warn("没有需要采集的{}点位，任务结束", grainType.getName());
            return new ReturnT<>(ReturnT.FAIL_CODE, "没有需要采集的" + grainType.getName() + "点位，任务结束");
        }
        log.info("[采集任务] 查询到待采集{}点位数量: {}", grainType.getName(), pointStatusList.size());

        // 2. 按采集时间分组并拆分成批次
        Map<LocalDateTime, List<CollectPointStatus>> groupedByTime = groupByCollectTime(pointStatusList);
        List<CollectBatchDto> batchList = createBatches(groupedByTime, timeCalculator);

        // 3. 执行采集任务
        CollectionResults results = executeCollectionBatches(batchList);

        // 4. 处理采集结果
        processResults(results.getSuccessList(), results.getFailList(), grainType);

        // 5. 生成聚合状态
        generateAggregationStatus(groupedByTime, grainType);

        log.info("周期{}采集完成，成功：{}，失败：{}", grainType.getName(), results.getSuccessCount(), results.getFailCount());
        return ReturnT.SUCCESS;
    }

    /**
     * 按采集时间对点位进行分组
     *
     * @param pointStatusList 点位状态列表
     * @return 按采集时间分组的Map
     */
    private Map<LocalDateTime, List<CollectPointStatus>> groupByCollectTime(List<CollectPointStatus> pointStatusList) {
        return pointStatusList.stream().collect(Collectors.groupingBy(CollectPointStatus::getCollectTime));
    }

    /**
     * 创建采集批次
     *
     * @param groupedByTime 按时间分组的点位集合
     * @param timeCalculator 批次时间计算器
     * @return 采集批次列表
     */
    private List<CollectBatchDto> createBatches(Map<LocalDateTime, List<CollectPointStatus>> groupedByTime,
                                                BatchTimeCalculator timeCalculator) {
        List<CollectBatchDto> batchList = new ArrayList<>();
        for (Map.Entry<LocalDateTime, List<CollectPointStatus>> entry : groupedByTime.entrySet()) {
            LocalDateTime collectTime = entry.getKey();
            List<CollectPointStatus> statusList = entry.getValue();

            // 将每个时间点的点位列表拆分为更小的批次
            List<List<CollectPointStatus>> subBatches = SplitUtils.splitList(statusList, EmsConstantsX.BATCH_TAG_NUM);

            // 为每个子批次创建采集批次对象
            for (List<CollectPointStatus> batch : subBatches) {
                CollectBatchDto batchDto = timeCalculator.calculateBatchTime(collectTime);
                batchDto.setPoints(batch);
                batchList.add(batchDto);
            }
        }
        return batchList;
    }

    /**
     * 执行采集批次任务
     *
     * @param batchList 采集批次列表
     * @return 采集结果集合
     */
    private CollectionResults executeCollectionBatches(List<CollectBatchDto> batchList) {
        // 1. 构建并发任务
        List<Callable<List<CollectResultVo>>> tasks = new ArrayList<>();
        for (CollectBatchDto batch : batchList) {
            tasks.add(() -> calculateService.batchCollectPoints(batch.getPoints(), batch.getStartTime(), batch.getEndTime()));
        }

        // 2. 执行并发任务
        List<Future<List<CollectResultVo>>> futures;
        try {
            futures = executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("任务执行被中断", e);
            return new CollectionResults(Collections.emptyList(), Collections.emptyList());
        }

        // 3. 汇总结果
        List<CollectResultVo> successList = new ArrayList<>();
        List<CollectResultVo> failList = new ArrayList<>();

        for (Future<List<CollectResultVo>> future : futures) {
            try {
                List<CollectResultVo> batchResult = future.get();
                processBatchResult(batchResult, successList, failList);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("子任务被中断", e);
            } catch (Exception e) {
                log.error("任务执行异常", e);
            }
        }

        return new CollectionResults(successList, failList);
    }

    /**
     * 处理批量采集结果，将结果分为成功和失败两类
     * @param batchResult 批量采集结果列表
     * @param successList 成功结果存放列表
     * @param failList 失败结果存放列表
     */
    private void processBatchResult(List<CollectResultVo> batchResult,
                                    List<CollectResultVo> successList,
                                    List<CollectResultVo> failList) {
        for (CollectResultVo result : batchResult) {
            if (result.isSuccess()) {
                successList.add(result);
            } else {
                failList.add(result);
            }
        }
    }

    /**
     * 处理采集结果（成功和失败的）
     * 1. 将成功结果批量插入数据库
     * 2. 记录所有错误日志（包括部分成功的记录中的错误）
     * 3. 更新所有采集点的状态
     * @param successList 成功结果列表
     * @param failList 失败结果列表
     * @param grainType 采集粒度类型（小时/半小时等）
     */
    private void processResults(List<CollectResultVo> successList, List<CollectResultVo> failList, GrainTypeEnum grainType) {
        // 落库处理
        if (!successList.isEmpty()) {
            calculateService.batchInsertUsageDb(successList, grainType.getVal());
        }

        // 记录错误日志（包含完全失败和部分成功的错误）
        List<CollectResultVo> errorList = new ArrayList<>();
        errorList.addAll(failList);
        errorList.addAll(successList.stream()
                .filter(s -> s.getErrorType() != CollectErrorTypeEnum.NORMAL.getVal())
                .toList());
        if (!errorList.isEmpty()) {
            calculateService.batchInsertCollectLog(errorList);
        }

        // 更新采集点位状态（包含成功和失败的所有记录）
        List<CollectResultVo> allList = new ArrayList<>();
        allList.addAll(successList);
        allList.addAll(failList);
        calculateService.updateCollectPointStatus(allList, JobTypeEnum.PERIOD_COLLECT_JOB.getVal());
    }

    /**
     * 生成聚合状态
     * 只在小时采集或半小时采集中的整点时间生成聚合状态
     * @param groupedByTime 按时间分组的状态列表
     * @param grainType 采集粒度类型
     */
    private void generateAggregationStatus(Map<LocalDateTime, List<CollectPointStatus>> groupedByTime, GrainTypeEnum grainType) {
        for (LocalDateTime utime : groupedByTime.keySet()) {
            // 只有小时采集或半小时采集中的整点才生成聚合状态
            if (grainType == GrainTypeEnum.HOUR || utime.getMinute() == 0) {
                aggregationStatusGenerator.generateAggregationStatusAfterHourCollect(utime, grainType.getVal());
            }
        }
    }

    /**
     * 批次时间计算器函数式接口
     * 用于计算采集批次的时间信息
     */
    @FunctionalInterface
    private interface BatchTimeCalculator {
        CollectBatchDto calculateBatchTime(LocalDateTime collectTime);
    }

}

