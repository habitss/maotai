package com.keytech.modules.line.efficiency.dto;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.lang.Pair;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * sku数据
 * @author aolei
 * @create 2025-09-29 16:07
 */
@Data
public class SkuDataDto {
    /**
     * sku值
     */
    String skuValue;

    /**
     * sku值，汉化
     */
    String skuValueName;

    /**
     * sku speed
     */
    Double skuSpeed;

    /**
     * sku编码
     */
    String skuCode;

    /**
     * sku开始时间
     */
    LocalDateTime startTime;

    /**
     * sku结束时间
     */
    LocalDateTime endTime;

    /**
     * sku产量
     */
    Double outPut;

}
