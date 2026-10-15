package com.keytech.modules.maotai.mes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 车检反馈请求(API10: 勾储MES → 平台, /api/VehicleCheck/Result, POST)。
 * MES 把车检结果反馈给平台。字段对应参数字典表 V0.3 API10。
 *
 * @author maotai
 * @since 2026-07-31
 */
@Data
@Schema(description = "车检反馈请求(API10 勾储MES→平台)")
public class VehicleCheckFeedbackRequest {

    @Schema(description = "请求流水Id")
    @NotBlank
    @Size(max = 64)
    @JsonProperty("rqi")
    private String rqi;

    @Schema(description = "车检流水号")
    @NotBlank
    @Size(max = 50)
    @JsonProperty("checkNo")
    private String checkNo;

    @Schema(description = "车牌号")
    @NotBlank
    @Size(max = 50)
    @JsonProperty("carNo")
    private String carNo;

    @Schema(description = "是否通过 1-通过 0-不通过")
    @NotNull
    @JsonProperty("passFlag")
    private Integer passFlag;

    @Schema(description = "不通过原因")
    @Size(max = 500)
    @JsonProperty("passReason")
    private String passReason;

    @Schema(description = "允许装卸量 kg")
    @JsonProperty("allowWeight")
    private BigDecimal allowWeight;
}
