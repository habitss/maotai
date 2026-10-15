package com.keytech.modules.kpi.dto;

import lombok.Data;

/**
 * KPI一致性DTO
 */
@Data
public class ConsistencyDto {

    /**
     * 工厂编码（查询参数）
     */
    private String factoryCode;

    /**
     * 工厂编码
     */
    private String factory;

    /**
     * 工厂名称
     */
    private String factoryName;

    /**
     * KPI编码
     */
    private String kpiCode;

    /**
     * KPI名称
     */
    private String kpiName;

    /**
     * PI编码
     */
    private String piCode;

    /**
     * PI名称
     */
    private String piName;

    /**
     * 权重
     */
    private String weight;

    /**
     * 绿区批次
     */
    private Integer greenBatchCount;

    /**
     * 总批次
     */
    private Integer totalBatchCount;

    /**
     * 合格率
     */
    private String passRate;

    /**
     * 重复次数
     */
    private Integer repeatCount;

    /**
     * 重复率
     */
    private String repeatRate;

    /**
     * 日期(周)-查询参数
     * 每周的最后一天格式：yyyy-MM-dd
     */
    private String week;

    /**
     * 日期(周)
     * 每周的最后一天格式：yyyy-MM-dd
     */
    private String weekDate;

    /**
     * 月年累
     * KPI月年累
     */
    private String monthYearAccumulate;

    /**
     * 月累
     * KPI月累
     */
    private String monthAccumulate;

}