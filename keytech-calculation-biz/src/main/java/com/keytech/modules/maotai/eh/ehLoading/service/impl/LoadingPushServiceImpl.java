package com.keytech.modules.maotai.eh.ehLoading.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.modules.maotai.eh.ehLoading.client.EhLoadingClient;
import com.keytech.modules.maotai.eh.ehLoading.dto.EhResponse;
import com.keytech.modules.maotai.eh.ehLoading.dto.LoadingPlanDTO;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingCloseService;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingOrderService;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingPushService;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * 装卸车计划推送便捷入口(API04 出站,直接吃 DTO 推 E+H;生产路径走 LoadingOrderService.pushOrder)。
 * 出站容错:超时+异常降级返回 fail,不挂主流程;推送流水由 EhLoadingClient 写 loading_push_log。
 * token 管理、失效重试已下沉到 EhLoadingClient;操作日志+失败重试在本层挂(op_log)。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Service
@Primary
@RequiredArgsConstructor
public class LoadingPushServiceImpl implements LoadingPushService {

    private static final String API04_CREATE = "API04";

    private final EhLoadingClient client;
    private final ApiOpLogService opLogService;
    private final LoadingOrderService loadingOrderService;
    private final ObjectMapper objectMapper;

    @Override
    public EhResponse pushPlan(LoadingPlanDTO plan) {
        String reqBody = toJson(plan);
        String operator = loadingOrderService.currentModifyor();
        EhResponse resp;
        try {
            resp = client.createPlan(plan);
        } catch (Exception e) {
            opLogService.recordPendingRetry(operator, API04_CREATE, plan.getOrderNo(), null, reqBody, "推送异常: " + e.getMessage());
            throw e;
        }
        if (resp.isSuccess()) {
            opLogService.recordOutbound(operator, API04_CREATE, plan.getOrderNo(), null, reqBody, resp != null && resp.isSuccess(), resp == null ? null : resp.getContent());
        } else {
            opLogService.recordPendingRetry(operator, API04_CREATE, plan.getOrderNo(), null, reqBody, resp.getContent());
        }
        return resp;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }
}
