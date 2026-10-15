package com.keytech.modules.oauth2.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明访问该接口所需的OAuth2 scope，由 OAuth2AuthInterceptor 校验。
 * 可标注在方法上（优先）或类上（作为该Controller下所有接口的默认scope）。
 * @author claude
 * @since 2026.08.19
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireScope {
    String value();
}
