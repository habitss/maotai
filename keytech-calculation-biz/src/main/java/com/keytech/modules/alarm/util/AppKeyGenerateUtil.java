package com.keytech.modules.alarm.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 应用密钥生成工具类
 * @author yepeng
 * @since 2026.02.08
 */
public class AppKeyGenerateUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int SECRET_LENGTH = 32; // 32字节 = 256位

    /**
     * 生成安全的应用密钥
     * @return 应用密钥
     */
    public static String generateAppSecret() {
        byte[] bytes = new byte[SECRET_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 生成应用ID
     * @param prefix 前缀
     * @return 应用ID
     */
    public static String generateAppId(String prefix) {
        StringBuilder sb = new StringBuilder();
        if (prefix != null && !prefix.isEmpty()) {
            sb.append(prefix).append("_");
        }
        sb.append(System.currentTimeMillis());
        sb.append("_");
        sb.append(SECURE_RANDOM.nextInt(10000));
        return sb.toString();
    }

    /**
     * 生成随机字符串
     * @param length 长度
     * @return 随机字符串
     */
    public static String generateRandomString(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, length);
    }
}
