package com.keytech.modules.line.efficiency.dto;

import lombok.Data;

import java.util.Date;

/**
 * 产量的最新时间和产量基准
 * @author aolei
 */
@Data
public class ProductionDto {
    /**
     * 产量编码
     */
    private String productionCode;
    /**
     * 产量时间
     */
    private Date productionTime;
    /**
     * 产量基准
     */
    private Double productionBase;
    /**
     * 产量值
     */
    private Double production;
}
