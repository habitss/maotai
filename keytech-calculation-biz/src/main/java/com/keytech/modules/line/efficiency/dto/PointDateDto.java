package com.keytech.modules.line.efficiency.dto;

import lombok.Data;

import java.time.Instant;

/**
 * 点位时间段集合
 * @author aolei
 * @create 2020-08-19 16:07
 */
@Data
public class PointDateDto {
    /**
     * 开始时间
     */
    private Instant startInstant;

    /**
     * 结束时间
     */
    private Instant endInstant;

    /**
     * 运行时长
     */
    private Double runningTime;

    /**
     * 值
     */
    private Double skuValue;

}
