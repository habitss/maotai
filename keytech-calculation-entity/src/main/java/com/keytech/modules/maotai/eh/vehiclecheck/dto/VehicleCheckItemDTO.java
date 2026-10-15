package com.keytech.modules.maotai.eh.vehiclecheck.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 车检项明细(checkItems 数组元素)。校验注解带中文 message。
 *
 * @author maotai
 * @since 2026-07-22
 */
@Data
@Schema(description = "车检项明细")
public class VehicleCheckItemDTO {

    @Schema(description = "检查项名称", example = "罐体")
    @NotBlank(message = "检查项名称(itemName)不能为空")
    @Size(max = 50, message = "检查项名称(itemName)长度不能超过50")
    @JsonProperty("itemName")
    private String itemName;

    @Schema(description = "是否通过 1-通过 0-不通过", example = "1")
    @NotNull(message = "检查项结果(passFlag)不能为空")
    @JsonProperty("passFlag")
    private Integer passFlag;

    @Schema(description = "备注(不通过原因等)")
    @Size(max = 200, message = "检查项备注(remark)长度不能超过200")
    @JsonProperty("remark")
    private String remark;
}
