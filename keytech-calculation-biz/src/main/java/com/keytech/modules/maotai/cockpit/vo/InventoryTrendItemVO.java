package com.keytech.modules.maotai.cockpit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 品牌库存趋势-品牌项
 *
 * @author cockpit
 * @since 2026-09-10
 */
@Data
@Accessors(chain = true)
@Schema(description = "品牌库存趋势品牌项")
public class InventoryTrendItemVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "品牌名称")
    private String name;

    @Schema(description = "库存量(吨)")
    private Double quantity;
}
