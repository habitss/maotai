package com.keytech.modules.ems.dto;

import com.keytech.modules.ems.entity.CollectPointStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采集批次DTO
 * @author yepeng
 * @since 2025.05.04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectBatchDto {
    //查询底数开始时间
    private LocalDateTime startTime;
    //查询底数结束时间
    private LocalDateTime endTime;
    //查询底数的点位
    private List<CollectPointStatus> points;

    public CollectBatchDto(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
