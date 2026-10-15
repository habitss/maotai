package com.keytech.modules.pi.config;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author f'l
 * @date 2023/4/7 9:23
 */
@Data
@Configuration
public class InfluxdbConfig {

    @Value("${influx.url}")
    private String influxDBUrl;

    @Value("${influx.token}")
    private String token;

    @Value("${influx.org}")
    private String org;

    @Value("${influx.bucket}")
    private String bucket;

    @Value("${influx.username}")
    private String username;

    @Value("${influx.password}")
    private String password;


    @Bean
    public InfluxDBClient influxDBClient() {
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(influxDBUrl, token.toCharArray(), org, bucket);
//        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(influxDBUrl,username,password.toCharArray());
        influxDBClient.setLogLevel(LogLevel.BASIC);
        return influxDBClient;
    }
}
