package com.keytech.modules.oauth2.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * OAuth2 client_id / client_secret 生成工具类
 * @author claude
 * @since 2026.08.19
 */
public class OAuth2KeyUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int SECRET_LENGTH = 32; // 32字节 = 256位

    /** 生成安全的client_secret（明文，仅创建/重置时返回一次） */
    public static String generateClientSecret() {
        byte[] bytes = new byte[SECRET_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** 生成client_id */
    public static String generateClientId(String prefix) {
        StringBuilder sb = new StringBuilder();
        if (prefix != null && !prefix.isEmpty()) {
            sb.append(prefix).append("_");
        }
        sb.append(System.currentTimeMillis()).append("_").append(SECURE_RANDOM.nextInt(10000));
        return sb.toString();
    }
}
