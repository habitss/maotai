package com.keytech.modules.plan.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class PlanOrderKey {
    // Getters (可选)
    private final String taskId;
    private final LocalDateTime autoTriggerCalcTime;

    public PlanOrderKey(String taskId, LocalDateTime autoTriggerCalcTime) {
        this.taskId = taskId;
        this.autoTriggerCalcTime = autoTriggerCalcTime;
    }

    // 必须重写 equals 和 hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanOrderKey that = (PlanOrderKey) o;
        return Objects.equals(taskId, that.taskId) &&
                Objects.equals(autoTriggerCalcTime, that.autoTriggerCalcTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, autoTriggerCalcTime);
    }

    // toString 便于调试
    @Override
    public String toString() {
        return "PlanOrderKey{" +
                "taskId=" + taskId +
                ", autoTriggerCalcTime=" + autoTriggerCalcTime +
                '}';
    }
}
