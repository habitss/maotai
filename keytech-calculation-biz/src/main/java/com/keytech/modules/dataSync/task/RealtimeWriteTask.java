package com.keytech.modules.dataSync.task;

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
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 实时数据写入任务（修复：仅触发补全，不干扰补全状态）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeWriteTask {
    private final DataPointRepository dataPointRepository;
    private final RedisStatusManager statusManager;
    /**
     * influxDB客户端
     */
    @Resource
    private InfluxDBClient influxDBClient;
    @Resource
    private InfluxDBClientHelper influxDBClientHelper;

    private static final int INTERRUPT_THRESHOLD_MINUTES = 10;

    @XxlJob("RealtimeWriteTask")
    public void execute() {
        try {
            List<DataPoint> realtimeData = dataPointRepository.queryRealtimeData();
            if (realtimeData.isEmpty()) {
                log.warn("实时数据为空，本次跳过");
                return;
            }

            List<Point> realtimePoints = new ArrayList<>();
            for (DataPoint dataPoint : realtimeData) {
                String dataPointId = dataPoint.getDataPointId();
                LocalDateTime dataTime = dataPoint.getTimestampFrom();
                Double value = dataPoint.getValue() != null ? dataPoint.getValue().doubleValue() : null;

                if (dataTime == null || value == null) {
                    log.warn("点位[{}]数据不完整，跳过", dataPointId);
                    continue;
                }

                PointStatus pointStatus = statusManager.getPointStatus(dataPointId);
                LocalDateTime lastWriteTime = pointStatus.getLastWriteTime();

                // 首次写入：初始化状态（仅标记未中断，不修改补全状态）
                if (lastWriteTime == null) {
                    statusManager.updateInterruptedStatus(dataPointId, false);
                    log.info("点位[{}]首次写入，初始化Redis状态", dataPointId);
                } else {
                    // 非首次写入：仅做两件事
                    // 1. 判断是否中断，若中断则触发补全（仅第一次检测到中断时置为OPEN）
                    long diffMinutes = ChronoUnit.MINUTES.between(lastWriteTime, dataTime);
                    if (diffMinutes > INTERRUPT_THRESHOLD_MINUTES) {
                        if (!pointStatus.isInterrupted()) {
                            statusManager.updateInterruptedStatus(dataPointId, true);
                            // 首次检测到中断时，记录「中断基准时间」（中断前的最后写入时间）
                            statusManager.updateInterruptBaseTime(dataPointId, lastWriteTime);
                            if (pointStatus.getHistoryFillStatus() != HistoryFillStatus.RUNNING) {
                                statusManager.updateHistoryFillStatus(dataPointId, HistoryFillStatus.OPEN);
                            }
                            log.warn("点位[{}]数据中断超过{}分钟，触发历史补全（基准时间：{}）",
                                    dataPointId, INTERRUPT_THRESHOLD_MINUTES, lastWriteTime);
                        }
                    } else if (pointStatus.isInterrupted()) {
                        // 2. 数据恢复：标记中断结束 + 记录中断结束时间（仅首次恢复时记录）
                        statusManager.updateInterruptedStatus(dataPointId, false);
                        // 记录中断结束时间（仅第一次恢复时设置，避免重复覆盖）
                        statusManager.updateInterruptEndTime(dataPointId, dataTime);
                        log.info("点位[{}]数据恢复，中断状态已清除（中断时间：{} ~ {}），补全任务将补全该时间段数据",
                                dataPointId, pointStatus.getInterruptBaseTime(), dataTime);
                    }
                }

                // 正常写入InfluxDB + 更新最后写入信息（不干扰补全状态）
                Point point = Point.measurement(influxDBClientHelper.tagTable)
                        .time(dataPoint.getTimestampFrom().atZone(ZoneId.systemDefault()).toInstant(), WritePrecision.MS)
                        .addField(influxDBClientHelper.field, dataPoint.getValue())
                        .addTag(influxDBClientHelper.tag, dataPoint.getDataPointId());

                if (point != null) {
                    realtimePoints.add(point);
                    statusManager.updateLastWriteInfo(dataPointId, dataTime, value);
                }
            }

            // 批量写入InfluxDB
            if(null != realtimePoints && realtimePoints.size() > 0){
                influxDBClient.makeWriteApi().writePoints(realtimePoints);
                log.info("实时数据批量写入成功，总条数: {}", realtimePoints.size());
            } else {
                log.warn("实时数据转换为Point后为空，本次跳过");
            }

        } catch (Exception e) {
            log.error("实时写入任务执行失败", e);
        }
    }

}