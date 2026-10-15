package com.keytech.modules.maotai.cockpit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 品牌库存趋势-单日数据点(date + 各品牌库存量)
 *
 * @author cockpit
 * @since 2026-09-10
 */
@Data
@Accessors(chain = true)
@Schema(description = "品牌库存趋势数据点")
public class InventoryTrendPointVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "日期 yyyy-MM-dd")
    private String date;

    @Schema(description = "各品牌当日库存(吨)")
    private List<InventoryTrendItemVO> items;
}
