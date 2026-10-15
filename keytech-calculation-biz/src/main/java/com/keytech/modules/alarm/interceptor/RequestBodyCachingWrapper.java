package com.keytech.modules.alarm.interceptor;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 自定义请求包装类，用于缓存请求体内容
 * 解决请求体只能读取一次的问题
 * @author yepeng
 * @since 2026.02.12
 */
public class RequestBodyCachingWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public RequestBodyCachingWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 读取请求体并缓存
        body = readBody(request);
    }

    /**
     * 读取请求体内容
     */
    private byte[] readBody(HttpServletRequest request) throws IOException {
        try (InputStream inputStream = request.getInputStream()) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CachedServletInputStream(body);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    /**
     * 获取缓存的请求体内容
     */
    public String getBody() {
        return new String(body, StandardCharsets.UTF_8);
    }

    /**
     * 缓存的 ServletInputStream 实现
     */
    private static class CachedServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream byteArrayInputStream;

        public CachedServletInputStream(byte[] body) {
            this.byteArrayInputStream = new ByteArrayInputStream(body);
        }

        @Override
        public boolean isFinished() {
            return byteArrayInputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            // 不实现
        }

        @Override
        public int read() throws IOException {
            return byteArrayInputStream.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return byteArrayInputStream.read(b, off, len);
        }

        @Override
        public int read(byte[] b) throws IOException {
            return byteArrayInputStream.read(b);
        }

        @Override
        public long skip(long n) throws IOException {
            return byteArrayInputStream.skip(n);
        }

        @Override
        public int available() throws IOException {
            return byteArrayInputStream.available();
        }

        @Override
        public void close() throws IOException {
            byteArrayInputStream.close();
        }
    }

}
