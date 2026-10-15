package com.keytech.modules.alarm.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.nio.charset.StandardCharsets;

/**
 * Http 工具类（优化版）
 * @author yepeng
 * @since 2026.01.26
 */
public class HttpUtil {

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int REQUEST_TIMEOUT = 3000;
    private static final int SOCKET_TIMEOUT = 5000;

    private static RequestConfig defaultConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(REQUEST_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();
    }

    /**
     * 发送 GET 请求（URL 传参）
     */
    public static String httpSendGet(String url, String token) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(defaultConfig());
        httpGet.setHeader("Accept", "application/json");
        if (StringUtils.isNotEmpty(token)) {
            httpGet.setHeader("token", token);
        }
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            return entity == null ? "" : EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("HTTP GET 请求失败，url=" + url);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 发送 POST 请求（无 body，URL 传参）
     */
    public static String httpSendPost(String url) {
        return postJson(url, null);
    }

    /**
     * 发送 JSON POST 请求
     */
    public static String postJson(String url, String json) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(defaultConfig());
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        if (json != null && !json.isEmpty()) {
            httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        }
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            return entity == null ? "" : EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("HTTP POST 请求失败，url=" + url);
            e.printStackTrace();
            return null;
        }
    }
}
