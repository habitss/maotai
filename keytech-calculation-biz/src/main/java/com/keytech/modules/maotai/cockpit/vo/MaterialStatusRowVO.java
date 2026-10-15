package com.keytech.modules.maotai.cockpit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 *
 * 实时物料单行数据VO
 * <p>
 * 对应前端 mapMaterialStatusResponse 中 rows 数组的单条记录。
 * 前端兼容字段：brand/brandName/materialName、tank/tankName/tankArea、
 * batch/batchNo/batchNumber、inventory/stock/quantity、status/state。
 *
 * @author keytech
 * @since 2026-07-29
 */
@Data
@Accessors(chain = true)
@Schema(description = "实时物料单行数据")
public class MaterialStatusRowVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 品牌名称 */
    @Schema(description = "品牌名称", example = "飞天茅台")
    private String brand;

    /** 罐区/罐体名称 */
    @Schema(description = "罐体名称", example = "ZTK101储罐")
    private String tank;

    /** 批次号 */
    @Schema(description = "批次号", example = "BATCH-20260728-001")
    private String batch;

    /** 库存量（吨） */
    @Schema(description = "库存量", example = "713.5")
    private Double inventory;

    /** 状态：normal/warning/alarm */
    @Schema(description = "状态", example = "normal")
    private String status;

    /** 更新时间 */
    @Schema(description = "更新时间", example = "2026-07-28 10:30:00")
    private String updatedAt;
}
