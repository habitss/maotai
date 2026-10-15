package com.keytech.modules.maotai.cockpit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 *
 * 实时物料数据VO
 * <p>
 * 对应前端 updateMaterialStatusData 的标准结构。
 * 前端兼容外层字段：rows/items/list。
 *
 * @author keytech
 * @since 2026-07-29
 */
@Data
@Accessors(chain = true)
@Schema(description = "实时物料数据")
public class MaterialStatusVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 单位 */
    @Schema(description = "单位", example = "吨")
    private String unit;

    /** 物料明细行列表 */
    @Schema(description = "物料明细行列表")
    private List<MaterialStatusRowVO> rows;
}
