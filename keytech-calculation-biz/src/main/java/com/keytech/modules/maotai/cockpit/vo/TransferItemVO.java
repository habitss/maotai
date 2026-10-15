package com.keytech.modules.maotai.cockpit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 *
 * 单品牌调拨数据VO（月度/年度通用）
 *
 * @author keytech
 * @since 2026-07-29
 */
@Data
@Accessors(chain = true)
@Schema(description = "单品牌调拨数据")
public class TransferItemVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 品牌名称 */
    @Schema(description = "品牌名称", example = "飞天茅台")
    private String name;

    /** 品牌简称（柱状图标签） */
    @Schema(description = "品牌简称", example = "飞天")
    private String shortName;

    /** 计划量（吨） */
    @Schema(description = "计划量", example = "950")
    private Double planned;

    /** 实际量（吨） */
    @Schema(description = "实际量", example = "780")
    private Double actual;
}
