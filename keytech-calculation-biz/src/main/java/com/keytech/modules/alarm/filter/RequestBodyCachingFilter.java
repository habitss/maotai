package com.keytech.modules.alarm.filter;

import com.keytech.modules.alarm.interceptor.RequestBodyCachingWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 请求体缓存过滤器
 * 用于包装请求，使请求体可以被多次读取
 * @author yepeng
 * @since 2026.02.12
 */
public class RequestBodyCachingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 只包装 HTTP 请求
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            // 使用自定义的请求包装类包装请求
            RequestBodyCachingWrapper requestWrapper = new RequestBodyCachingWrapper(httpRequest);
            // 将包装后的请求传递给后续的过滤器和控制器
            chain.doFilter(requestWrapper, response);
        } else {
            // 非 HTTP 请求，直接传递
            chain.doFilter(request, response);
        }
    }

}
