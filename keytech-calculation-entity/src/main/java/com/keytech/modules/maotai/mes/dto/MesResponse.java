package com.keytech.modules.maotai.mes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 勾储MES 通用响应(与中集自控的 ScadaResponse、E+H 的 EhResponse 分开)。
 * resultCode:1-成功 0-失败(参数字典 V0.3 API09 + 枚举字典已统一);rqi 回传同请求;resultDesp 描述。
 *
 * @author maotai
 * @since 2026-07-30
 */
@Data
@Schema(description = "勾储MES 通用响应")
public class MesResponse {

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
    public static MesResponse ok(String rqi, String desp) {
        MesResponse r = new MesResponse();
        r.rqi = rqi;
        r.resultCode = 1;
        r.resultDesp = desp;
        return r;
    }

    /** 失败:rqi 回传同请求, resultCode=0 */
    public static MesResponse fail(String rqi, String desp) {
        MesResponse r = new MesResponse();
        r.rqi = rqi;
        r.resultCode = 0;
        r.resultDesp = desp;
        return r;
    }
}
