package com.keytech.modules.alarm.config;

import com.keytech.modules.alarm.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 报警接口认证配置
 * @author yepeng
 * @since 2026.01.29
 */
@Configuration
public class AlarmAuthConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册认证拦截器，拦截报警相关的接口
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/alarm/auth/**")
                .excludePathPatterns("/alarm/simple/**");
    }
}
