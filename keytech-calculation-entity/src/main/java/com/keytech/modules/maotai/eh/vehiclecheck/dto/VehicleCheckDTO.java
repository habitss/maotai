package com.keytech.modules.maotai.eh.vehiclecheck.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 车检信息请求体(API08: E+H → 平台 /api/WorkshopInfo/Upload)。
 * 含 busiType/OrderNo/Modifyor/ModifyType/ModifyValue(为 API11 推 E+H 用)+ flag(控制是否触发 API11)。
 * 校验注解全部带中文 message(经 EhInboundValidationAdvice 拼接返回,不再漏英文长串)。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Data
@Schema(description = "车检信息请求(API08)")
public class VehicleCheckDTO {

    @Schema(description = "业务类型 1-销售(装车) 2-采购(卸车)", example = "1")
    @NotNull(message = "业务类型(busiType)不能为空")
    @JsonProperty("busiType")
    private Integer busiType;

    @Schema(description = "订单号/派车单号")
    @NotBlank(message = "订单号(OrderNo)不能为空")
    @Size(max = 50, message = "订单号(OrderNo)长度不能超过50")
    @JsonProperty("OrderNo")
    private String orderNo;

    @Schema(description = "修改人")
    @Size(max = 50, message = "修改人(Modifyor)长度不能超过50")
    @JsonProperty("Modifyor")
    private String modifyor;

    @Schema(description = "修改类型")
    @JsonProperty("ModifyType")
    private Integer modifyType;

    @Schema(description = "修改内容(车检结果) 1-通过 0-不通过")
    @JsonProperty("ModifyValue")
    private Integer modifyValue;

    @Schema(description = "车检流水号(唯一)", example = "VC001")
    @NotBlank(message = "车检流水号(checkNo)不能为空")
    @Size(max = 50, message = "车检流水号(checkNo)长度不能超过50")
    @JsonProperty("checkNo")
    private String checkNo;

    @Schema(description = "车牌号", example = "沪A12345")
    @NotBlank(message = "车牌号(carNo)不能为空")
    @Size(max = 50, message = "车牌号(carNo)长度不能超过50")
    @JsonProperty("carNo")
    private String carNo;

    @Schema(description = "IC 卡号")
    @Size(max = 50, message = "IC卡号(icCardNo)长度不能超过50")
    @JsonProperty("icCardNo")
    private String icCardNo;

    @Schema(description = "车检类型 1-入厂 2-出厂", example = "1")
    @NotNull(message = "车检类型(checkType)不能为空")
    @JsonProperty("checkType")
    private Integer checkType;

    @Schema(description = "车检时间 yyyy-MM-dd HH:mm:ss", example = "2026-07-22 09:30:00")
    @NotBlank(message = "车检时间(checkTime)不能为空")
    @Size(max = 50, message = "车检时间(checkTime)长度不能超过50")
    @JsonProperty("checkTime")
    private String checkTime;

    @Schema(description = "检查项明细数组(每项 itemName/passFlag/remark)")
    @Valid
    @JsonProperty("checkItems")
    private List<VehicleCheckItemDTO> checkItems;

    @Schema(description = "标志位 默认1-直接走API11;0-只落库不触发", example = "1")
    @JsonProperty("flag")
    private Integer flag;
}
