package com.keytech.modules.config;

import org.jeecg.config.shiro.ShiroRealm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 注册自己的MesShiroRealm，替换核心框架的shiroRealm
 */
@Configuration
public class MesShiroConfig {
    @Bean
    public ShiroRealm shiroRealm() {
        return new MesShiroRealm(); // 注册自定义的MesShiroRealm
    }
}
