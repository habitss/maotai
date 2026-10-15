package com.keytech.modules.maotai.eh.ehLoading.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.modules.maotai.eh.ehLoading.config.LoadingEhProperties;
import com.keytech.modules.maotai.eh.ehLoading.dto.BusinessDetailsRequest;
import com.keytech.modules.maotai.eh.ehLoading.dto.CloseRequest;
import com.keytech.modules.maotai.eh.ehLoading.dto.EhResponse;
import com.keytech.modules.maotai.eh.ehLoading.dto.LoadingPlanDTO;
import com.keytech.modules.maotai.eh.ehLoading.dto.LoginTokenRequest;
import com.keytech.modules.maotai.eh.ehLoading.entity.LoadingPushLog;
import com.keytech.modules.maotai.eh.ehLoading.mapper.LoadingPushLogMapper;
import com.keytech.modules.maotai.eh.vehiclecheck.dto.VehicleCheckResultRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;

/**
 * E+H 装卸车系统 HTTP 客户端。
 * 统一管理 token(Redis 缓存 2h)+ 失效重试,对外暴露 createPlan / closeOrder / queryBusinessDetails / sendVehicleCheckResult。
 * 每次 Create/Close 调用都会写一条推送流水(LoadingPushLog),失败不影响主流程。
 * HTTP 走 Hutool,JSON 走 Jackson(DTO 上有 @JsonProperty)。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EhLoadingClient {

    private static final String TOKEN_CACHE_KEY = "loading:eh:token";
    private static final long TOKEN_TTL_SECONDS = 2 * 3600L - 60L;

    private final LoadingEhProperties properties;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final LoadingPushLogMapper pushLogMapper;

    /**
     * 装卸车工单同步(API04: 平台 → E+H, /api/Bussiness/Create, POST+Token,Token 由 API20 取得)。
     */
    public EhResponse createPlan(LoadingPlanDTO plan) {
        EhResponse resp = postWithToken(properties.getCreatePath(), plan);
        logPush(plan.getOrderNo(), "Create", toJson(plan), resp);
        return resp;
    }

    /**
     * 工单结束(API19: 平台 → E+H, /api/Bussiness/Close, POST+Token,Token 由 API20 取得)。
     * 请求字段(参数字典 V0.3):busiType(1-装车/2-卸车,Y)、OrderNo(Y)、CarNo(Y)、
     * Modifyor(N)、ModifyTime(N)、ABV(卸车传度数值,装车传空字符串)。结束类型由 E+H 内部判断,不传 CloseType。
     */
    public EhResponse closeOrder(CloseRequest close) {
        EhResponse resp = postWithToken(properties.getClosePath(), close);
        logPush(close.getOrderNo(), "Close", toJson(close), resp);
        return resp;
    }

    /**
     * 查询业务命令(API16: 平台 → E+H, /api/Bussiness/Details, POST+Token,Token 由 API20 取得)。
     * 查 E+H 内未归档的装卸车单据(备用/兜底)。请求:busiType/OrderNo/CarNo;响应 data 含 status 等业务信息。查询不记推送流水。
     */
    public EhResponse queryBusinessDetails(BusinessDetailsRequest req) {
        return postWithToken(properties.getDetailsPath(), req);
    }

    /** 车检结果回传(API11: 平台 → E+H /api/Bussiness/Update,ModifyType=5),passFlag 由调用方设 */
    public EhResponse sendVehicleCheckResult(VehicleCheckResultRequest req) {
        return postWithToken(properties.getVehicleCheckResultPath(), req);
    }

    /** 记推送流水,异常不抛(不影响主流程) */
    private void logPush(String orderNo, String type, String req, EhResponse resp) {
        try {
            LoadingPushLog rec = new LoadingPushLog();
            rec.setOrderNo(orderNo);
            rec.setPushType(type);
            rec.setReqBody(req);
            rec.setRespCode(resp.getCode());
            rec.setRespContent(resp.getContent());
            rec.setPushTime(new Date());
            pushLogMapper.insert(rec);
        } catch (Exception e) {
            log.warn("[推送流水] 记录失败 orderNo={} type={}", orderNo, type, e);
        }
    }

    private EhResponse postWithToken(String path, Object body) {
        String token = getToken();
        EhResponse resp = post(url(path), toJson(body), token);
        if (!resp.isSuccess() && resp.getContent() != null
                && StrUtil.containsIgnoreCase(resp.getContent(), "token")) {
            log.warn("[E+H] 疑似 token 失效,刷新重试: {}", resp.getContent());
            stringRedisTemplate.delete(TOKEN_CACHE_KEY);
            resp = post(url(path), toJson(body), getToken());
        }
        return resp;
    }

    /** 取 token:Redis 缓存命中直接返回,否则调 LoginToken 并缓存 */
    private String getToken() {
        String token = stringRedisTemplate.opsForValue().get(TOKEN_CACHE_KEY);
        if (StrUtil.isNotBlank(token)) {
            return token;
        }
        LoginTokenRequest req = new LoginTokenRequest();
        req.setUser(properties.getUser());
        req.setPassword(properties.getPassword());
        EhResponse resp = post(url(properties.getLoginPath()), toJson(req), null);
        if (!resp.isSuccess() || StrUtil.isBlank(resp.getContent())) {
            throw new JeecgBootBizTipException("获取 E+H token 失败: " + resp.getContent());
        }
        token = resp.getContent();
        stringRedisTemplate.opsForValue().set(TOKEN_CACHE_KEY, token, Duration.ofSeconds(TOKEN_TTL_SECONDS));
        return token;
    }

    private String url(String path) {
        return StrUtil.removeSuffix(properties.getBaseUrl(), "/") + path;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("序列化 E+H 请求失败: " + obj.getClass().getSimpleName(), e);
        }
    }

    private EhResponse post(String url, String json, String token) {
        int timeout = properties.getConnectTimeoutMs() + properties.getReadTimeoutMs();
        HttpRequest req = HttpRequest.post(url)
                .body(json, "application/json")
                .timeout(timeout);
        if (StrUtil.isNotBlank(token) && StrUtil.isNotBlank(properties.getTokenHeader())) {
            req.header(properties.getTokenHeader(), token);
        }
        log.info("[E+H] POST {} | body={}", url, json);
        try (HttpResponse resp = req.execute()) {
            String body = resp.body();
            log.info("[E+H] resp status={} body={}", resp.getStatus(), body);
            if (StrUtil.isBlank(body)) {
                return fail("E+H 响应为空, HTTP " + resp.getStatus());
            }
            return objectMapper.readValue(body, EhResponse.class);
        } catch (Exception e) {
            log.error("[E+H] 请求异常 url={}", url, e);
            return fail("请求 E+H 异常: " + e.getMessage());
        }
    }

    private EhResponse fail(String msg) {
        EhResponse r = new EhResponse();
        r.setCode(0);
        r.setContent(msg);
        return r;
    }
}
