package com.keytech.modules.maotai.eh.orderfeedback.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 工单信息回传(API15: E+H → 平台,装卸实际的回传)。
 *
 * @author maotai
 * @since 2026-07-22
 */
@Data
@TableName("mes3_maotai_order_feedback")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "工单信息回传")
public class OrderFeedback extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "车牌号")
    private String carNo;

    @Schema(description = "实际装卸量 kg")
    private BigDecimal actualWeight;

    @Schema(description = "装卸开始时间")
    private String loadStartTime;

    @Schema(description = "装卸结束时间")
    private String loadEndTime;

    @Schema(description = "业务状态")
    private String status;

    @Schema(description = "回传时间")
    private String feedbackTime;
}
