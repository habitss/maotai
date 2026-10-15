package com.keytech.modules.oauth2.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Set;

/**
 * 第三方应用身份的请求级上下文（ThreadLocal）。
 * 因OAuth2鉴权链完全绕开Shiro，业务代码不能用 SecurityUtils.getLoginUser()，
 * 需通过本类获取当前调用方（client_id/租户/scope）。
 * 用法与 TenantContext 一致：拦截器 preHandle 中 set，afterCompletion 中 clear。
 * @author claude
 * @since 2026.08.19
 */
public class OAuthClientContext {

    private static final ThreadLocal<Principal> HOLDER = new ThreadLocal<>();

    public static void set(Principal principal) {
        HOLDER.set(principal);
    }

    public static Principal get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    @Data
    @AllArgsConstructor
    public static class Principal {
        private String clientId;
        private Integer tenantId;
        private Set<String> scopes;
    }
}
