package com.keytech.modules.ems.job;

import com.keytech.modules.ems.constants.AggregationTypeEnum;
import com.keytech.modules.ems.entity.AggregationStatus;
import com.keytech.modules.ems.service.IAggregationCalcExecutor;
import com.keytech.modules.ems.service.IAggregationStatusService;
import com.keytech.modules.ems.util.CalcuDebugHelper;
import com.keytech.modules.ems.vo.CalcuDebugResult;
import com.xxl.job.core.biz.model.ReturnT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 通用批处理执行器
 * @author yepeng
 * @since 2025.05.13
 */
@Slf4j
@Component
public class AggregationBatchProcessor {
    private final IAggregationStatusService aggregationStatusService;
    private final ExecutorService executorService;
    private final RedisTemplate<String, String> redisTemplate;

    public AggregationBatchProcessor(
            IAggregationStatusService aggregationStatusService,
            @Qualifier("aggregatExecutor") ExecutorService executorService,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.aggregationStatusService = aggregationStatusService;
        this.executorService = executorService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 执行批处理
     * @param calcExecutor 聚合执行器
     * @param aggregationEnum 聚合类型(采集表班,采集表日,第三方班,第三方日,中间点日周月等)
     */
    public void execute(IAggregationCalcExecutor calcExecutor, AggregationTypeEnum aggregationEnum) {
        //判断是否开启调试模式，如果开启则找到调试的点位集合
        CalcuDebugResult debugResult = CalcuDebugHelper.getCalcuDebugResult(redisTemplate);
        if(debugResult.isDebug()) {
            log.warn("开启调试模式，只处理调试点位: [C]-{} [O]-{}", debugResult.getCTagIds(), debugResult.getOTagIds());
            if (debugResult.getCTagIds().isEmpty() && debugResult.getOTagIds().isEmpty()) {
                log.warn("无调试点位，跳过聚合执行器");
            }
        }

        try {
            List<AggregationStatus> pendingList = aggregationStatusService.queryPendingAggregationByType(aggregationEnum);
            //调试模式开启，只处理调试点位
            pendingList = filterDebugList(debugResult, aggregationEnum, pendingList);

            if (pendingList.isEmpty()) {
                log.info("[{}] 无待计算任务", aggregationEnum.getName());
                return;
            }
            //按时间段分组，key=时间段开始时间，value=该时间段的状态集合
            Map<LocalDateTime, List<AggregationStatus>> timeGroup = pendingList.stream().collect(Collectors.groupingBy(AggregationStatus::getTimePoint));

            List<Future<?>> futures = new ArrayList<>();
            for (Map.Entry<LocalDateTime, List<AggregationStatus>> entry : timeGroup.entrySet()) {
                futures.add(executorService.submit(() -> {
                    try {
                        calcExecutor.execute(entry.getValue());
                    } catch (Exception e) {
                        log.error("[{}] 聚合失败 时间段={}", aggregationEnum.getName(), entry.getKey(), e);
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            log.info("[{}] 任务完成", aggregationEnum.getName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[{}] 任务被中断", aggregationEnum.getName(), e);
        } catch (Exception e) {
            log.error("[{}] 执行异常", aggregationEnum.getName(), e);
        }
    }

    /**
     * 过滤调试点位
     * @param debugResult 调试配置结果
     * @param aggregationEnum 聚合类型
     * @param pendingList 待处理列表
     */
    private List<AggregationStatus> filterDebugList(CalcuDebugResult debugResult, AggregationTypeEnum aggregationEnum, List<AggregationStatus> pendingList) {
        if(debugResult.isDebug()) {
            if (aggregationEnum.getVal().equals(AggregationTypeEnum.COLLECT_SHIFT.getVal()) || aggregationEnum.getVal().equals(AggregationTypeEnum.COLLECT_DAY.getVal())) {
                return pendingList.stream().filter(s -> debugResult.getCTagIds().contains(s.getTagId())).toList();
            }else {
                return pendingList.stream().filter(s -> debugResult.getOTagIds().contains(s.getTagId())).toList();
            }
        }
        return pendingList;
    }

}
