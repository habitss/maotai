package com.keytech.modules.maotai.cockpit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 单品牌库存数据VO
 *
 *
 * @author keytech
 * @since 2026-07-29
 */
@Data
@Accessors(chain = true)
@Schema(description = "单品牌库存数据")
public class InventoryItemVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 品牌名称 */
    @Schema(description = "品牌名称", example = "飞天茅台")
    private String name;

    /** 库存占比（%，前端会根据 quantity 自动重算，不盲信此值） */
    @Schema(description = "库存占比", example = "22.9")
    private Double percentage;

    /** 库存数量（吨） */
    @Schema(description = "库存数量", example = "713")
    private Double quantity;

    /** 品牌颜色（可选，缺失时前端用默认色） */
    @Schema(description = "品牌颜色", example = "#ff5058")
    private String color;
}
