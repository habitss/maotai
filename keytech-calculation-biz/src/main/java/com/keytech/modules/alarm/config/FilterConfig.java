package com.keytech.modules.alarm.config;

import com.keytech.modules.alarm.filter.RequestBodyCachingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置
 * @author yepeng
 * @since 2026.02.12
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RequestBodyCachingFilter> requestBodyCachingFilter() {
        FilterRegistrationBean<RequestBodyCachingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestBodyCachingFilter());
        // 只对报警授权接口应用此过滤器
        registrationBean.addUrlPatterns("/alarm/auth/*");
        // 设置过滤器顺序
        registrationBean.setOrder(1);
        return registrationBean;
    }

}
