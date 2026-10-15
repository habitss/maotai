package com.keytech.modules.maotai.cockpit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 *
 * 品牌库存数据VO
 * <p>
 * 对应前端 updateInventoryData 的标准结构。
 *
 * @author keytech
 * @since 2026-07-29
 */
@Data
@Accessors(chain = true)
@Schema(description = "品牌库存数据")
public class InventoryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 库存总量（吨） */
    @Schema(description = "库存总量", example = "3109")
    private Double total;

    /** 单位 */
    @Schema(description = "单位", example = "吨")
    private String unit;

    /** 各品牌库存明细列表 */
    @Schema(description = "各品牌库存明细")
    private List<InventoryItemVO> items;
}
