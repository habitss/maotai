package com.keytech.modules.maotai.mes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 工单信息回传触发入参(API07 平台内部端点 /maotai/mes/orderInfo/push)。
 * orderNo + rqi + status 必填;actualWeight 由平台从 loading_order 取;
 * execData/feedbackTime 可选(feedbackTime 为空时平台取当前时间)。
 *
 * @author maotai
 * @since 2026-08-06
 */
@Data
@Schema(description = "工单信息回传触发入参(API07)")
public class OrderInfoFeedbackCommand {

    @Schema(description = "订单号(装卸车工单)")
    @NotBlank
    private String orderNo;

    @Schema(description = "请求流水Id(响应回传同请求)")
    @NotBlank
    private String rqi;

    @Schema(description = "执行状态 1新建/3开始装卸/5装卸结束/8业务结束/4删除业务")
    @NotBlank
    private String status;

    @Schema(description = "执行数据(执行明细JSON,可选)")
    private String execData;

    @Schema(description = "回传时间(可选,为空时平台取当前时间)")
    private String feedbackTime;
}
