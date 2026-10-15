package com.keytech.modules.ems.job;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.keytech.modules.ems.constants.*;
import com.keytech.modules.ems.entity.CalcWarningLog;
import com.keytech.modules.ems.entity.CollectErrorLog;
import com.keytech.modules.ems.entity.CollectPointStatus;
import com.keytech.modules.ems.entity.CollectTag;
import com.keytech.modules.ems.service.ICalcWarningLogService;
import com.keytech.modules.ems.service.ICollectPointStatusService;
import com.keytech.modules.ems.service.ICollectTagService;
import com.keytech.modules.ems.service.impl.EmsInfluxDbService;
import com.keytech.modules.ems.service.impl.TimeSeriesAnalyzer;
import com.keytech.modules.ems.util.CalcuDebugHelper;
import com.keytech.modules.ems.util.SplitUtils;
import com.keytech.modules.ems.vo.CalcuDebugResult;
import com.keytech.modules.ems.vo.FluxZone8Data;
import com.keytech.modules.pi.common.MeasurementData;
import com.keytech.modules.pi.entity.PiBasePoint;
import com.keytech.modules.pi.service.IPiBasePointService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 初始化小时采集状态记录任务
 *
 * 主要功能：
 * 1. 定期初始化采集点位状态记录（每小时或每半小时执行）
 * 2. 处理手动补采任务的参考值计算
 * 3. 管理采集点位的状态和参考值计算逻辑
 *
 * 核心业务逻辑：
 * - 根据配置的时间粒度（小时/半小时）初始化采集状态记录
 * - 从InfluxDB查询历史数据作为参考值
 * - 处理未配置FluxCode的点位并生成预警
 * - 批量处理采集状态记录并入库
 *
 * @author yepeng
 * @since 2025.04.30
 */
@Slf4j
@Component
public class InitCollectPointStatusJob {
    // 依赖的服务组件
    private final ICollectTagService collectTagService; // 采集标签服务
    private final ICollectPointStatusService collectPointStatusService; // 采集状态服务
    private final TimeSeriesAnalyzer timeSeriesAnalyzer; // 时间序列分析器
    private final EmsInfluxDbService emsInfluxDbService; // InfluxDB服务
    private final ICalcWarningLogService calcWarningLogService; // 预警日志服务
    private final ExecutorService executorService; // 线程池服务
    private final RedisTemplate<String, String> redisTemplate; // Redis缓存服务

