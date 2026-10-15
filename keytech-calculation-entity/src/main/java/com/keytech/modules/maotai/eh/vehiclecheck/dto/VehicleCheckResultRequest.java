package com.keytech.modules.maotai.eh.vehiclecheck.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 车检结果回传请求(API11: 平台 → E+H, /api/Bussiness/Update, POST+Token)。
 * 字段对应 PDF V0.5 第 4.3 节(更新业务命令,平台侧用 ModifyType=5 推车检结果):
 * busiType(N) / OrderNo(Y) / CarNo(Y) / Modifyor(Y) / ModifyTime(N) / ModifyType(Y,固定5) / ModifyValue(Y,"1"通过/"0"或其他不通过)。
 * 校验注解带中文 message。该 DTO 也被 API08 联动(flag=1 触发 API11)内部构造使用,内部路径不走 @Valid 由构造方保证赋值。
 *
 * @author maotai
 * @since 2026-07-22
 */
@Data
@Schema(description = "车检结果回传请求(API11)")
public class VehicleCheckResultRequest {

    @Schema(description = "业务类型 1-销售(装车) 2-采购(卸车)", example = "1")
    @JsonProperty("busiType")
    private Integer busiType;

    @Schema(description = "订单号/派车单号", example = "20240123_001")
    @NotBlank(message = "订单号(OrderNo)不能为空")
    @Size(max = 50, message = "订单号(OrderNo)长度不能超过50")
    @JsonProperty("OrderNo")
    private String orderNo;

    @Schema(description = "车牌号", example = "沪A12345")
    @NotBlank(message = "车牌号(CarNo)不能为空")
    @Size(max = 50, message = "车牌号(CarNo)长度不能超过50")
    @JsonProperty("CarNo")
    private String carNo;

    @Schema(description = "修改人")
    @NotBlank(message = "修改人(Modifyor)不能为空")
    @Size(max = 50, message = "修改人(Modifyor)长度不能超过50")
    @JsonProperty("Modifyor")
    private String modifyor;

    @Schema(description = "修改时间 yyyy-MM-dd HH:mm:ss", example = "2024-01-23 19:09:12")
    @Size(max = 50, message = "修改时间(ModifyTime)长度不能超过50")
    @JsonProperty("ModifyTime")
    private String modifyTime;

    @Schema(description = "修改类型(固定 5=更新车检结果)", example = "5")
    @NotNull(message = "修改类型(ModifyType)不能为空")
    @JsonProperty("ModifyType")
    private Integer modifyType;

    @Schema(description = "修改值:\"1\"通过,\"0\"或其他不通过(ModifyType=1 时为 decimal 串)", example = "1")
    @NotBlank(message = "修改值(ModifyValue)不能为空")
    @Size(max = 50, message = "修改值(ModifyValue)长度不能超过50")
    @JsonProperty("ModifyValue")
    private String modifyValue;
}
