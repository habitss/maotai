package com.keytech.modules.kpi.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.util.DigestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

/**
 * 维护KPI获取签名
 */
public class SignatureUtil {

    /**
     * 计算签名
     *
     * @param params
     * @return
     */
    public static String getSignature(JSONObject params) {
        // 获取密钥
        String key = "BF9D3D3AW113D87";
        // 参数进行字典排序
        String sortStr = getFormatParams(params);
        // 将密钥key拼接在字典排序后的参数字符串中,得到待签名字符串。
        sortStr += "key=" + key;
        // 使用md5算法加密待加密字符串并转为大写即为sign
        // String sign = SecureUtil.md5(sortStr).toUpperCase();
        String sign = DigestUtils.md5DigestAsHex(sortStr.getBytes()).toUpperCase();
        String getSignature = null;
        try {
            getSignature = hmacSHA256(key, sign);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getSignature;
    }


    /**
     * 参数字典排序
     *
     * @param params
     * @return
     */
    public static String getFormatParams(Map<String, Object> params) {
        List<Map.Entry<String, Object>> infoIds = new ArrayList<Map.Entry<String, Object>>(params.entrySet());
        Collections.sort(infoIds, new Comparator<Map.Entry<String, Object>>() {
            public int compare(Map.Entry<String, Object> arg0, Map.Entry<String, Object> arg1) {
                return (arg0.getKey()).compareTo(arg1.getKey());
            }
        });
        String ret = "";
        for (Map.Entry<String, Object> entry : infoIds) {
            if("_nonce".equals(entry.getKey())||"_timestamp".equals(entry.getKey())) {
                ret += entry.getKey();
                ret += "=";
                ret += entry.getValue();
                ret += "&";
            }
        }
        return ret;
    }

    /**
     * sha256_HMAC加密
     *
     * @param message 消息
     * @param secret  秘钥
     * @return 加密后字符串
     */
    public static String hmacSHA256(String secret, String message) throws Exception {
        String hash = "";
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        hmacSha256.init(secret_key);
        byte[] bytes = hmacSha256.doFinal(message.getBytes());
        // hash = byteArrayToHexString(bytes);
        hash = Base64.getEncoder().encodeToString(bytes);
        return hash;
    }
}
