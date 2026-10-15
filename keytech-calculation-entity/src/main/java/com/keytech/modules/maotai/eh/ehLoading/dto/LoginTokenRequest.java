package com.keytech.modules.maotai.eh.ehLoading.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * E+H 取得 Token 请求体(/api/token/LoginToken)。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Data
@Schema(description = "E+H 登录取 Token 请求")
public class LoginTokenRequest {

    @Schema(description = "用户名", example = "Test")
    @NotBlank
    @JsonProperty("user")
    private String user;

    @Schema(description = "密码", example = "Test_Test123456!")
    @NotBlank
    @JsonProperty("password")
    private String password;
}
