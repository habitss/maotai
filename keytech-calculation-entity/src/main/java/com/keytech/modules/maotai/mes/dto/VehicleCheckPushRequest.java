package com.keytech.modules.maotai.mes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 车检信息推送请求(API09: 平台 → 勾储MES, /GZMES/notify/vehicleCheck, POST)。
 * 由平台从 mes3_maotai_vehicle_check 组装后发出(非用户直接输入)。字段对应参数字典表 V0.3 API09。
 *
 * @author maotai
 * @since 2026-07-30
 */
@Data
@Schema(description = "车检信息推送请求(API09 平台→勾储MES)")
public class VehicleCheckPushRequest {

    @Schema(description = "请求流水Id")
    @JsonProperty("rqi")
    private String rqi;

    @Schema(description = "车检流水号")
    @JsonProperty("checkNo")
    private String checkNo;

    @Schema(description = "车牌号")
    @JsonProperty("carNo")
    private String carNo;

    @Schema(description = "IC卡号")
    @JsonProperty("icCardNo")
    private String icCardNo;

    @Schema(description = "车检类型 1-入厂 2-出厂")
    @JsonProperty("checkType")
    private Integer checkType;

    @Schema(description = "车检时间")
    @JsonProperty("checkTime")
    private String checkTime;
}
