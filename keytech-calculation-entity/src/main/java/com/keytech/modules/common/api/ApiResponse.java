package com.keytech.modules.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 入站 API 通用响应（外部系统 E+H / 自控 → 平台 约定的格式）。
 * code:1-成功 0-失败；content:描述；data:业务数据。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Data
@Schema(description = "入站 API 通用响应")
public class ApiResponse {

    @Schema(description = "状态码 1-成功 0-失败", example = "1")
    private Integer code;

    @Schema(description = "内容/描述")
    private String content;

    @Schema(description = "业务数据")
    private Object data;

    public static ApiResponse ok(String content) {
        return ok(content, null);
    }

    public static ApiResponse ok(String content, Object data) {
        ApiResponse r = new ApiResponse();
        r.code = 1;
        r.content = content;
        r.data = data;
        return r;
    }

    public static ApiResponse fail(String content) {
        ApiResponse r = new ApiResponse();
        r.code = 0;
        r.content = content;
        return r;
    }
}
