package com.keytech.modules.maotai.scada.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;



/**
 * 工单信息回传请求(API17: 中集自控系统 → 平台, /openapi/v1/api/WorkOrder/InfoFeedback, POST)。
 * 中集自控把工单信息回传给平台。字段对应参数字典表 V0.3 API17;2026-08-26 与中集沟定:
 * 状态字段改 <b>scadaStatus</b>(Integer,0-空闲 1-运行 7-暂停 10-异常终止 11-正常结束),
 * 与公共报文字段 status(E+H 字典)区分,中集按新字段名传。
 * 校验注解带中文 message(经 ScadaInboundValidationAdvice 拼 ScadaResponse 返回)。
 *
 * @author maotai
 * @since 2026-07-29
 */
@Data
@Schema(description = "工单信息回传请求(API17 中集自控→平台)")
public class WorkOrderInfoFeedbackRequest {

    @Schema(description = "请求流水Id")
    @NotBlank(message = "请求流水Id(rqi)不能为空")
    @Size(max = 64, message = "请求流水Id(rqi)长度不能超过64")
    @JsonProperty("rqi")
    private String rqi;

    @Schema(description = "工单号")
    @NotBlank(message = "工单号(OrderNo)不能为空")
    @Size(max = 50, message = "工单号(OrderNo)长度不能超过50")
    @JsonProperty("OrderNo")
    private String orderNo;

    @Schema(description = "中集运行状态:0-空闲 1-运行 7-暂停 10-异常终止 11-正常结束")
    @NotNull(message = "中集运行状态(scadaStatus)不能为空")
    @JsonProperty("scadaStatus")
    private Integer scadaStatus;

    @Schema(description = "回传信息JSON")
    @Size(max = 200, message = "回传信息(infoData)长度不能超过200")
    @JsonProperty("infoData")
    private String infoData;

    @Schema(description = "回传时间")
    @NotBlank(message = "回传时间(feedbackTime)不能为空")
    @Size(max = 50, message = "回传时间(feedbackTime)长度不能超过50")
    @JsonProperty("feedbackTime")
    private String feedbackTime;
}
