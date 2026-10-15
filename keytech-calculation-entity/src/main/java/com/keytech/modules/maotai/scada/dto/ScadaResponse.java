package com.keytech.modules.maotai.scada.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 中集自控系统(SCADA)通用响应(与 E+H 的 EhResponse 分开)。
 * resultCode:1-成功 0-失败(平台约定;参数字典 V0.3 原标 0-成功,已按平台约定改为 1-成功);
 * rqi 回传同请求;resultDesp 描述。
 *
 * @author maotai
 * @since 2026-07-27
 */
@Data
@Schema(description = "中集自控(SCADA)通用响应")
public class ScadaResponse {

    @Schema(description = "请求流水Id(回传同请求)")
    @JsonProperty("rqi")
    private String rqi;

    @Schema(description = "状态码 1-成功 0-失败")
    @JsonProperty("resultCode")
    private Integer resultCode;

    @Schema(description = "描述")
    @JsonProperty("resultDesp")
    private String resultDesp;

    public boolean isSuccess() {
        return resultCode != null && resultCode == 1;
    }

    /** 成功:rqi 回传同请求, resultCode=1 */
    public static ScadaResponse ok(String rqi, String desp) {
        ScadaResponse r = new ScadaResponse();
        r.rqi = rqi;
        r.resultCode = 1;
        r.resultDesp = desp;
        return r;
    }

    /** 失败:rqi 回传同请求, resultCode=0 */
    public static ScadaResponse fail(String rqi, String desp) {
        ScadaResponse r = new ScadaResponse();
        r.rqi = rqi;
        r.resultCode = 0;
        r.resultDesp = desp;
        return r;
    }
}
