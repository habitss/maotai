package com.keytech.modules.alarm.demo;

import com.alibaba.fastjson2.JSON;
import com.keytech.modules.alarm.dto.AlarmReceiveRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 第三方系统授权接口请求示例
 * 用于测试 AlarmReceiveController 的 auth/receive 接口
 * 
 * 使用说明：
 * 1. 首先在系统管理平台中注册应用，获取 AppId 和 AppSecret
 * 2. 将获取到的 AppId 和 AppSecret 填入下面的常量中
 * 3. 运行 main 方法，测试授权接口
 */
public class ThirdPartyAlarmAuthRequestDemo {

    // ====== 测试配置 ======
    // 注意：在生产环境中，这些值应该从配置文件或环境变量中读取
    
    // 应用标识和密钥（需要在系统管理平台中注册获取）
    private static final String APP_ID = "app_1770740448957_5635"; // 测试用固定 AppId
    private static final String APP_SECRET = "imWBDP-iCcOS4ygUqyDQ6kWQpDlTMDPjWYDULCA8Prw"; // 测试用固定 AppSecret
    
    // 接口地址
    private static final String API_URL = "http://localhost:8082/keytech-boot/alarm/auth/receive";
    
    public static void main(String[] args) {
        System.out.println("=== 第三方系统授权接口请求示例 ===");
        System.out.println("测试用密钥:");
        System.out.println("AppId: " + APP_ID);
        System.out.println("AppSecret: " + APP_SECRET);
        System.out.println();
        
        try {
            // 1. 构造请求参数
            AlarmReceiveRequest request = buildRequest();
            String requestBody = JSON.toJSONString(request);
            System.out.println("请求参数:");
            System.out.println(requestBody);
            System.out.println();
            
            // 2. 生成请求头
            Map<String, String> headers = generateHeaders(requestBody);
            System.out.println("请求头:");
            headers.forEach((key, value) -> System.out.println(key + ": " + value));
            System.out.println();
            
            // 3. 发送请求
            String response = sendRequest(API_URL, requestBody, headers);
            System.out.println("响应结果:");
            System.out.println(response);
            System.out.println();
            
        } catch (Exception e) {
            System.out.println("请求失败:");
            e.printStackTrace();
        }
    }
    
    /**
     * 构造报警接收请求参数
     */
    private static AlarmReceiveRequest buildRequest() {
        AlarmReceiveRequest request = new AlarmReceiveRequest();
        request.setSourceSystem("EMS");
        request.setRequestId("EMS-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + "-" + RandomStringUtils.randomNumeric(4));
        request.setAlarmPiCode("APAC-CN93223001-0801_AggreAlr24-16");
        request.setAlarmTime(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        request.setAlarmLevel(1);
        request.setAlarmDesc("测试报警描述");
        request.setOnDutyPerson("张三");
        request.setExtInfo("{\"upperLimit\":\"230.0\",\"lowerLimit\":\"220.0\",\"piValue\":\"235.6\"}");
        request.setTimestamp(System.currentTimeMillis());
        return request;
    }
    
    /**
     * 生成请求头
     */
    private static Map<String, String> generateHeaders(String requestBody) throws Exception {
        Map<String, String> headers = new HashMap<>();
        
        // 生成时间戳
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // 生成随机字符串
        String nonce = RandomStringUtils.randomAlphanumeric(32);
        
        // 生成签名
        String sign = generateSign(APP_ID, timestamp, nonce, requestBody, APP_SECRET);
        
        // 设置请求头
        headers.put("X-App-Id", APP_ID);
        headers.put("X-Timestamp", timestamp);
        headers.put("X-Nonce", nonce);
        headers.put("X-Sign", sign);
        headers.put("Content-Type", "application/json;charset=UTF-8");
        
        return headers;
    }
    
    /**
     * 生成签名
     * 签名算法: HMAC-SHA256(appId + timestamp + nonce + requestBody, appSecret)
     */
    private static String generateSign(String appId, String timestamp, String nonce, String requestBody, String appSecret) throws Exception {
        // 拼接签名字符串
        StringBuilder sb = new StringBuilder();
        sb.append(appId)
          .append(timestamp)
          .append(nonce)
          .append(requestBody != null ? requestBody : "");
        
        // 使用 HMAC-SHA256 算法生成签名
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signBytes = mac.doFinal(sb.toString().getBytes(StandardCharsets.UTF_8));
        
        // 将签名转换为 Base64 字符串（与服务端保持一致）
        return java.util.Base64.getEncoder().encodeToString(signBytes);
    }
    
    /**
     * 发送 HTTP 请求
     */
    private static String sendRequest(String urlStr, String requestBody, Map<String, String> headers) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // 设置请求方法
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        
        // 设置请求头
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        
        // 发送请求体
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
        
        // 读取响应
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        } catch (Exception e) {
            // 读取错误响应
            try {
                if (connection.getErrorStream() != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }
                    }
                } else {
                    response.append("Error: " + e.getMessage());
                }
            } catch (Exception ex) {
                response.append("Error reading error stream: " + ex.getMessage());
            }
        }
        
        connection.disconnect();
        return response.toString();
    }
    
    /**
     * 验证签名
     * 用于第三方系统验证接收到的回调签名
     */
    public static boolean verifySign(String appId, String timestamp, String nonce, String requestBody, String appSecret, String sign) throws Exception {
        String expectedSign = generateSign(appId, timestamp, nonce, requestBody, appSecret);
        return StringUtils.equals(expectedSign, sign);
    }

}
