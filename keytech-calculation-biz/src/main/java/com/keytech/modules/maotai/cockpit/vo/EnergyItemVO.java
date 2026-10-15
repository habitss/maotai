package com.keytech.modules.maotai.cockpit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 *
 * 能耗单行数据VO
 * <p>
 * 对应前端 mapEnergyResponse 中数组的单条记录。
 * 前端兼容字段：name/energyName/typeName、value/today/todayValue、
 * plan/dailyPlan/planValue、perUnit/unitConsumption、state/status/evaluation。
 *
 * @author keytech
 * @since 2026-07-29
 */
@Data
@Accessors(chain = true)
@Schema(description = "能耗单行数据")
public class EnergyItemVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 能源名称（生产水、电、压缩空气） */
    @Schema(description = "能源名称", example = "生产水")
    private String name;

    /** 今日消耗量 */
    @Schema(description = "今日消耗量", example = "194.0")
    private Double value;

    /** 单位 */
    @Schema(description = "单位", example = "m³")
    private String unit;

    /** 日计划量 */
    @Schema(description = "日计划量", example = "210.0")
    private Double plan;

    /** 单耗（每吨产品消耗量） */
    @Schema(description = "单耗", example = "0.58")
    private String perUnit;

    /** 评价状态：normal/warning/alarm */
    @Schema(description = "评价状态", example = "normal")
    private String state;
}
