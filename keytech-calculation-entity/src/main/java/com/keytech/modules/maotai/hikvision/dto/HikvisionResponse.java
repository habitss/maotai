package com.keytech.modules.maotai.hikvision.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 海康 iSecure Center 公共响应(artemis 网关返回)。
 * 与勾储MES 的 MesResponse(resultCode 1=成功)不同:海康 code="0" 成功(字符串),其他为失败码。
 * data 用 JsonNode —— 各接口结构不同(cameras 列表 / previewURLs 单 url / online 数组),
 * client 层只判 code,由 service 层按需把 data 转成具体 VO,避免 client 耦合泛型。
 *
 * @author maotai
 * @since 2026-08-10
 */
@Data
@Schema(description = "海康 iSecure Center 公共响应")
public class HikvisionResponse {

    @Schema(description = "状态码 \"0\"=成功,其他为错误码")
    private String code;

    @Schema(description = "描述信息")
    private String msg;

    @Schema(description = "业务数据(各接口结构不同,原始 JSON)")
    private JsonNode data;

    public boolean isSuccess() {
        return "0".equals(code);
    }

    /** 成功:code="0" */
    public static HikvisionResponse ok(JsonNode data) {
        HikvisionResponse r = new HikvisionResponse();
        r.code = "0";
        r.msg = "success";
        r.data = data;
        return r;
    }

    /** 失败:code="-1"(客户端构造的失败,区别于海康返回的错误码) */
    public static HikvisionResponse fail(String msg) {
        HikvisionResponse r = new HikvisionResponse();
        r.code = "-1";
        r.msg = msg;
        return r;
    }
}
