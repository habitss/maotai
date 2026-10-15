package com.keytech.modules.maotai.scada.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.modules.maotai.scada.config.ScadaProperties;
import com.keytech.modules.maotai.scada.dto.ScadaResponse;
import com.keytech.modules.maotai.scada.dto.WorkOrderStartRequest;
import com.keytech.modules.maotai.scada.dto.WorkOrderSyncLoadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 中集自控系统(SCADA) HTTP 客户端(与 E+H 的 EhLoadingClient 分开,无 Token)。
 * 鉴权 HTTP Basic(Authorization: Basic base64(empNo:password),2026-08-26 实装);
 * JSON 走 Jackson(Hutool 发请求)。
 *
 * @author maotai
 * @since 2026-07-27
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScadaClient {

    private final ScadaProperties properties;
    private final ObjectMapper objectMapper;

    /** 启动工单(API05: 平台 → 中集自控 /api/WorkOrder/Start) */
    public ScadaResponse startWorkOrder(WorkOrderStartRequest req) {
        return post(properties.getWorkOrderStartPath(), req);
    }

    /** 装卸车工单同步(API14: 平台 → 中集自控 /api/WorkOrder/SyncLoad) */
    public ScadaResponse syncLoad(WorkOrderSyncLoadRequest req) {
        return post(properties.getWorkOrderSyncLoadPath(), req);
    }

    private ScadaResponse post(String path, Object body) {
        // 鉴权:中集自控 HTTP Basic(2026-08-26 实装,原 AES-ECB 方案作废)
        String url = StrUtil.removeSuffix(properties.getBaseUrl(), "/") + path;
        String json = toJson(body);
        int timeout = properties.getConnectTimeoutMs() + properties.getReadTimeoutMs();
        log.info("[SCADA] POST {} | body={}", url, json);
        try (HttpResponse resp = HttpRequest.post(url)
                .body(json, "application/json")
                .header("Authorization", basicAuth())
                .timeout(timeout)
                .execute()) {
            String respBody = resp.body();
            log.info("[SCADA] resp status={} body={}", resp.getStatus(), respBody);
            if (StrUtil.isBlank(respBody)) {
                return fail("SCADA 响应为空, HTTP " + resp.getStatus());
            }
            // 中集响应体带 UTF-8 BOM(EF BB BF,实测 2026-08-26),Jackson 遇 ﻿ 直接抛异常——先剥掉再解析
            String cleaned = StrUtil.removePrefix(respBody, "﻿");
            return objectMapper.readValue(cleaned, ScadaResponse.class);
        } catch (Exception e) {
            log.error("[SCADA] 请求异常 url={}", url, e);
            return fail("请求 SCADA 异常: " + e.getMessage());
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("序列化 SCADA 请求失败: " + obj.getClass().getSimpleName(), e);
        }
    }

    /** HTTP Basic 头:Base64(empNo:password);未配置时返回 null 不带该头 */
    private String basicAuth() {
        if (StrUtil.isBlank(properties.getEmpNo())) {
            return null;
        }
        String raw = properties.getEmpNo() + ":" + properties.getPassword();
        return "Basic " + java.util.Base64.getEncoder().encodeToString(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private ScadaResponse fail(String msg) {
        ScadaResponse r = new ScadaResponse();
        r.setResultCode(0);
        r.setResultDesp(msg);
        return r;
    }
}
