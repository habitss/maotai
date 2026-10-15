package com.keytech.modules.maotai.scada.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 启动工单请求(API05: 平台 → 中集自控系统, /api/WorkOrder/Start, POST)。
 * 字段对应参数字典表 V0.3 API05;rqi 为请求流水Id(来自另一系统,平台接收后原样转发,响应回传同请求)。
 * 校验注解带中文 message(内部触发端点 @Valid,经 jeecg 全局兜底也能取 message 拼中文)。
 *
 * @author maotai
 * @since 2026-07-27
 */
@Data
@Schema(description = "启动工单请求(API05 中集自控)")
public class WorkOrderStartRequest {

    @Schema(description = "请求流水Id(来自另一系统,原样转发)")
    @NotBlank(message = "请求流水Id(rqi)不能为空")
    @Size(max = 50, message = "请求流水Id(rqi)长度不能超过50")
    @JsonProperty("rqi")
    private String rqi;

    @Schema(description = "工单号(与API06 workOrderNo 同名统一)")
    @NotBlank(message = "工单号(workOrderNo)不能为空")
    @Size(max = 50, message = "工单号(workOrderNo)长度不能超过50")
    @JsonProperty("workOrderNo")
    private String workOrderNo;

    @Schema(description = "当前车载酒数量")
    @NotBlank(message = "当前车载酒数量(currentVehicleQty)不能为空")
    @Size(max = 50, message = "当前车载酒数量(currentVehicleQty)长度不能超过50")
    @JsonProperty("currentVehicleQty")
    private String currentVehicleQty;

    @Schema(description = "目标罐")
    @NotBlank(message = "目标罐(targetTankCode)不能为空")
    @Size(max = 50, message = "目标罐(targetTankCode)长度不能超过50")
    @JsonProperty("targetTankCode")
    private String targetTankCode;

    @Schema(description = "传输量设定值")
    @NotBlank(message = "传输量设定值(transferSetpoint)不能为空")
    @Size(max = 50, message = "传输量设定值(transferSetpoint)长度不能超过50")
    @JsonProperty("transferSetpoint")
    private String transferSetpoint;

    @Schema(description = "酒精度")
    @NotBlank(message = "酒精度(alcoholContent)不能为空")
    @Size(max = 50, message = "酒精度(alcoholContent)长度不能超过50")
    @JsonProperty("alcoholContent")
    private String alcoholContent;

    @Schema(description = "装酒路径")
    @NotBlank(message = "装酒路径(loadingPath)不能为空")
    @Size(max = 50, message = "装酒路径(loadingPath)长度不能超过50")
    @JsonProperty("loadingPath")
    private String loadingPath;

    @Schema(description = "源罐(卸车撬,2026-08-27 改必填)")
    @NotBlank(message = "源罐(sourceTankCode)不能为空")
    @Size(max = 50, message = "源罐(sourceTankCode)长度不能超过50")
    @JsonProperty("sourceTankCode")
    private String sourceTankCode;

    @Schema(description = "操作人员名称")
    @NotBlank(message = "操作人员名称(operatorName)不能为空")
    @Size(max = 50, message = "操作人员名称(operatorName)长度不能超过50")
    @JsonProperty("operatorName")
    private String operatorName;
}
