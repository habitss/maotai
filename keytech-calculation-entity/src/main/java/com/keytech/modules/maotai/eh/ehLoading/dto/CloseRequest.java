package com.keytech.modules.maotai.eh.ehLoading.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 工单结束请求(API19: 平台 → E+H, /api/Bussiness/Close, POST+Token)。
 * <p>字段对应 PDF V0.5 第 4.2 节(结束业务命令):busiType(1-装车/2-卸车,Y)、OrderNo(Y)、CarNo(Y)、
 * Modifyor(Y)、ModifyTime(N,yyyy-MM-dd HH:mm:ss)、ABV(卸车订单传度数值,装车订单传空字符串)。
 * <p>CloseType 已按 4.2.2 移除——结束类型由 E+H 内部判断,不传。
 * 校验注解带中文 message,经 EhOutboundValidationAdvice 返回中文短句(出站走 /maotai/** jeecg Result)。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Data
@Schema(description = "工单结束请求(API19 平台→E+H /api/Bussiness/Close)")
public class CloseRequest {

    @Schema(description = "业务类型 1-装车 2-卸车(卸车订单需传 ABV)", example = "1")
    @NotNull(message = "业务类型(busiType)不能为空")
    @JsonProperty("busiType")
    private Integer busiType;

    @Schema(description = "订单号", example = "20240123_001")
    @NotBlank(message = "订单号(OrderNo)不能为空")
    @Size(max = 50, message = "订单号(OrderNo)长度不能超过50")
    @JsonProperty("OrderNo")
    private String orderNo;

    @Schema(description = "车牌号", example = "沪A12345")
    @NotBlank(message = "车牌号(CarNo)不能为空")
    @Size(max = 50, message = "车牌号(CarNo)长度不能超过50")
    @JsonProperty("CarNo")
    private String carNo;

    @Schema(description = "修改人", example = "mes")
    @NotBlank(message = "修改人(Modifyor)不能为空")
    @Size(max = 50, message = "修改人(Modifyor)长度不能超过50")
    @JsonProperty("Modifyor")
    private String modifyor;

    @Schema(description = "修改时间 yyyy-MM-dd HH:mm:ss", example = "2024-01-23 21:10:15")
    @Size(max = 50, message = "修改时间(ModifyTime)长度不能超过50")
    @JsonProperty("ModifyTime")
    private String modifyTime;

    @Schema(description = "酒精度数(ABV) 卸车订单传度数值,装车订单传空字符串")
    @Size(max = 50, message = "酒精度数(ABV)长度不能超过50")
    @JsonProperty("ABV")
    private String abv;
}
