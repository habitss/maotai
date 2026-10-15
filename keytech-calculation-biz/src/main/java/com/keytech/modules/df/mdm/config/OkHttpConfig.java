package com.keytech.modules.df.mdm.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * OKHttp配置类
 * @author Rocye
 * @since 2025.07.16
 */
@Configuration
public class OkHttpConfig {
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                // 连接超时时间
                .connectTimeout(10, TimeUnit.SECONDS)
                // 读取超时时间
                .readTimeout(30, TimeUnit.SECONDS)
                // 写入超时时间
                .writeTimeout(30, TimeUnit.SECONDS)
                // 是否自动重连
                .retryOnConnectionFailure(true)
                .build();
    }
}