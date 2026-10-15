package com.keytech.modules.maotai.cockpit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 *
 * 调拨计划数据VO（月度/年度通用）
 * <p>
 * 对应前端 updateMonthlyTransferData / updateAnnualTransferData 的标准结构。
 *
 * @author keytech
 * @since 2026-07-29
 */
@Data
@Accessors(chain = true)
@Schema(description = "调拨计划数据")
public class TransferPlanVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 完成率（%） */
    @Schema(description = "完成率", example = "92.4")
    private Double completion;

    /** 偏差量（吨，负值表示未完成） */
    @Schema(description = "偏差量", example = "-18.6")
    private Double variance;

    /** Y轴最大值（柱状图刻度） */
    @Schema(description = "Y轴最大值", example = "1000")
    private Double axisMax;

    /** 单位 */
    @Schema(description = "单位", example = "吨")
    private String unit;

    /** 各品牌调拨明细列表 */
    @Schema(description = "各品牌调拨明细")
    private List<TransferItemVO> items;
}
