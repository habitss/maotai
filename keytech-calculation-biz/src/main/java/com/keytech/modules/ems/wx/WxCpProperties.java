package com.keytech.modules.ems.wx;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信企业号配置
 * @author yepeng
 * @since 2025.05.23
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx.cp")
@Schema(description = "微信企业号配置")
public class WxCpProperties {
    @Schema(description = "企业ID")
    private String corpId;
    @Schema(description = "企业密钥")
    private String corpSecret;
    @Schema(description = "应用ID")
    private Integer agentId;

    // 可选项
    @Schema(description = "消息校验token")
    private String token;
    @Schema(description = "消息加解密key")
    private String aesKey;
}