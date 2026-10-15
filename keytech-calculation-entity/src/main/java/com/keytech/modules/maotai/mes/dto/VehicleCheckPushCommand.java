package com.keytech.modules.maotai.mes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 车检信息推送触发入参(API09 平台内部端点 /maotai/mes/vehicleCheck/push)。
 * 只传 checkNo + rqi,业务字段由平台从 mes3_maotai_vehicle_check 组装后发给勾储MES。
 *
 * @author maotai
 * @since 2026-07-30
 */
@Data
@Schema(description = "车检信息推送触发入参(API09)")
public class VehicleCheckPushCommand {

    @Schema(description = "车检流水号")
    @NotBlank
    private String checkNo;

    @Schema(description = "请求流水Id(响应回传同请求)")
    @NotBlank
    private String rqi;
}
