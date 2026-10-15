package com.keytech.modules.maotai.hikvision.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 海康 iSecure Center 对接配置(监控中心 视频取流)。
 * 前缀 hikvision;host/appKey/appSecret 联调时填现场运管中心→artemis组件→API管理 创建的合作方秘钥。
 * 与 E+H(loading.eh)/中集自控(scada)/勾储MES(gzmes) 分开,独立体系。
 *
 * @author maotai
 * @since 2026-08-10
 */
@Data
@Component
@ConfigurationProperties(prefix = "hikvision")
@Schema(description = "海康 iSecure Center 对接配置")
public class HikvisionProperties {

    @Schema(description = "平台地址 IP:Port(https 走平台 nginx 端口;http 走 artemis 默认 9016)")
    private String host = "";

    @Schema(description = "协议 https 或 http")
    private String scheme = "https";

    @Schema(description = "合作方 Key")
    private String appKey = "";

    @Schema(description = "合作方 Secret")
    private String appSecret = "";

    @Schema(description = "码流类型 0主码流 1子码流 2第三码流")
    private int streamType = 0;

    @Schema(description = "取流协议 hik/rtsp/rtmp/hls/ws(监控中心 H5 无插件用 ws)")
    private String streamProtocol = "ws";

    @Schema(description = "传输协议 0 UDP 1 TCP")
    private int transmode = 1;

    @Schema(description = "连接超时(ms,artemis SDK 当前用默认 10s,本配置预留)")
    private int connectTimeoutMs = 5000;

    @Schema(description = "读取超时(ms,artemis SDK 当前用默认 10s,本配置预留)")
    private int readTimeoutMs = 10000;
}
