package com.keytech.modules.ems.wx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

/**
 * 微信企业号 token 服务
 * @author yepeng
 * @since 2025.05.23
 */
@Service
@RequiredArgsConstructor
public class WxCpTokenService {
    private static final String TOKEN_CACHE_KEY = "wxcp:access_token";
    private final WxCpProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取 token (Redis 缓存)
     */
    public String getToken() {
        String token = redisTemplate.opsForValue().get(TOKEN_CACHE_KEY);
        if (token != null) {
            return token;
        }

        String url = String.format("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s",
                properties.getCorpId(), properties.getCorpSecret());
        String response = restTemplate.getForObject(url, String.class);
        try {
            JsonNode json = objectMapper.readTree(response);
            if (json.get("errcode").asInt() != 0) {
                throw new JeecgBootBizTipException("获取 access_token 失败: " + json);
            }

            token = json.get("access_token").asText();
            int expiresIn = json.get("expires_in").asInt();
            redisTemplate.opsForValue().set(TOKEN_CACHE_KEY, token, Duration.ofSeconds(expiresIn - 60L));
            return token;
        } catch (Exception e) {
            throw new JeecgBootBizTipException("解析 token 响应失败", e);
        }
    }
}
