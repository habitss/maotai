package com.keytech.modules.oauth2.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.modules.oauth2.annotation.RequireScope;
import com.keytech.modules.oauth2.context.OAuthClientContext;
import com.keytech.modules.oauth2.dto.OAuth2TokenPayload;
import com.keytech.modules.oauth2.entity.OAuth2Client;
import com.keytech.modules.oauth2.service.IOAuth2ClientService;
import com.keytech.modules.oauth2.util.OAuth2TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.PrintWriter;

/**
 * 第三方开放接口（/openapi/v1/**）鉴权拦截器。
 * 与Shiro JwtFilter完全独立并行，不依赖、不影响内部用户鉴权体系。
 * @author claude
 * @since 2026.08.19
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthInterceptor implements HandlerInterceptor {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final OAuth2TokenUtil oAuth2TokenUtil;
    private final IOAuth2ClientService oAuth2ClientService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 提取Bearer token
        String header = request.getHeader(HEADER_AUTHORIZATION);
        if (StringUtils.isEmpty(header) || !header.startsWith(BEARER_PREFIX)) {
            return handleError(response, 401, "invalid_token", "缺少或格式错误的Authorization头");
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();

        // 2. 校验token（Redis中不存在即为无效或已过期）
        OAuth2TokenPayload payload = oAuth2TokenUtil.parseToken(token);
        if (payload == null) {
            return handleError(response, 401, "invalid_token", "token无效或已过期");
        }

        // 3. 复核应用状态（禁用/过期后立即生效，不必等token自然过期）
        // 一次查库，供本次请求内的有效性校验和IP白名单校验共用
        OAuth2Client client = oAuth2ClientService.getByClientId(payload.getClientId());
        if (!oAuth2ClientService.isValidClient(client)) {
            return handleError(response, 401, "invalid_token", "应用已禁用或过期");
        }

        // 4. IP白名单校验（可选）
        String ipWhitelist = client.getIpWhitelist();
        if (StringUtils.isNotEmpty(ipWhitelist) && !isIpAllowed(request, ipWhitelist)) {
            return handleError(response, 403, "access_denied", "客户端IP不在白名单内");
        }

        // 5. scope校验
        if (handler instanceof HandlerMethod handlerMethod) {
            RequireScope requireScope = handlerMethod.getMethodAnnotation(RequireScope.class);
            if (requireScope == null) {
                requireScope = handlerMethod.getBeanType().getAnnotation(RequireScope.class);
            }
            if (requireScope != null && (payload.getScopes() == null || !payload.getScopes().contains(requireScope.value()))) {
                return handleError(response, 403, "insufficient_scope", "无权限访问该接口: " + requireScope.value());
            }
        }

        // 6. 设置请求级上下文，供业务代码使用
        OAuthClientContext.set(new OAuthClientContext.Principal(payload.getClientId(), payload.getTenantId(), payload.getScopes()));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 必须清理，避免线程池复用线程导致上下文串用
        OAuthClientContext.clear();
    }

    private boolean isIpAllowed(HttpServletRequest request, String ipWhitelist) {
        String remoteIp = request.getRemoteAddr();
        for (String ip : ipWhitelist.split(",")) {
            if (ip.trim().equals(remoteIp)) {
                return true;
            }
        }
        return false;
    }

    private boolean handleError(HttpServletResponse response, int httpStatus, String errorCode, String message) throws Exception {
        response.setStatus(httpStatus);
        response.setContentType("application/json;charset=UTF-8");
        // 注意：org.jeecg.common.api.vo.Result 没有 error(String, String) 重载，
        // Result.error(errorCode, message) 会被 Java 匹配到 error(String msg, T data)（泛型重载），
        // 导致 errorCode 被塞进 message、message 被塞进 result、code 变成该重载内部的默认值 500。
        // 这里改用 error(int code, String msg)，code 用真实 HTTP 状态码，errorCode 拼进 message 里。
        Result<?> result = Result.error(httpStatus, "[" + errorCode + "] " + message);
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(result));
        writer.flush();
        writer.close();
        return false;
    }
}
