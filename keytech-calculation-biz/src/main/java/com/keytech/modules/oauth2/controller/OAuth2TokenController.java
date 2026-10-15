package com.keytech.modules.oauth2.controller;

import com.keytech.modules.oauth2.entity.OAuth2Client;
import com.keytech.modules.oauth2.service.IOAuth2ClientService;
import com.keytech.modules.oauth2.util.OAuth2TokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 第三方OAuth2令牌端点（Client Credentials，RFC 6749 §4.4）。
 * 与Shiro登录接口（LoginController）完全独立，供第三方系统换取access_token。
 * @author claude
 * @since 2026.08.19
 */
@Tag(name = "第三方OAuth2令牌")
@RestController
@RequestMapping("/oauth2")
@Slf4j
@RequiredArgsConstructor
public class OAuth2TokenController {

    private static final long DEFAULT_EXPIRE_SECONDS = 7200L;

    private final IOAuth2ClientService oAuth2ClientService;
    private final OAuth2TokenUtil oAuth2TokenUtil;

    @Operation(summary = "第三方应用换取access_token（client_credentials）")
    @PostMapping(value = "/token", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> token(
            @RequestParam(value = "grant_type", required = false) String grantType,
            @RequestParam(value = "client_id", required = false) String bodyClientId,
            @RequestParam(value = "client_secret", required = false) String bodyClientSecret,
            @RequestParam(value = "scope", required = false) String requestedScope,
            HttpServletRequest request) {

        // 同时支持 HTTP Basic（RFC 6749 §2.3.1 标准做法）与 body 参数（兼容非标准第三方SDK）
        String clientId = bodyClientId;
        String clientSecret = bodyClientSecret;
        String[] basicCredential = extractBasicCredential(request);
        if (basicCredential != null) {
            clientId = basicCredential[0];
            clientSecret = basicCredential[1];
        }

        if (!"client_credentials".equals(grantType)) {
            return error(HttpStatus.BAD_REQUEST, "unsupported_grant_type", "仅支持 client_credentials");
        }
        if (StringUtils.isAnyEmpty(clientId, clientSecret)) {
            return error(HttpStatus.BAD_REQUEST, "invalid_request", "缺少client_id或client_secret");
        }
        // 一次查库，后面的有效性/密钥/scope校验都基于这一份实体，避免同一请求内重复查询
        OAuth2Client client = oAuth2ClientService.getByClientId(clientId);
        if (!oAuth2ClientService.isValidClient(client)) {
            return error(HttpStatus.UNAUTHORIZED, "invalid_client", "应用不存在、已禁用或已过期");
        }
        if (!oAuth2ClientService.verifySecret(client, clientSecret)) {
            log.warn("OAuth2 token请求client_secret校验失败, clientId={}", clientId);
            return error(HttpStatus.UNAUTHORIZED, "invalid_client", "client_secret错误");
        }

        // 按应用整体授权的scope集合裁剪本次签发的scope（不允许超出已授权范围）
        Set<String> authorizedScopes = oAuth2ClientService.getScopes(client);
        Set<String> grantedScopes = authorizedScopes;
        if (StringUtils.isNotEmpty(requestedScope)) {
            Set<String> requested = Arrays.stream(requestedScope.split(" "))
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toSet());
            if (!authorizedScopes.containsAll(requested)) {
                return error(HttpStatus.BAD_REQUEST, "invalid_scope", "请求的scope超出应用授权范围");
            }
            grantedScopes = requested;
        }

        String accessToken = oAuth2TokenUtil.issueToken(clientId, grantedScopes, client.getTenantId(), DEFAULT_EXPIRE_SECONDS);
        oAuth2ClientService.checkAndCleanExpiredOldSecret(client);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("access_token", accessToken);
        body.put("token_type", "Bearer");
        body.put("expires_in", DEFAULT_EXPIRE_SECONDS);
        body.put("scope", String.join(" ", grantedScopes));
        return ResponseEntity.ok(body);
    }

    private String[] extractBasicCredential(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.isEmpty(header) || !header.startsWith("Basic ")) {
            return null;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(header.substring(6).trim()), StandardCharsets.UTF_8);
            int idx = decoded.indexOf(':');
            if (idx < 0) {
                return null;
            }
            return new String[]{decoded.substring(0, idx), decoded.substring(idx + 1)};
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String error, String description) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", error);
        body.put("error_description", description);
        return ResponseEntity.status(status).body(body);
    }
}
