package com.keytech.modules.oauth2.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.modules.oauth2.dto.OAuth2TokenPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * OAuth2 access_token 生成与校验工具。
 * 采用不透明随机token（非JWT），签发信息存Redis，天然支持主动吊销，
 * 且与内部Shiro/JWT体系（org.jeecg.JwtUtil）完全隔离，互不影响、互不可伪造。
 * @author claude
 * @since 2026.08.19
 */
@Component
@RequiredArgsConstructor
public class OAuth2TokenUtil {

    private static final String TOKEN_KEY_PREFIX = "oauth2:token:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /** 签发token并写入Redis，返回access_token */
    public String issueToken(String clientId, Set<String> scopes, Integer tenantId, long expireSeconds) {
        String token = generateOpaqueToken();
        OAuth2TokenPayload payload = new OAuth2TokenPayload();
        payload.setClientId(clientId);
        payload.setScopes(scopes);
        payload.setTenantId(tenantId);
        try {
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + token, json, expireSeconds, TimeUnit.SECONDS);
        } catch (IOException e) {
            throw new IllegalStateException("token序列化失败", e);
        }
        return token;
    }

    /** 解析token，不存在或已过期返回null */
    public OAuth2TokenPayload parseToken(String token) {
        Object cached = redisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + token);
        if (cached == null) {
            return null;
        }
        try {
            return objectMapper.readValue(cached.toString(), OAuth2TokenPayload.class);
        } catch (IOException e) {
            return null;
        }
    }

    /** 主动吊销token（如应用被禁用时可调用） */
    public void revokeToken(String token) {
        redisTemplate.delete(TOKEN_KEY_PREFIX + token);
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
