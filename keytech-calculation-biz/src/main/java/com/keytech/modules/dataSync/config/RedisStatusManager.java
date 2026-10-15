package com.keytech.modules.dataSync.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * Redis状态管理工具类（Map结构存储）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStatusManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static final String POINT_STATUS_PREFIX = "data_sync:point_status:";

    /**
     * 保存点位完整状态信息（显式设置dataPointId）
     */
    public void savePointStatus(String dataPointId, PointStatus status) {
        try {
            // 显式设置点位ID到状态对象中
            status.setDataPointId(dataPointId);
            String key = POINT_STATUS_PREFIX + dataPointId;
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(status));
        } catch (JsonProcessingException e) {
            log.error("序列化点位状态失败: {}", dataPointId, e);
        }
    }

    /**
     * 获取点位完整状态信息
     */
    public PointStatus getPointStatus(String dataPointId) {
        try {
            String key = POINT_STATUS_PREFIX + dataPointId;
            String statusJson = (String) redisTemplate.opsForValue().get(key);
            if (statusJson == null) {
                return new PointStatus(); // 返回默认状态
            }
            return objectMapper.readValue(statusJson, PointStatus.class);
        } catch (Exception e) {
            log.error("获取点位状态失败: {}", dataPointId, e);
            return new PointStatus();
        }
    }

    /**
     * 更新点位最后写入信息
     */
    public void updateLastWriteInfo(String dataPointId, LocalDateTime time, Double value) {
        PointStatus status = getPointStatus(dataPointId);
        status.setDataPointId(dataPointId);  // 确保更新时ID存在
        status.setLastWriteTime(time);
        status.setLastValue(value);
        savePointStatus(dataPointId, status);
    }

    /**
     * 更新点位中断状态
     */
    public void updateInterruptedStatus(String dataPointId, boolean interrupted) {
        PointStatus status = getPointStatus(dataPointId);
        status.setInterrupted(interrupted);
        savePointStatus(dataPointId, status);
    }

    /**
     * 更新历史补全状态
     */
    public void updateHistoryFillStatus(String dataPointId, HistoryFillStatus status) {
        PointStatus pointStatus = getPointStatus(dataPointId);
        pointStatus.setHistoryFillStatus(status);
        savePointStatus(dataPointId, pointStatus);
    }

    /**
     * 更新历史补全结束时间
     */
    public void updateLastHistoryEndTime(String dataPointId, LocalDateTime endTime) {
        PointStatus status = getPointStatus(dataPointId);
        status.setLastHistoryEndTime(endTime);
        savePointStatus(dataPointId, status);
    }

    /**
     * 删除点位状态
     */
    public void deletePointStatus(String dataPointId) {
        String key = POINT_STATUS_PREFIX + dataPointId;
        redisTemplate.delete(key);
    }

    public void updateInterruptBaseTime(String dataPointId, LocalDateTime interruptBaseTime) {
        PointStatus status = getPointStatus(dataPointId);
        // 仅首次设置，后续不覆盖（保证基准时间是中断前的最后写入时间）
        if (status.getInterruptBaseTime() == null) {
            status.setInterruptBaseTime(interruptBaseTime);
        }
        savePointStatus(dataPointId, status);
    }

    // 重置中断基准时间的方法（补全完成后调用）
    public void resetInterruptBaseTime(String dataPointId) {
        PointStatus status = getPointStatus(dataPointId);
        status.setInterruptBaseTime(null);
        savePointStatus(dataPointId, status);
    }

    // RedisStatusManager 中新增：
    /**
     * 更新中断结束时间（仅首次恢复时设置，不重复覆盖）
     */
    public void updateInterruptEndTime(String dataPointId, LocalDateTime interruptEndTime) {
        PointStatus status = getPointStatus(dataPointId);
        // 仅当interruptEndTime为空时才设置（避免实时任务重复更新）
        if (status.getInterruptEndTime() == null) {
            status.setInterruptEndTime(interruptEndTime);
        }
        savePointStatus(dataPointId, status);
    }

    /**
     * 重置中断时间（补全完成后调用）
     */
    public void resetInterruptTime(String dataPointId) {
        PointStatus status = getPointStatus(dataPointId);
        status.setInterruptBaseTime(null);
        // 同时重置中断结束时间
        status.setInterruptEndTime(null);
        savePointStatus(dataPointId, status);
    }
}