package com.keytech.modules.alarm.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.modules.alarm.entity.AlarmApiApp;
import com.keytech.modules.alarm.service.IAlarmApiAppService;
import com.keytech.modules.alarm.service.IAlarmApiPermissionService;
import com.keytech.modules.alarm.util.AlarmAuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * 报警接口认证拦截器
 * @author yepeng
 * @since 2026.01.29
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private static final String HEADER_APP_ID = "X-App-Id";
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEADER_NONCE = "X-Nonce";
    private static final String HEADER_SIGN = "X-Sign";
    private static final int ALLOWED_TIME_DIFF_MINUTES = 5;
    private static final String NONCE_PREFIX = "alarm:nonce:";

    private final IAlarmApiAppService alarmApiAppService;
    private final IAlarmApiPermissionService alarmApiPermissionService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 预处理拦截器
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理程序
     * @return 是否继续处理
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 读取请求头
        String appId = request.getHeader(HEADER_APP_ID);
        String timestamp = request.getHeader(HEADER_TIMESTAMP);
        String nonce = request.getHeader(HEADER_NONCE);
        String sign = request.getHeader(HEADER_SIGN);

        // 2. 验证请求头是否完整
        if (StringUtils.isAnyEmpty(appId, timestamp, nonce, sign)) {
            return handleError(response, 401, "40101", "请求头不完整");
        }

        // 3. 验证appId是否存在、状态是否可用
        AlarmApiApp app = alarmApiAppService.getByAppId(appId);
        if (app == null) {
            return handleError(response, 401, "40101", "appId不存在");
        }
        if (app.getStatus() != 1) {
            return handleError(response, 401, "40102", "app已停用");
        }

        // 4. 验证时间戳是否在允许范围
        if (!AlarmAuthUtil.isValidTimestamp(timestamp, ALLOWED_TIME_DIFF_MINUTES)) {
            return handleError(response, 401, "40104", "请求过期");
        }

        // 5. 验证nonce 是否已使用（防重放）
        if (!checkNonce(nonce)) {
            return handleError(response, 401, "40105", "重放请求");
        }

        // 6. 读取请求体（现在请求已经被过滤器包装，可以安全地读取）
        String requestBody = "";
        if (request instanceof RequestBodyCachingWrapper) {
            // 如果是我们的包装类，直接使用 getBody() 方法
            requestBody = ((RequestBodyCachingWrapper) request).getBody();
        } else {
            // 否则，使用传统方式读取
            try {
                StringBuilder sb = new StringBuilder();
                java.io.BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                requestBody = sb.toString();
            } catch (IOException e) {
                // 忽略异常
            }
        }

        // 7. 验证签名
        try {
            // 使用新旧密钥同时验签
            if (!AlarmAuthUtil.verifySignWithOldSecret(appId, timestamp, nonce, requestBody, app.getAppSecret(), app.getAppSecretOld(), sign)) {
                return handleError(response, 401, "40103", "签名错误");
            }

            // 自动清理过期的旧密钥
            alarmApiAppService.checkAndCleanExpiredOldSecret(app);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return handleError(response, 500, "50000", "签名验证失败");
        }

        // 8. 验证接口权限
        String apiCode = getApiCode(request);
        if (!alarmApiPermissionService.hasPermission(appId, apiCode)) {
            return handleError(response, 403, "40301", "无接口权限");
        }

        // 9. 放行请求
        return true;
    }



    /**
     * 检查nonce是否已使用
     * @param nonce 随机字符串
     * @return 是否可用
     */
    private boolean checkNonce(String nonce) {
        String key = NONCE_PREFIX + nonce;
        Boolean exists = redisTemplate.hasKey(key);
        if (exists != null && exists) {
            return false;
        }
        // 缓存nonce，过期时间为10分钟
        redisTemplate.opsForValue().set(key, "1", 10, TimeUnit.MINUTES);
        return true;
    }

    /**
     * 获取接口编码
     * @param request 请求
     * @return 接口编码
     */
    private String getApiCode(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        // 简单实现，实际项目中可能需要更复杂的映射规则
        return method + ":" + uri;
    }

    /**
     * 处理错误
     * @param response 响应
     * @param httpStatus HTTP状态码
     * @param code 错误码
     * @param message 错误信息
     * @return 是否继续处理
     * @throws IOException IO异常
     */
    private boolean handleError(HttpServletResponse response, int httpStatus, String code, String message) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(code, message);
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(result));
        writer.flush();
        writer.close();
        return false;
    }
}
