package com.keytech.modules.oauth2.config;

import com.keytech.modules.oauth2.interceptor.OAuth2AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 第三方开放接口认证配置。
 * 仅拦截 /openapi/v1/** ，/oauth2/token 本身通过client_id+client_secret自证身份，无需此拦截器。
 * @author claude
 * @since 2026.08.19
 */
@Configuration
public class OAuth2Config implements WebMvcConfigurer {

    @Autowired
    private OAuth2AuthInterceptor oAuth2AuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(oAuth2AuthInterceptor)
                .addPathPatterns("/openapi/v1/**");
    }
}
