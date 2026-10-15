package com.keytech.modules.maotai.scada.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 工单结束通知请求(API06: 中集自控系统 → 平台, /openapi/v1/api/WorkOrder/Finish, POST)。
 * 中集自控通知平台罐转罐工单结束。字段对应参数字典表 V0.3 API06。
 * 校验注解带中文 message(经 ScadaInboundValidationAdvice 拼 ScadaResponse 返回)。
 *
 * @author maotai
 * @since 2026-07-27
 */
@Data
@Schema(description = "工单结束通知请求(API06 中集自控→平台)")
public class WorkOrderFinishNotify {

    @Schema(description = "请求流水Id")
    @NotBlank(message = "请求流水Id(rqi)不能为空")
    @Size(max = 64, message = "请求流水Id(rqi)长度不能超过64")
    @JsonProperty("rqi")
    private String rqi;

    @Schema(description = "工单号(罐转罐工单)")
    @NotBlank(message = "工单号(workOrderNo)不能为空")
    @Size(max = 50, message = "工单号(workOrderNo)长度不能超过50")
    @JsonProperty("workOrderNo")
    private String workOrderNo;

    @Schema(description = "任务类型 1-5 罐间作业")
    @NotNull(message = "任务类型(taskType)不能为空")
    @JsonProperty("taskType")
    private Integer taskType;

    @Schema(description = "来源罐号")
    @Size(max = 50, message = "来源罐号(sourceTank)长度不能超过50")
    @JsonProperty("sourceTank")
    private String sourceTank;

    @Schema(description = "目标罐号")
    @Size(max = 50, message = "目标罐号(targetTank)长度不能超过50")
    @JsonProperty("targetTank")
    private String targetTank;

    @Schema(description = "目标量 kg")
    @JsonProperty("targetWeight")
    private BigDecimal targetWeight;

    @Schema(description = "产品名称")
    @Size(max = 50, message = "产品名称(productName)长度不能超过50")
    @JsonProperty("productName")
    private String productName;

    @Schema(description = "同步时间")
    @NotBlank(message = "同步时间(syncTime)不能为空")
    @Size(max = 50, message = "同步时间(syncTime)长度不能超过50")
    @JsonProperty("syncTime")
    private String syncTime;

    @Schema(description = "目标品牌")
    @Size(max = 50, message = "目标品牌(targetProduct)长度不能超过50")
    @JsonProperty("targetProduct")
    private String targetProduct;

    @Schema(description = "输送量")
    @Size(max = 50, message = "输送量(transferVol)长度不能超过50")
    @JsonProperty("transferVol")
    private String transferVol;

    @Schema(description = "是否打空源罐 1-是 0-否")
    @JsonProperty("emptySource")
    private Integer emptySource;
}
