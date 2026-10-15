package com.keytech.modules.maotai.eh.ehLoading.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * E+H 装卸车系统对接配置。
 * base-url 默认本机,联调时改为真实 E+H 地址并填写 user/password。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Data
@Component
@ConfigurationProperties(prefix = "loading.eh")
@Schema(description = "E+H 装卸车对接配置")
public class LoadingEhProperties {

    @Schema(description = "E+H 服务基础地址(默认本机,联调改真实地址)")
    private String baseUrl = "http://localhost:8080";

    @Schema(description = "取 Token 路径")
    private String loginPath = "/api/token/LoginToken";

    @Schema(description = "装卸车工单同步路径(API04 /api/Bussiness/Create)")
    private String createPath = "/api/Bussiness/Create";

    @Schema(description = "工单结束路径(API19 /api/Bussiness/Close)")
    private String closePath = "/api/Bussiness/Close";

    @Schema(description = "查询业务命令路径(API16 /api/Bussiness/Details)")
    private String detailsPath = "/api/Bussiness/Details";

    @Schema(description = "车检结果回传路径(API11)")
    private String vehicleCheckResultPath = "/api/Bussiness/Update";

    @Schema(description = "E+H 用户名")
    private String user = "";

    @Schema(description = "E+H 密码")
    private String password = "";

    @Schema(description = "Token 放置的请求头名称")
    private String tokenHeader = "token";

    @Schema(description = "连接超时(ms)")
    private int connectTimeoutMs = 5000;

    @Schema(description = "读取超时(ms)")
    private int readTimeoutMs = 10000;

    @Schema(description = "出站推送失败最大重试次数(API04/11/19)")
    private int retryMaxAttempts = 3;

    @Schema(description = "出站重试间隔(秒)")
    private int retryIntervalSeconds = 60;

    @Schema(description = "重试扫描周期(ms,@Scheduled fixedDelay)")
    private long retryScanFixedDelayMs = 60000;

    @Schema(description = "操作日志保留天数(每天 3:17 物理清理超期行)")
    private int opLogRetentionDays = 7;
}
