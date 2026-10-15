package com.keytech.modules.maotai.mes.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 勾储MES 对接配置(API09 等;与 E+H 的 LoadingEhProperties、中集自控的 ScadaProperties 分开)。
 * 前缀 gzmes(application-*.yml 里 mes: 前缀已被平台 mes.url 占用,故用 gzmes)。
 * base-url 默认本机占位,联调改真实勾储MES 地址;鉴权 empNo+password(AES-ECB) 暂留空待补。
 *
 * @author maotai
 * @since 2026-07-30
 */
@Data
@Component
@ConfigurationProperties(prefix = "gzmes")
@Schema(description = "勾储MES 对接配置")
public class MesProperties {

    @Schema(description = "勾储MES 服务基础地址(默认本机占位,联调改真实地址)")
    private String baseUrl = "http://localhost:8080";

    @Schema(description = "车检信息推送路径(API09 /GZMES/notify/vehicleCheck)")
    private String vehicleCheckPushPath = "/GZMES/notify/vehicleCheck";

    @Schema(description = "工单结束汇总路径(API18 /GZMES/notify/orderFinish)")
    private String orderFinishPushPath = "/GZMES/notify/orderFinish";

    @Schema(description = "工单信息回传路径(API07 /GZMES/notify/orderInfo)")
    private String orderInfoPushPath = "/GZMES/notify/orderInfo";

    @Schema(description = "工单查询路径(API02 /GZMES/workOrders/list,轮询)")
    private String workOrderListQueryPath = "/GZMES/workOrders/list";

    @Schema(description = "系统标识 serialNo(API02 查询用)")
    private String serialNo = "maotai-platform";

    @Schema(description = "操作员号(鉴权用,暂留空,AES-ECB 细节待补)")
    private String empNo = "";

    @Schema(description = "密码(鉴权用,暂留空,AES-ECB 细节待补)")
    private String password = "";

    @Schema(description = "连接超时(ms)")
    private int connectTimeoutMs = 5000;

    @Schema(description = "读取超时(ms)")
    private int readTimeoutMs = 10000;
}
