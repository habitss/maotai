package com.keytech.modules.maotai.eh.ehLoading.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * E+H 通用响应体。（出站）
 * 成功/失败均为 {@code {"code":1或0,"content":"..."}}(小写);
 * 文档失败示例里的 Code/Content 大写为笔误,以小写为准。
 * 查询业务命令(API16)的响应另带 {@code data}(业务对象,含 status 等);其余接口 data 为 null。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Data
@Schema(description = "E+H 通用响应")
public class EhResponse {

    @Schema(description = "结果码:1-成功 0-失败", example = "1")
    private Integer code;

    @Schema(description = "内容:成功时为提示/Token 字符串,失败时为失败原因")
    private String content;

    @Schema(description = "业务数据:API16 查询成功时为业务对象(含 status 等),失败/无数据时可能为空串或 null")
    private JsonNode data;

    public boolean isSuccess() {
        return code != null && code == 1;
    }
}
