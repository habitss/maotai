package com.keytech.modules.ems.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 测试环境下的WebSocket配置
 * @author yepeng
 * @since 2025.05.06
 */
@TestConfiguration
public class TestWebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return null;
    }
}