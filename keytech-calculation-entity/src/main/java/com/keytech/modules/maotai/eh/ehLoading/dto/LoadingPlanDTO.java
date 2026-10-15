package com.keytech.modules.maotai.eh.ehLoading.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 装卸车工单同步请求体(API04: 平台 → E+H, /api/Bussiness/Create, POST+Token)。
 * 字段对应 PDF V0.5 第 4.1 节(接收装卸车计划),JSON 字段名严格保留原大小写。
 *
 * 必填字段(共 9 个):
 *   OrderNo、CarNo、PreWeight、ProductName、Validate、DriverName、Identity、CarrierName、CustomerName
 * 校验注解带中文 message——出站走 /maotai/** jeecg Result 响应,jeecg 全局兜底会取注解 message 拼 msg,
 * 前端能看到"xxx不能为空"短句而不是英文长串。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Data
@Schema(description = "装卸车计划(推送 E+H)")
public class LoadingPlanDTO {

    @Schema(description = "业务类型 1-销售(出库/装车) 2-采购(入库/卸车)", example = "1")
    @JsonProperty("busiType")
    private Integer busiType;

    @Schema(description = "订单号/派车单号,唯一", example = "20240123_001")
    @NotBlank(message = "订单号(OrderNo)不能为空")
    @Size(max = 50, message = "订单号(OrderNo)长度不能超过50")
    @JsonProperty("OrderNo")
    private String orderNo;

    @Schema(description = "车牌号", example = "沪A12345")
    @NotBlank(message = "车牌号(CarNo)不能为空")
    @Size(max = 50, message = "车牌号(CarNo)长度不能超过50")
    @JsonProperty("CarNo")
    private String carNo;

    @Schema(description = "定量(预装/预卸量),仅数值", example = "34000")
    @NotNull(message = "定量(PreWeight)不能为空")
    @JsonProperty("PreWeight")
    private BigDecimal preWeight;

    @Schema(description = "单位 kg 或 t,默认 kg", example = "kg")
    @Size(max = 50, message = "单位(Unit)长度不能超过50")
    @JsonProperty("Unit")
    private String unit;

    @Schema(description = "产品名称(E+H 系统内全字段匹配)", example = "乙二醇")
    @NotBlank(message = "产品名称(ProductName)不能为空")
    @Size(max = 50, message = "产品名称(ProductName)长度不能超过50")
    @JsonProperty("ProductName")
    private String productName;

    @Schema(description = "来源罐号(装车工单必填,卸车不传;动态校验见 validateOrder)", example = "TK-101")
    @Size(max = 50, message = "来源罐号(TankNameBefore)长度不能超过50")
    @JsonProperty("TankNameBefore")
    private String tankNameBefore;

    @Schema(description = "预目标罐号(卸车工单必填,装车不传;动态校验见 validateOrder)", example = "TK-102")
    @Size(max = 50, message = "预目标罐号(TankName)长度不能超过50")
    @JsonProperty("TankName")
    private String tankName;

    @Schema(description = "计划启动时间 yyyy-MM-dd HH:mm:ss", example = "2024-01-23 13:02:35")
    @Size(max = 50, message = "计划启动时间(ValidateBeginDate)长度不能超过50")
    @JsonProperty("ValidateBeginDate")
    private String validateBeginDate;

    @Schema(description = "工单过期时间 yyyy-MM-dd HH:mm:ss(需在此时间前刷卡)", example = "2024-01-23 23:59:59")
    @Size(max = 50, message = "工单过期时间(ValidateEndDate)长度不能超过50")
    @JsonProperty("ValidateEndDate")
    private String validateEndDate;

    @Schema(description = "IC 卡号,必填无则 \"0\",范围 0–4294967295", example = "0")
    @NotBlank(message = "IC卡号(Validate)不能为空,无卡请传\"0\"")
    @Size(max = 50, message = "IC卡号(Validate)长度不能超过50")
    @JsonProperty("Validate")
    private String validate;

    @Schema(description = "创建人", example = "创建人")
    @Size(max = 50, message = "创建人(UserName)长度不能超过50")
    @JsonProperty("UserName")
    private String userName;

    @Schema(description = "创建时间 yyyy-MM-dd HH:mm:ss", example = "2024-01-23 13:02:35")
    @Size(max = 50, message = "创建时间(BeginDate)长度不能超过50")
    @JsonProperty("BeginDate")
    private String beginDate;

    @Schema(description = "司机姓名", example = "王驾驶")
    @NotBlank(message = "司机姓名(DriverName)不能为空")
    @Size(max = 50, message = "司机姓名(DriverName)长度不能超过50")
    @JsonProperty("DriverName")
    private String driverName;

    @Schema(description = "司机身份证号/驾照号", example = "310445678567899999")
    @NotBlank(message = "司机身份证号(Identity)不能为空")
    @Size(max = 50, message = "司机身份证号(Identity)长度不能超过50")
    @JsonProperty("Identity")
    private String identity;

    @Schema(description = "司机手机号", example = "15545601123")
    @Size(max = 50, message = "司机手机号(Mobile)长度不能超过50")
    @JsonProperty("Mobile")
    private String mobile;

    @Schema(description = "批次/勾兑批次号", example = "酒的批次号")
    @Size(max = 50, message = "批次号(extendBlendBatchNo)长度不能超过50")
    @JsonProperty("extendBlendBatchNo")
    private String extendBlendBatchNo;

    @Schema(description = "产品代码,无则空串")
    @Size(max = 50, message = "产品代码(ProductCode)长度不能超过50")
    @JsonProperty("ProductCode")
    private String productCode;

    @Schema(description = "鹤位号,无则空串")
    @Size(max = 50, message = "鹤位号(BatchNo)长度不能超过50")
    @JsonProperty("BatchNo")
    private String batchNo;

    @Schema(description = "承运公司名称,无则 \"承运公司\"", example = "客户公司")
    @NotBlank(message = "承运公司名称(CarrierName)不能为空")
    @Size(max = 50, message = "承运公司名称(CarrierName)长度不能超过50")
    @JsonProperty("CarrierName")
    private String carrierName;

    @Schema(description = "承运商代码")
    @Size(max = 50, message = "承运商代码(CarrierCode)长度不能超过50")
    @JsonProperty("CarrierCode")
    private String carrierCode;

    @Schema(description = "客户名称,无则 \"客户名称\"", example = "客户公司")
    @NotBlank(message = "客户名称(CustomerName)不能为空")
    @Size(max = 50, message = "客户名称(CustomerName)长度不能超过50")
    @JsonProperty("CustomerName")
    private String customerName;

    @Schema(description = "客户代码")
    @Size(max = 50, message = "客户代码(CustomerCode)长度不能超过50")
    @JsonProperty("CustomerCode")
    private String customerCode;

    @Schema(description = "铅封号,无则空串")
    @Size(max = 50, message = "铅封号(SealNo)长度不能超过50")
    @JsonProperty("SealNo")
    private String sealNo;

    @Schema(description = "标密,无则空串")
    @Size(max = 50, message = "标密(Density)长度不能超过50")
    @JsonProperty("Density")
    private String density;

    @Schema(description = "酒精度数(Alcohol by volume)")
    @Size(max = 50, message = "酒精度数(ABV)长度不能超过50")
    @JsonProperty("ABV")
    private String abv;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注(Remark)长度不能超过500")
    @JsonProperty("Remark")
    private String remark;
}
