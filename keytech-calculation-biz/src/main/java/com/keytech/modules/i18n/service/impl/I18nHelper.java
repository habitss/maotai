package com.keytech.modules.i18n.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.text.MessageFormat;

/**
 * 国际化帮助类 用于获取国际化文本
 * 仅针对 HTTP 请求有效 因为要从HTTP请求中获取语言
 * @author yepeng
 * @since 2025.12.23
 */
@Component
public class I18nHelper {
    private static StringRedisTemplate redisTemplate;
    private static final String DEFAULT_LANG = "zh-CN";
    private static final String KEY_PREFIX = "i18n:lang:";

    @Autowired
    public void setRedisTemplate(StringRedisTemplate template) {
        I18nHelper.redisTemplate = template;
    }

    /**
     * 获取国际化文本
     */
    public static String get(String key, Object... args) {
        if (!StringUtils.hasText(key)) {
            return "";
        }
        String lang = getLang();
        String redisKey = KEY_PREFIX + lang;
        String template = (String) redisTemplate.opsForHash().get(redisKey, key);
        if (!StringUtils.hasText(template)) {
            // 兜底：直接返回 key
            return key;
        }
        return format(template, args);
    }

    /**
     * 语言解析
     */
    private static String getLang() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return DEFAULT_LANG;
            }
            String lang = attrs.getRequest().getHeader("Accept-Language");
            return StringUtils.hasText(lang) ? lang : DEFAULT_LANG;
        } catch (Exception e) {
            return DEFAULT_LANG;
        }
    }

    /**
     * 参数格式化
     */
    private static String format(String template, Object... args) {
        try {
            return MessageFormat.format(template, args);
        } catch (Exception e) {
            return template;
        }
    }
}
