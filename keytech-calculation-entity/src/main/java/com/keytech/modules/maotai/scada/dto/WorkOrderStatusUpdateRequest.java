package com.keytech.modules.maotai.scada.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 工单状态更新请求(API13: 中集自控系统 → 平台, /openapi/v1/api/WorkOrder/StatusUpdate, POST)。
 * 中集自控把工单状态(1新建 3开始 5结束 8业务结束)推送给平台。字段对应参数字典表 V0.3 API13。
 * 校验注解带中文 message(经 ScadaInboundValidationAdvice 拼 ScadaResponse 返回)。
 *
 * @author maotai
 * @since 2026-07-28
 */
@Data
@Schema(description = "工单状态更新请求(API13 中集自控→平台)")
public class WorkOrderStatusUpdateRequest {

    @Schema(description = "请求流水Id(响应回传同请求)")
    @NotBlank(message = "请求流水Id(rqi)不能为空")
    @Size(max = 64, message = "请求流水Id(rqi)长度不能超过64")
    @JsonProperty("rqi")
    private String rqi;

    @Schema(description = "工单号")
    @NotBlank(message = "工单号(OrderNo)不能为空")
    @Size(max = 50, message = "工单号(OrderNo)长度不能超过50")
    @JsonProperty("OrderNo")
    private String orderNo;

    @Schema(description = "工单状态 1-新建 3-开始 5-结束 8-业务结束")
    @NotBlank(message = "工单状态(status)不能为空")
    @Size(max = 50, message = "工单状态(status)长度不能超过50")
    @JsonProperty("status")
    private String status;

    @Schema(description = "鹤位")
    @Size(max = 50, message = "鹤位(batchNo)长度不能超过50")
    @JsonProperty("batchNo")
    private String batchNo;

    @Schema(description = "更新时间")
    @NotBlank(message = "更新时间(updateTime)不能为空")
    @Size(max = 50, message = "更新时间(updateTime)长度不能超过50")
    @JsonProperty("updateTime")
    private String updateTime;
}
