package com.keytech.modules.pi.util;

import okhttp3.OkHttpClient;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * @FileName OkHttpClientFactory
 * @Description 证书设置
 * @Author HQ
 * @date 2025-08-19
 **/
public class OkHttpClientFactory {

    public static OkHttpClient createIgnoreSslClient() throws NoSuchAlgorithmException, KeyManagementException {
        // 信任所有证书(忽略证书认证)
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[0]; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                }
        };

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        return new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                .hostnameVerifier((hostname, session) -> true) // 忽略 hostname 验证
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

}
