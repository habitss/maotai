package com.keytech.modules.maotai.eh.ehLoading.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.modules.maotai.eh.ehLoading.client.EhLoadingClient;
import com.keytech.modules.maotai.eh.ehLoading.dto.CloseRequest;
import com.keytech.modules.maotai.eh.ehLoading.dto.EhResponse;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingCloseService;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingOrderService;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 工单结束推送实现(API19 出站)。出站容错:超时+异常降级返回 fail,不挂主流程;推送流水由 EhLoadingClient 写 loading_push_log。
 * token 管理/失效重试已下沉到 EhLoadingClient;操作日志+失败重试在本层挂(op_log)。
 *
 * @author maotai
 * @since 2026-07-23
 */
@Service
@RequiredArgsConstructor
public class LoadingCloseServiceImpl implements LoadingCloseService {

    private static final String API19_CLOSE = "API19";

    private final EhLoadingClient client;
    private final ApiOpLogService opLogService;
    private final LoadingOrderService loadingOrderService;
    private final ObjectMapper objectMapper;

    @Override
    public EhResponse closeOrder(CloseRequest close) {
        String reqBody = toJson(close);
        String operator = loadingOrderService.currentModifyor();
        EhResponse resp;
        try {
            resp = client.closeOrder(close);
        } catch (Exception e) {
            opLogService.recordPendingRetry(operator, API19_CLOSE, close.getOrderNo(), null, reqBody, "结束工单异常: " + e.getMessage());
            throw e;
        }
        if (resp.isSuccess()) {
            opLogService.recordOutbound(operator, API19_CLOSE, close.getOrderNo(), null, reqBody, resp != null && resp.isSuccess(), resp == null ? null : resp.getContent());
        } else {
            opLogService.recordPendingRetry(operator, API19_CLOSE, close.getOrderNo(), null, reqBody, resp.getContent());
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
