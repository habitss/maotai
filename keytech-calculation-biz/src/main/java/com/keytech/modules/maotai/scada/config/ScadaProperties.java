package com.keytech.modules.maotai.scada.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 中集自控系统(SCADA)对接配置(API05 等;与 E+H 的 LoadingEhProperties 分开)。
 * base-url 默认本机占位,联调改中集自控真实地址;鉴权 empNo+password(AES-ECB) 暂留空待补。
 *
 * @author maotai
 * @since 2026-07-27
 */
@Data
@Component
@ConfigurationProperties(prefix = "scada")
@Schema(description = "中集自控(SCADA)对接配置")
public class ScadaProperties {

    @Schema(description = "中集自控服务基础地址(默认本机占位,联调改真实地址)")
    private String baseUrl = "http://localhost:8080";

    @Schema(description = "启动工单路径(API05 /api/WorkOrder/Start)")
    private String workOrderStartPath = "/api/WorkOrder/Start";

    @Schema(description = "装卸车工单同步路径(API14 /api/WorkOrder/SyncLoad)")
    private String workOrderSyncLoadPath = "/api/WorkOrder/SyncLoad";

    @Schema(description = "操作员号(Basic 鉴权用户名,如 mes)")
    private String empNo = "";

    @Schema(description = "密码(Basic 鉴权密码)")
    private String password = "";

    @Schema(description = "连接超时(ms)")
    private int connectTimeoutMs = 5000;

    @Schema(description = "读取超时(ms)")
    private int readTimeoutMs = 10000;
}
