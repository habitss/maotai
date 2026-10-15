package com.keytech.modules.oauth2.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * client_secret 哈希工具类。
 * client_secret 是32字节随机高熵值（非用户密码），采用不加盐SHA-256哈希即可满足安全要求，
 * 数据库中不存储明文，避免拖库泄露。
 * @author claude
 * @since 2026.08.19
 */
public class OAuth2SecretUtil {

    public static String hashSecret(String rawSecret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawSecret.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256不可用", e);
        }
    }
}
