package com.keytech.modules.maotai.mes.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.modules.maotai.mes.config.MesProperties;
import com.keytech.modules.maotai.mes.dto.MesResponse;
import com.keytech.modules.maotai.mes.dto.OrderInfoFeedbackRequest;
import com.keytech.modules.maotai.mes.dto.OrderFinishSummaryRequest;
import com.keytech.modules.maotai.mes.dto.VehicleCheckPushRequest;
import com.keytech.modules.maotai.mes.dto.WorkOrderListQueryRequest;
import com.keytech.modules.maotai.mes.dto.WorkOrderListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 勾储MES HTTP 客户端(与 E+H 的 EhLoadingClient、中集自控的 ScadaClient 分开,无 Token)。
 * 鉴权 empNo+password(AES-ECB) 暂未实装,留 TODO;JSON 走 Jackson(Hutool 发请求)。
 *
 * @author maotai
 * @since 2026-07-30
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MesClient {

    private final MesProperties properties;
    private final ObjectMapper objectMapper;

    /** 车检信息推送(API09: 平台 → 勾储MES /GZMES/notify/vehicleCheck) */
    public MesResponse vehicleCheckPush(VehicleCheckPushRequest req) {
        return post(properties.getVehicleCheckPushPath(), req);
    }

    /** 工单结束汇总(API18: 平台 → 勾储MES /GZMES/notify/orderFinish) */
    public MesResponse orderFinishPush(OrderFinishSummaryRequest req) {
        return post(properties.getOrderFinishPushPath(), req);
    }

    /** 工单信息回传(API07: 平台 → 勾储MES /GZMES/notify/orderInfo) */
    public MesResponse orderInfoPush(OrderInfoFeedbackRequest req) {
        return post(properties.getOrderInfoPushPath(), req);
    }

    /** 工单查询(API02: 平台 → 勾储MES /GZMES/workOrders/list)。返回带工单明细的完整响应,调用方解析 services[].properties.workOrders 落库。 */
    public WorkOrderListResponse workOrderListQuery(WorkOrderListQueryRequest req) {
        String path = properties.getWorkOrderListQueryPath();
        String url = StrUtil.removeSuffix(properties.getBaseUrl(), "/") + path;
        String json = toJson(req);
        int timeout = properties.getConnectTimeoutMs() + properties.getReadTimeoutMs();
        log.info("[GZMES] POST {} | body={}", url, json);
        try (HttpResponse resp = HttpRequest.post(url)
                .body(json, "application/json")
                .timeout(timeout)
                .execute()) {
            String respBody = resp.body();
            log.info("[GZMES] resp status={} body={}", resp.getStatus(), respBody);
            if (StrUtil.isBlank(respBody)) {
                return failFull(req.getRqi(), "GZMES 响应为空, HTTP " + resp.getStatus());
            }
            WorkOrderListResponse parsed = objectMapper.readValue(respBody, WorkOrderListResponse.class);
            // rqi 回传兜底(MES 没回填则用请求方的)
            if (parsed != null && (parsed.getRqi() == null || parsed.getRqi().isEmpty())) {
                parsed.setRqi(req.getRqi());
            }
            return parsed;
        } catch (Exception e) {
            log.error("[GZMES] 请求异常 url={}", url, e);
            return failFull(req.getRqi(), "请求 GZMES 异常: " + e.getMessage());
        }
    }

    private MesResponse post(String path, Object body) {
        // TODO 鉴权:勾储MES empNo+password(AES-ECB),细节(密钥/padding/编码、放头还是体)待补后在此加
        String url = StrUtil.removeSuffix(properties.getBaseUrl(), "/") + path;
        String json = toJson(body);
        int timeout = properties.getConnectTimeoutMs() + properties.getReadTimeoutMs();
        log.info("[GZMES] POST {} | body={}", url, json);
        try (HttpResponse resp = HttpRequest.post(url)
                .body(json, "application/json")
                .timeout(timeout)
                .execute()) {
            String respBody = resp.body();
            log.info("[GZMES] resp status={} body={}", resp.getStatus(), respBody);
            if (StrUtil.isBlank(respBody)) {
                return fail("GZMES 响应为空, HTTP " + resp.getStatus());
            }
            return objectMapper.readValue(respBody, MesResponse.class);
        } catch (Exception e) {
            log.error("[GZMES] 请求异常 url={}", url, e);
            return fail("请求 GZMES 异常: " + e.getMessage());
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("序列化 GZMES 请求失败: " + obj.getClass().getSimpleName(), e);
        }
    }

    private MesResponse fail(String msg) {
        MesResponse r = new MesResponse();
        r.setResultCode(0);
        r.setResultDesp(msg);
        return r;
    }

    /** API02 降级:空响应/异常时返回 resultCode=0 的完整响应(无 services) */
    private WorkOrderListResponse failFull(String rqi, String msg) {
        WorkOrderListResponse r = new WorkOrderListResponse();
        r.setRqi(rqi);
        r.setResultCode(0);
        r.setResultDesp(msg);
        return r;
    }
}