    /**
     * 构造函数，通过Spring依赖注入初始化服务组件
     * @param collectTagService 采集标签服务
     * @param collectPointStatusService 采集状态服务
     * @param timeSeriesAnalyzer 时间序列分析器
     * @param emsInfluxDbService InfluxDB服务
     * @param calcWarningLogService 预警日志服务
     * @param executorService 线程池执行服务
     */
    public InitCollectPointStatusJob(
            ICollectTagService collectTagService,
            ICollectPointStatusService collectPointStatusService,
            TimeSeriesAnalyzer timeSeriesAnalyzer,
            EmsInfluxDbService emsInfluxDbService,
            ICalcWarningLogService calcWarningLogService,
            @Qualifier("commonExecutor") ExecutorService executorService,
            RedisTemplate<String, String> redisTemplate
    ) {
        // 初始化各服务组件
        this.collectTagService = collectTagService;
        this.collectPointStatusService = collectPointStatusService;
        this.timeSeriesAnalyzer = timeSeriesAnalyzer;
        this.emsInfluxDbService = emsInfluxDbService;
        this.calcWarningLogService = calcWarningLogService;
        this.executorService = executorService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 初始化采集状态记录的主任务（XXL-JOB定时任务: 每小时 0 分 0 秒执行）
     * 参数例子：{"x-tenant-id":1006, "grainType":1}
     * 执行流程：
     * 1. 解析参数确定时间粒度（小时/半小时）
     * 2. 获取采集时间范围
     * 3. 获取需要采集的点位配置
     * 4. 获取需要补填的状态记录
     * 5. 处理并插入状态记录
     *
     * @param params 1-GrainTypeEnum.HOUR      0 0 0/1 * * ?     每小时执行一次
     * @param params 0-GrainTypeEnum.HALFHOUR  0 0,30 0/1 * * ?  每半小时执行一次
     * @return 执行结果
     */
    @XxlJob("initCollectPointStatusJob")
    public ReturnT<String> initCollectPointStatus(String params) {
        //判断是否开启调试模式，如果开启则找到调试的点位集合
        CalcuDebugResult debugResult = CalcuDebugHelper.getCalcuDebugResult(redisTemplate);
        if(debugResult.isDebug()) {
            log.warn("开启调试模式，只处理调试点位: {}", debugResult.getCTagIds());
            if (debugResult.getCTagIds().isEmpty()) {
                log.warn("无调试点位，跳过初始化采集任务");
                return new ReturnT<>(ReturnT.FAIL_CODE, "无调试点位，跳过初始化采集任务");
            }
        }

        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        JSONObject paramsObj = JSON.parseObject(params);
        int grainType = parseGrainType(paramsObj);

        log.info("[初始化采集任务状态记录] 启动初始化采集任务状态记录...{}", GrainTypeEnum.getNameByVal(grainType));

        try {
            LocalDateTime startCollectTime = getStartCollectTime(grainType);
            LocalDateTime lastCollectTime = getLastCollectTime(grainType);

            log.info("初始化采集状态记录，{}采集时间段：{}", GrainTypeEnum.getNameByVal(grainType), lastCollectTime);

            List<CollectTag> emsPointList = collectPointStatusService.getCollectPoints(grainType);
            //调试模式开启，只处理调试点位
            if(debugResult.isDebug()) {
                emsPointList = emsPointList.stream().filter(p -> debugResult.getCTagIds().contains(p.getTagId())).toList();
            }
            if (CollectionUtils.isEmpty(emsPointList)) {
                log.warn("无可用{}点位配置，跳过初始化", GrainTypeEnum.getNameByVal(grainType));
                return new ReturnT<>(ReturnT.FAIL_CODE, "无可用" + GrainTypeEnum.getNameByVal(grainType) + "点位配置");
            }

            List<CollectPointStatus> backfillStatusList = collectPointStatusService.getBackfillPointStatusList(grainType, startCollectTime, lastCollectTime, emsPointList);
            int totalInsertSize = processAndInsertCollectStatuses(grainType, backfillStatusList);
            log.info("本次共初始化 {} 条 {} 采集状态记录。", totalInsertSize, GrainTypeEnum.getNameByVal(grainType));
            return ReturnT.SUCCESS;

        } catch (Exception e) {
            log.error("initCollectPointStatusJob {} 任务执行失败", GrainTypeEnum.getNameByVal(grainType), e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "initCollectPointStatusJob " + GrainTypeEnum.getNameByVal(grainType) + " 执行失败：" + e.getMessage());
        }
    }

    /**
     * 手动补采任务的参考值计算（XXL-JOB定时任务：每 5 分 执行）
     * 0 0/5 * * * ?
     * 执行流程：
     * 1. 查询未计算参考值的状态记录
     * 2. 按点位分组处理
     * 3. 为每组记录计算参考值
     * 4. 批量更新状态记录
     *
     * @param params 任务参数
     * @return 执行结果
     */
    @XxlJob("initManualBackfillPointStatusJob")
    public ReturnT<String> initManualBackfillPointStatus(String params) {
        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        log.info("[计算手动补采任务参考值] 启动计算参考值任务...");
        try {
            int total = 0;
            //点位太多，分批处理，每批100个点位
            int batchSize = 100;
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
                    List<CollectPointStatus> backfillStatusList = collectPointStatusService.queryNotLastBaseValBackfill(grainType, null, null, tagIds);
                    Map<String, List<CollectPointStatus>> groupedByTag = backfillStatusList.stream().filter(s->StringUtils.isNotBlank(s.getFluxCode())).collect(Collectors.groupingBy(CollectPointStatus::getTagId));
                    for (Map.Entry<String, List<CollectPointStatus>> entry : groupedByTag.entrySet()) {
                        List<CollectPointStatus> statuses = entry.getValue().stream().sorted(Comparator.comparing(CollectPointStatus::getCollectTime)).toList();
                        total += processLastBasevalAndUpdateStatusList(statuses);
                    }
                    // 每批处理完毕后，休眠1秒，避免CPU过载
                    Thread.sleep(1000);
                }
            }
            log.info("本次共计算参考值 {} 条", total);
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            log.error("initManualBackfillPointStatus 任务执行失败",  e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "initManualBackfillPointStatus 执行失败：" + e.getMessage());
        } finally {
            log.info("[计算手动补采任务参考值] 结束计算参考值任务...");
        }
    }

    /**
     * 解析时间粒度参数
     * @param paramsObj JSON参数对象
     * @return 时间粒度枚举值
     */
    private int parseGrainType(JSONObject paramsObj) {
        return paramsObj.containsKey("grainType") && paramsObj.getInteger("grainType") == GrainTypeEnum.HALFHOUR.getVal()
                ? GrainTypeEnum.HALFHOUR.getVal()
                : GrainTypeEnum.HOUR.getVal();
    }

    /**
     * 获取开始采集时间
     * 逻辑：如果没有历史记录，则从当前时间前推一个周期；否则从最后一条记录时间后推一个周期
     * @param grainType 时间粒度
     * @return 开始采集时间
     */
    private LocalDateTime getStartCollectTime(int grainType) {
        CollectPointStatus lastPointStatus = collectPointStatusService.getOne(
                new QueryWrapper<CollectPointStatus>().select(" MAX(collect_time) AS collect_time").eq("grain_type", grainType)
        );

        if (lastPointStatus == null) {
            LocalDateTime now = LocalDateTime.now();
            if (grainType == GrainTypeEnum.HOUR.getVal()) {
                return now.truncatedTo(ChronoUnit.HOURS).minusHours(1);
            } else {
                if (now.getMinute() < 30) {
                    return now.truncatedTo(ChronoUnit.HOURS).minusMinutes(30);
                } else {
                    return now.withMinute(30).truncatedTo(ChronoUnit.MINUTES).minusMinutes(30);
                }
            }
        } else {
            return grainType == GrainTypeEnum.HOUR.getVal() ? lastPointStatus.getCollectTime().plusHours(1) : lastPointStatus.getCollectTime().plusMinutes(30);
        }
    }

    /**
     * 获取本次采集的截止时间
     * @param grainType 时间粒度
     * @return 截止时间
     */
    private LocalDateTime getLastCollectTime(int grainType) {
        LocalDateTime now = LocalDateTime.now();
        if (grainType == GrainTypeEnum.HOUR.getVal()) {
            return now.truncatedTo(ChronoUnit.HOURS);
        } else {
            if (now.getMinute() < 30) {
                return now.truncatedTo(ChronoUnit.HOURS);
            } else {
                return now.withMinute(30).truncatedTo(ChronoUnit.MINUTES);
            }
        }
    }

    /**
     * 处理并插入采集状态记录
     * 核心逻辑：
     * 1. 按采集时间分组
     * 2. 为每组记录补充参考值
     * 3. 处理零值记录（从InfluxDB查询历史数据）
     * 4. 批量插入记录
     *
     * @param grainType 时间粒度
     * @param backfillStatusList 待处理的状态记录
     * @return 成功插入的记录数
     */
    private int processAndInsertCollectStatuses(int grainType, List<CollectPointStatus> backfillStatusList) {
        Map<LocalDateTime, List<CollectPointStatus>> grouped = backfillStatusList.stream()
                .collect(Collectors.groupingBy(CollectPointStatus::getCollectTime));
        List<LocalDateTime> collectTimes = grouped.keySet().stream().sorted().toList();

        List<CollectPointStatus> toInsertList = new ArrayList<>();
        int directInsertSize = 0;

        for (LocalDateTime time : collectTimes) {
            List<CollectPointStatus> entries = grouped.get(time);
            enrichWithRefValue(grainType, time, entries);

            List<String> zeroBaseValCodes = entries.stream()
                    .filter(s -> s.getLastBaseVal().doubleValue() == 0)
                    .map(CollectPointStatus::getFluxCode)
                    .toList();

            boolean processed = false;
            if (!zeroBaseValCodes.isEmpty()) {
                Map<String, List<FluxZone8Data>> dataMap = queryLastBaseDataMap(zeroBaseValCodes, time);
                if (!dataMap.isEmpty()) {
                    applyLastBaseData(entries, dataMap, grainType);
                    collectPointStatusService.saveBatch(entries);
                    directInsertSize += entries.size();
                    processed = true;
                }
            }
            if (!processed) {
                toInsertList.addAll(entries);
            }
        }

        if (!toInsertList.isEmpty()) {
            collectPointStatusService.saveBatch(toInsertList);
        }
        return directInsertSize + toInsertList.size();
    }

    /**
     * 从InfluxDB批量查询历史数据作为参考值
     * 使用线程池并行查询提高效率
     * @param fluxCodes 点位编码列表
     * @param time 查询时间
     * @return 历史数据映射表
     */
    private Map<String, List<FluxZone8Data>> queryLastBaseDataMap(List<String> fluxCodes, LocalDateTime time) {
        List<List<String>> batches = SplitUtils.splitList(fluxCodes, EmsConstantsX.BATCH_TAG_NUM);
        List<Future<Map<String, List<FluxZone8Data>>>> futures = new ArrayList<>();

        for (List<String> batch : batches) {
            futures.add(executorService.submit(() ->
                    emsInfluxDbService.queryMultiPointsWithIntervalLastMap(
                            batch, time.minusMinutes(5000), time, 300)
            ));
        }

        Map<String, List<FluxZone8Data>> dataMap = new HashMap<>();
        for (Future<Map<String, List<FluxZone8Data>>> future : futures) {
            try {
                Map<String, List<FluxZone8Data>> result = future.get();
                if (result != null) {
                    dataMap.putAll(result);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("子任务被中断", e);
            } catch (Exception e) {
                log.error("查询InfluxDB失败", e);
            }
        }

        return dataMap;
    }

    /**
     * 应用查询到的历史数据到状态记录
     * @param entries 状态记录列表
     * @param dataMap 历史数据映射
     * @param grainType 时间粒度
     */
    private void applyLastBaseData(List<CollectPointStatus> entries, Map<String, List<FluxZone8Data>> dataMap, int grainType) {
        int windowSize = (grainType == GrainTypeEnum.HOUR.getVal()) ? 12 : 6;

        for (Map.Entry<String, List<FluxZone8Data>> entry : dataMap.entrySet()) {
            String fluxCode = entry.getKey();
            List<Double> values = entry.getValue().stream().map(FluxZone8Data::getValue).toList();

            TimeSeriesAnalyzer.AnalysisResult result = timeSeriesAnalyzer.analyze(values, windowSize);

            entries.stream().filter(s -> s.getFluxCode().equals(fluxCode))
                    .findFirst().ifPresent(s -> {
                        s.setLastBaseVal(result.latestNormalValue);
                        s.setLastUsage(result.valueIncrement);
                    });
        }
    }

    /**
     * 使用历史状态记录补充参考值
     * 对于零值记录，尝试从历史记录中推算当前值
     * @param grainType 时间粒度
     * @param collectTime 采集时间
     * @param pointList 状态记录列表
     */
    private void enrichWithRefValue(int grainType, LocalDateTime collectTime, List<CollectPointStatus> pointList) {
        List<String> tagIds = pointList.stream().map(CollectPointStatus::getTagId).toList();
        List<CollectPointStatus> refList = collectPointStatusService.selectLastHadBaseValRecordsByPoints(tagIds, collectTime);

        Map<String, CollectPointStatus> map = refList.stream()
                .collect(Collectors.toMap(CollectPointStatus::getTagId, Function.identity()));

        for (CollectPointStatus status : pointList) {
            if (status.getLastBaseVal().doubleValue() == 0 && map.containsKey(status.getTagId())) {
                CollectPointStatus ref = map.get(status.getTagId());
                if (ref.getLastBaseVal().doubleValue() > 0) {
                    long intervals = Duration.between(ref.getCollectTime(), status.getCollectTime()).toMinutes()
                            / (grainType == GrainTypeEnum.HOUR.getVal() ? 60 : 30);
                    status.setLastBaseVal(ref.getLastBaseVal().add(ref.getLastUsage().multiply(BigDecimal.valueOf(intervals))));
                    status.setLastUsage(ref.getLastUsage());
                }
            }
        }
    }

    /**
     * 处理单个点位的参考值计算并更新状态
     * 逻辑：
     * 1. 对于第一条记录或长时间间隔的记录，从InfluxDB查询初始值
     * 2. 其他记录基于前一条记录推算
     * @param statuses 状态记录列表
     * @return 处理的记录数
     */
    private int processLastBasevalAndUpdateStatusList(List<CollectPointStatus> statuses) {
        boolean isHasFluxData = false;
        for (int i = 0; i < statuses.size(); i++) {
            CollectPointStatus current = statuses.get(i);
            if (i == 0 || !isHasFluxData || Duration.between(statuses.get(i-1).getCollectTime(), current.getCollectTime()).toHours() > 8) {
                // 从influx获取初始值
                List<FluxZone8Data> data = emsInfluxDbService.queryMultiPointsWithIntervalLast(
                        List.of(current.getFluxCode()),
                        current.getCollectTime().minusMinutes(5000),
                        current.getCollectTime(),
                        300
                );
                if (!data.isEmpty()) {
                    TimeSeriesAnalyzer.AnalysisResult result = timeSeriesAnalyzer.analyze(
                        data.stream().map(FluxZone8Data::getValue).toList(), current.getGrainType() == GrainTypeEnum.HOUR.getVal() ? 12 : 6
                    );
                    current.setLastBaseVal(result.latestNormalValue.compareTo(EmsConstantsX.ZERO_BASE) < 0 ? EmsConstantsX.ZERO_BASE : result.latestNormalValue);
                    current.setLastUsage(result.valueIncrement);
                } else {
                    // 如果没有数据，则设置为异常点，避免后续计算
                    current.setErrorFlag(CollectErrorFlagEnum.NULL.getVal());
                }
                collectPointStatusService.updateById(current);
                // 为防止有些异常点一直为 0 不断耗费系统资源，上面强制给了ZERO_BASE，那么这里就要跟ZERO_BASE比较了
                isHasFluxData = current.getLastBaseVal() != null && current.getLastBaseVal().compareTo(EmsConstantsX.ZERO_BASE) > 0;
            } else {
                // 基于前一条记录计算
                CollectPointStatus prev = statuses.get(i-1);
                long minutesDiff = Duration.between(prev.getCollectTime(), current.getCollectTime()).toMinutes();
                BigDecimal factor = BigDecimal.valueOf(minutesDiff)
                        .divide(current.getGrainType() == GrainTypeEnum.HOUR.getVal() ? BigDecimal.valueOf(60) : BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
                current.setLastBaseVal(prev.getLastBaseVal().add(prev.getLastUsage().multiply(factor)));
                current.setLastUsage(prev.getLastUsage());
                collectPointStatusService.updateById(current);
            }
        }
        return statuses.size();
    }

}
