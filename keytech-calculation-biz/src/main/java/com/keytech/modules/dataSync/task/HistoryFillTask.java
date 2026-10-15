package com.keytech.modules.dataSync.task;

import cn.hutool.core.collection.CollUtil;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.keytech.modules.dataSync.config.DataPoint;
import com.keytech.modules.dataSync.config.HistoryFillStatus;
import com.keytech.modules.dataSync.config.PointStatus;
import com.keytech.modules.dataSync.config.RedisStatusManager;
import com.keytech.modules.dataSync.service.DataPointRepository;
import com.keytech.modules.pi.common.InfluxDBClientHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 历史数据补全任务（按点位补全）- 基于timestampFrom/timestampTo拆分10秒粒度
 * @author wangxiao
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HistoryFillTask {
    private final DataPointRepository dataPointRepository;
    private final RedisStatusManager statusManager;
    /**
     * influxDB客户端
     */
    @Resource
    private InfluxDBClient influxDBClient;
    @Resource
    private InfluxDBClientHelper influxDBClientHelper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 补全任务超时时间（避免RUNNING状态卡死）
     * */
    private static final int FILL_TIMEOUT_MINUTES = 30;
    /**
     * 数据拆分粒度（10秒）
     * */
    private static final int SPLIT_INTERVAL_SECONDS = 10;

    /**
     * 每5分钟执行
     * */
    @XxlJob("HistoryFillTask")
    public void execute() {
        try {
            // 1. 获取所有点位的状态键
            Set<String> keys = redisTemplate.keys(RedisStatusManager.POINT_STATUS_PREFIX + "*");
            if (CollUtil.isEmpty(keys)) {
                log.info("Redis中无点位状态数据，本次补全任务跳过");
                return;
            }

            // 2. 遍历所有点位，筛选出需要补全的点位
            for (String key : keys) {
                String dataPointId = key.replace(RedisStatusManager.POINT_STATUS_PREFIX, "");
                PointStatus pointStatus = statusManager.getPointStatus(dataPointId);

                // 3. 筛选需要处理的点位
                HistoryFillStatus currentStatus = pointStatus.getHistoryFillStatus();
                if (currentStatus == HistoryFillStatus.OPEN) {
                    // 首次补全前，初始化lastHistoryEndTime（若为null）
                    if (pointStatus.getLastHistoryEndTime() == null && pointStatus.getInterruptBaseTime() != null) {
                        statusManager.updateLastHistoryEndTime(dataPointId, pointStatus.getInterruptBaseTime());
                        log.info("点位[{}]初始化lastHistoryEndTime为中断基准时间：{}",
                                dataPointId, pointStatus.getInterruptBaseTime());
                    }
                    processFillTask(dataPointId, pointStatus);
                } else if (currentStatus == HistoryFillStatus.RUNNING) {
                    // 超时检测
                    LocalDateTime lastRunTime = pointStatus.getLastHistoryEndTime();
                    if (lastRunTime != null) {
                        long diffMinutes = ChronoUnit.MINUTES.between(lastRunTime, LocalDateTime.now());
                        if (diffMinutes > FILL_TIMEOUT_MINUTES) {
                            log.warn("点位[{}]补全任务超时，重置为待补全", dataPointId);
                            statusManager.updateHistoryFillStatus(dataPointId, HistoryFillStatus.OPEN);
                            processFillTask(dataPointId, pointStatus);
                        }
                    } else {
                        log.warn("点位[{}]补全任务无执行时间，重置为待补全", dataPointId);
                        statusManager.updateHistoryFillStatus(dataPointId, HistoryFillStatus.OPEN);
                        // 超时重置时，兜底初始化lastHistoryEndTime
                        if (pointStatus.getInterruptBaseTime() != null) {
                            statusManager.updateLastHistoryEndTime(dataPointId, pointStatus.getInterruptBaseTime());
                        }
                        processFillTask(dataPointId, pointStatus);
                    }
                }
            }

        } catch (Exception e) {
            log.error("历史补全任务执行失败", e);
        }
    }

    /**
     * 处理点位的历史补全任务
     * 该方法负责补全指定点位在中断时间段内的历史数据，将原始数据按10秒粒度拆分后批量写入InfluxDB。
     * 主要功能包括：
     * 1. 校验中断时间范围的有效性
     * 2. 查询历史数据或获取实时最新值
     * 3. 按时间范围拆分数据为10秒粒度
     * 4. 批量写入InfluxDB并更新状态
     * 5. 清空中断相关的时间字段
     *
     * @param dataPointId 点位ID，标识需要补全数据的点位
     * @param pointStatus 点位状态对象，包含中断时间、最后历史结束时间等信息
     */
    private void processFillTask(String dataPointId, PointStatus pointStatus) {
        statusManager.updateHistoryFillStatus(dataPointId, HistoryFillStatus.RUNNING);
        log.info("开始处理点位[{}]的历史补全任务", dataPointId);

        try {
            // 1. 获取中断时间范围（开始+结束）
            LocalDateTime interruptBaseTime = pointStatus.getInterruptBaseTime();
            LocalDateTime interruptEndTime = pointStatus.getInterruptEndTime();

            // 校验：中断开始/结束时间必须存在
            if (interruptBaseTime == null) {
                log.warn("点位[{}]无中断基准时间，跳过补全", dataPointId);
                statusManager.updateHistoryFillStatus(dataPointId, HistoryFillStatus.CLOSE);
                return;
            }
            if (interruptEndTime == null) {
                log.warn("点位[{}]中断未恢复（无结束时间），暂不补全", dataPointId);
                // 保留待补全状态
                statusManager.updateHistoryFillStatus(dataPointId, HistoryFillStatus.OPEN);
                return;
            }

            // 2. 补全时间范围：中断开始 → 中断结束
            LocalDateTime startTime = pointStatus.getLastHistoryEndTime() != null ?
                    pointStatus.getLastHistoryEndTime() : interruptBaseTime;
            LocalDateTime endTime = interruptEndTime;

            // 防止时间范围异常
            if (startTime.isAfter(endTime)) {
                log.warn("点位[{}]补全时间范围异常（{} > {}），跳过", dataPointId, startTime, endTime);
                statusManager.updateHistoryFillStatus(dataPointId, HistoryFillStatus.CLOSE);
                return;
            }

            log.info("点位[{}]补全中断时间段数据：{} ~ {}（中断基准：{}，恢复时间：{}），将按timestampFrom/timestampTo拆分为{}秒粒度",
                    dataPointId, startTime, endTime, interruptBaseTime, interruptEndTime, SPLIT_INTERVAL_SECONDS);

            // 3. 查询该时间段数据（仅查中断开始→结束的范围）
            List<DataPoint> historyData = dataPointRepository.queryHistoryDataByPoint(
                    dataPointId, startTime, endTime);

            // 4. 历史库无数据 → 调用修改后的方法获取最新值（仅最新一条）
            DataPoint latestData = null;
            if (CollUtil.isEmpty(historyData)) {
                log.info("点位[{}]历史库无中断时间段数据，尝试获取实时最新值补全", dataPointId);
                historyData = dataPointRepository.queryRealtimeDataByPoint(dataPointId);
                if (!CollUtil.isEmpty(historyData)) {
                    // 记录最新值，用于填充10秒粒度
                    latestData = historyData.get(0);
                    // 为最新值补充时间范围（中断开始→结束）
                    if (latestData.getTimestampTo() == null) {
                        latestData.setTimestampFrom(startTime);
                        latestData.setTimestampTo(endTime);
                    }
                }
            }

            if (CollUtil.isEmpty(historyData)) {
                log.warn("点位[{}]中断时间段内无数据，结束补全", dataPointId);
                statusManager.updateHistoryFillStatus(dataPointId, HistoryFillStatus.CLOSE);
                // 无数据时，写入endTime标记已查询过
                statusManager.updateLastHistoryEndTime(dataPointId, endTime);
                statusManager.resetInterruptTime(dataPointId);
                return;
            }

            // 5. 核心：基于每条数据的timestampFrom/timestampTo拆分10秒粒度数据
            List<DataPoint> splitDataList = splitDataByTimeRange(historyData, startTime, endTime);
            log.info("点位[{}]拆分后生成{}条10秒粒度数据", dataPointId, splitDataList.size());

            // 6. 批量写入InfluxDB（基于拆分后的数据）
            List<Point> points = new ArrayList<>();
            int batchSize = 1000;
            for (int i = 0; i < splitDataList.size(); i++) {
                DataPoint dataPoint = splitDataList.get(i);
                Point point = Point.measurement(influxDBClientHelper.tagTable)
                        .time(dataPoint.getTimestampFrom().atZone(ZoneId.systemDefault()).toInstant(), WritePrecision.MS)
                        .addField(influxDBClientHelper.field, dataPoint.getValue())
                        .addTag(influxDBClientHelper.tag, dataPoint.getDataPointId());
                if (point != null) {
                    points.add(point);
                }

                if ((i + 1) % batchSize == 0 || i == splitDataList.size() - 1) {
                    influxDBClient.makeWriteApi().writePoints(points);
                    points.clear();
                    // 分段写入，实时更新lastHistoryEndTime（适配大批量数据）
                    LocalDateTime batchEndTime = dataPoint.getTimestampFrom();
                    statusManager.updateLastHistoryEndTime(dataPointId, batchEndTime);
                    log.debug("点位[{}]批量写入{}条10秒粒度数据，更新lastHistoryEndTime为{}",
                            dataPointId, (i + 1), batchEndTime);
                }
            }

            // 7. 补全完成：更新状态 + 清空冗余字段
            statusManager.updateLastWriteInfo(dataPointId, endTime,
                    splitDataList.get(splitDataList.size() - 1).getValue().doubleValue());
            statusManager.updateHistoryFillStatus(dataPointId, HistoryFillStatus.CLOSE);
            statusManager.updateInterruptedStatus(dataPointId, false);
            // 先写入最终的endTime，再清空（保证分段补全的最后状态）
            statusManager.updateLastHistoryEndTime(dataPointId, endTime);
            statusManager.resetInterruptTime(dataPointId);
            // 最终清空lastHistoryEndTime（补全完成后状态归零）
            statusManager.updateLastHistoryEndTime(dataPointId, null);

            log.info("点位[{}]中断时间段（{}~{}）补全完成，共写入{}条10秒粒度数据，已清空中断/补全时间字段",
                    dataPointId, interruptBaseTime, interruptEndTime, splitDataList.size());

        } catch (Exception e) {
            log.error("点位[{}]补全失败", dataPointId, e);
            // 异常时，保留lastHistoryEndTime（下次从失败位置继续）
            statusManager.updateHistoryFillStatus(dataPointId, HistoryFillStatus.OPEN);
        }
    }

    /**
     * 基于每条数据的timestampFrom/timestampTo拆分10秒粒度数据
     * @param originData 原始历史数据（每条带timestampFrom/timestampTo区间）
     * @param globalStart 全局开始时间（中断开始时间）
     * @param globalEnd 全局结束时间（中断结束时间）
     * @return 10秒粒度的DataPoint列表
     */
    private List<DataPoint> splitDataByTimeRange(List<DataPoint> originData,
                                                 LocalDateTime globalStart,
                                                 LocalDateTime globalEnd) {
        List<DataPoint> splitList = new ArrayList<>();

        for (DataPoint originDp : originData) {
            // 1. 确定当前数据的有效时间范围（与全局范围取交集）
            LocalDateTime dataStart = originDp.getTimestampFrom() != null ? originDp.getTimestampFrom() : globalStart;
            LocalDateTime dataEnd = originDp.getTimestampTo() != null ? originDp.getTimestampTo() : globalEnd;

            // 取与全局范围的交集（避免超出中断时间范围）
            LocalDateTime effectiveStart = dataStart.isAfter(globalStart) ? dataStart : globalStart;
            LocalDateTime effectiveEnd = dataEnd.isBefore(globalEnd) ? dataEnd : globalEnd;

            if (effectiveStart.isAfter(effectiveEnd)) {
                log.debug("点位[{}]数据时间范围{}~{}超出全局范围，跳过拆分",
                        originDp.getDataPointId(), dataStart, dataEnd);
                continue;
            }

            // 2. 对齐起始时间到10秒整点
            LocalDateTime currentTime = alignTo10Seconds(effectiveStart);
            // 3. 按10秒步长拆分当前数据的时间区间
            while (currentTime.isBefore(effectiveEnd) || currentTime.isEqual(effectiveEnd)) {
                // 构建10秒粒度数据
                DataPoint splitDp = new DataPoint();
                // 复用原始数据的所有基础属性
                splitDp.setDataPointId(originDp.getDataPointId());
                splitDp.setDataPointName(originDp.getDataPointName());
                splitDp.setEquipmentId(originDp.getEquipmentId());
                splitDp.setEquipmentName(originDp.getEquipmentName());
                splitDp.setUomName(originDp.getUomName());
                // 复用原始数据的值
                splitDp.setValue(originDp.getValue());
                // 10秒粒度时间点
                splitDp.setTimestampFrom(currentTime);
                // timestampTo可设为currentTime+10秒（可选，标记该10秒区间）
                splitDp.setTimestampTo(currentTime.plusSeconds(SPLIT_INTERVAL_SECONDS));

                splitList.add(splitDp);

                // 4. 步进10秒
                currentTime = currentTime.plusSeconds(SPLIT_INTERVAL_SECONDS);
            }
        }

        // 去重（避免多条原始数据的时间区间重叠导致重复）
        return deduplicateSplitData(splitList);
    }

    /**
     * 将时间对齐到10秒整点（例如：10:00:03 → 10:00:00，10:00:12 → 10:00:10）
     */
    private LocalDateTime alignTo10Seconds(LocalDateTime time) {
        if (time == null) {
            return LocalDateTime.now();
        }
        // 计算秒数对10取余，减去余数对齐到10秒
        int seconds = time.getSecond();
        int remainder = seconds % SPLIT_INTERVAL_SECONDS;
        // 清空纳秒，避免精度问题
        return time.minusSeconds(remainder)
                .withNano(0);
    }

    /**
     * 去重拆分后的数据（按timestampFrom去重，保留最后一条）
     */
    private List<DataPoint> deduplicateSplitData(List<DataPoint> splitList) {
        Map<LocalDateTime, DataPoint> uniqueMap = new ConcurrentHashMap<>();
        for (DataPoint dp : splitList) {
            uniqueMap.put(dp.getTimestampFrom(), dp);
        }
        return new ArrayList<>(uniqueMap.values());
    }
}