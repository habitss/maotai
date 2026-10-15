package com.keytech.modules.alarm.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 报警接口认证工具类
 * @author yepeng
 * @since 2026.01.29
 */
public class AlarmAuthUtil {
    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * 生成签名
     * @param appId 应用标识
     * @param timestamp 时间戳
     * @param nonce 随机字符串
     * @param requestBody 请求体
     * @param appSecret 应用密钥
     * @return 签名
     * @throws NoSuchAlgorithmException 算法不存在异常
     * @throws InvalidKeyException 无效密钥异常
     */
    public static String generateSign(String appId, String timestamp, String nonce, String requestBody, String appSecret) throws NoSuchAlgorithmException, InvalidKeyException {
        String signContent = appId + timestamp + nonce + (requestBody != null ? requestBody : "");
        return hmacSHA256(signContent, appSecret);
    }

    /**
     * 验证签名
     * @param appId 应用标识
     * @param timestamp 时间戳
     * @param nonce 随机字符串
     * @param requestBody 请求体
     * @param appSecret 应用密钥
     * @param sign 待验证的签名
     * @return 是否验证通过
     * @throws NoSuchAlgorithmException 算法不存在异常
     * @throws InvalidKeyException 无效密钥异常
     */
    public static boolean verifySign(String appId, String timestamp, String nonce, String requestBody, String appSecret, String sign) throws NoSuchAlgorithmException, InvalidKeyException {
        String generatedSign = generateSign(appId, timestamp, nonce, requestBody, appSecret);
        return generatedSign.equals(sign);
    }

    /**
     * 验证签名（支持新旧密钥）
     * @param appId 应用标识
     * @param timestamp 时间戳
     * @param nonce 随机字符串
     * @param requestBody 请求体
     * @param appSecret 当前密钥
     * @param appSecretOld 旧密钥
     * @param sign 待验证的签名
     * @return 是否验证通过
     * @throws NoSuchAlgorithmException 算法不存在异常
     * @throws InvalidKeyException 无效密钥异常
     */
    public static boolean verifySignWithOldSecret(String appId, String timestamp, String nonce, String requestBody, String appSecret, String appSecretOld, String sign) throws NoSuchAlgorithmException, InvalidKeyException {
        // 先使用当前密钥验签
        boolean match = verifySign(appId, timestamp, nonce, requestBody, appSecret, sign);
        
        // 如果当前密钥验签失败，且有旧密钥，则使用旧密钥验签
        if (!match && appSecretOld != null && !appSecretOld.isEmpty()) {
            match = verifySign(appId, timestamp, nonce, requestBody, appSecretOld, sign);
        }
        
        return match;
    }

    /**
     * HMAC-SHA256算法
     * @param data 待加密数据
     * @param key 密钥
     * @return 加密结果
     * @throws NoSuchAlgorithmException 算法不存在异常
     * @throws InvalidKeyException 无效密钥异常
     */
    private static String hmacSHA256(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
        mac.init(secretKeySpec);
        byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 验证时间戳是否在允许范围内
     * @param timestamp 时间戳（秒）
     * @param allowedMinutes 允许的分钟数
     * @return 是否在允许范围内
     */
    public static boolean isValidTimestamp(String timestamp, int allowedMinutes) {
        try {
            long currentTime = System.currentTimeMillis() / 1000;
            long requestTime = Long.parseLong(timestamp) / 1000;
            long timeDiff = Math.abs(currentTime - requestTime);
            return timeDiff <= allowedMinutes * 60;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
