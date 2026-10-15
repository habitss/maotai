package com.keytech.modules.maotai.mes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 工单信息回传请求(API07: 平台 → 勾储MES, /GZMES/notify/orderInfo, POST)。
 * 由平台从 loading_order 组装后发出(非用户直接输入)。字段对应参数字典表 V0.3 API07。
 * 全字段 camelCase(MES 规格)。
 *
 * @author maotai
 * @since 2026-08-06
 */
@Data
@Schema(description = "工单信息回传请求(API07 平台→勾储MES)")
public class OrderInfoFeedbackRequest {

    @Schema(description = "请求流水Id")
    @JsonProperty("rqi")
    private String rqi;

    @Schema(description = "工单号(与下发一致)")
    @JsonProperty("orderId")
    private String orderId;

    @Schema(description = "执行状态 1新建/3开始装卸/5装卸结束/8业务结束/4删除业务")
    @JsonProperty("status")
    private String status;

    @Schema(description = "实际量 kg")
    @JsonProperty("actualWeight")
    private BigDecimal actualWeight;

    @Schema(description = "执行数据(执行明细JSON)")
    @JsonProperty("execData")
    private String execData;

    @Schema(description = "回传时间")
    @JsonProperty("feedbackTime")
    private String feedbackTime;
}
