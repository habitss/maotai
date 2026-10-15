package com.keytech.modules.pi.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @FileName InfluxDBProperties
 * @Description influxdb参数设置
 * @Author HQ
 * @date 2025-08-19
 **/
@Configuration
@ConfigurationProperties(prefix = "influx")
@Data
public class InfluxDBProperties {
    private String url;
    private String username;
    private String password;
    private String token;
    private String org;
    private String bucket;
    private String version;
}
