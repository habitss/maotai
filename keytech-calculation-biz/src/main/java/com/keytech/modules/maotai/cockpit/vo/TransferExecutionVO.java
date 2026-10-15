package com.keytech.modules.maotai.cockpit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 *
 * 今日调拨执行数据VO
 * <p>
 * 对应前端 updateTransferData 的标准结构。
 *
 * @author keytech
 * @since 2026-07-29
 */
@Data
@Accessors(chain = true)
@Schema(description = "今日调拨执行数据")
public class TransferExecutionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 入罐量（吨） */
    @Schema(description = "入罐量", example = "224.8")
    private Double inbound;

    /** 出罐量（吨） */
    @Schema(description = "出罐量", example = "203.1")
    private Double outbound;

    /** 入罐日计划（吨） */
    @Schema(description = "入罐日计划", example = "260.0")
    private Double inboundPlan;

    /** 出罐日计划（吨） */
    @Schema(description = "出罐日计划", example = "240.0")
    private Double outboundPlan;

    /** 净流入（吨） */
    @Schema(description = "净流入", example = "21.7")
    private Double net;

    /** 日计划完成率（%） */
    @Schema(description = "日计划完成率", example = "86")
    private Double completion;
}
