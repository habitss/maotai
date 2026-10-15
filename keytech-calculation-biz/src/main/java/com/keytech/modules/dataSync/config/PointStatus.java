package com.keytech.modules.dataSync.config;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PointStatus {
    /** 点位ID（显式存储） */
    private String dataPointId;
    /** 实时任务最后写入时间（实时任务持续更新） */
    private LocalDateTime lastWriteTime;
    /** 最后写入值 */
    private Double lastValue;
    /** 是否中断 */
    private boolean interrupted;
    /** 历史补全状态 */
    private HistoryFillStatus historyFillStatus = HistoryFillStatus.CLOSE;
    /** 历史补全最后执行的结束时间 */
    private LocalDateTime lastHistoryEndTime;
    /** 中断触发时的基准时间（仅首次检测到中断时记录） */
    private LocalDateTime interruptBaseTime;
    /** 中断恢复时的结束时间（仅数据恢复时记录） */
    private LocalDateTime interruptEndTime;
}