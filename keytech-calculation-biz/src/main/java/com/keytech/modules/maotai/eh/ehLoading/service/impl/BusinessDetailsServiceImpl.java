package com.keytech.modules.maotai.eh.ehLoading.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.modules.maotai.eh.ehLoading.client.EhLoadingClient;
import com.keytech.modules.maotai.eh.ehLoading.dto.BusinessDetailsRequest;
import com.keytech.modules.maotai.eh.ehLoading.dto.EhResponse;
import com.keytech.modules.maotai.eh.ehLoading.service.BusinessDetailsService;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingOrderService;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.keytech.modules.maotai.eh.ehLoading.entity.LoadingOrder;

/**
 * 查询业务命令实现(API16,出站)。token 管理/失效重试/出站容错(超时+异常降级返回 fail)已下沉到 EhLoadingClient,本层只转发。
 * 查询不挂重试(失败即返回由用户重查),只记操作日志(retry_status=NULL)。
 *
 * @author maotai
 * @since 2026-07-24
 */
@Service
@RequiredArgsConstructor
public class BusinessDetailsServiceImpl implements BusinessDetailsService {

    private static final String API16_DETAILS = "API16";

    private final EhLoadingClient client;
    private final ApiOpLogService opLogService;
    private final LoadingOrderService loadingOrderService;
    private final ObjectMapper objectMapper;

    @Override
    public EhResponse queryBusinessDetails(BusinessDetailsRequest req) {
        String reqBody = toJson(req);
        String operator = loadingOrderService.currentModifyor();
        try {
            EhResponse resp = client.queryBusinessDetails(req);
            opLogService.recordOutbound(operator, API16_DETAILS, req.getOrderNo(), null, reqBody, resp != null && resp.isSuccess(), resp == null ? null : resp.getContent());
            syncStatusFromEh(req.getOrderNo(), resp);
            return resp;
        } catch (Exception e) {
            opLogService.recordOutbound(operator, API16_DETAILS, req.getOrderNo(), null, reqBody, false, "异常: " + e.getMessage());
            throw e;
        }
    }

    /**
     * API16 查回 data.status 时同步公共状态(E+H 字典:1-新建业务 5-装卸车结束 8-业务结束)。
     * 只写公共状态 status,不动平台内部 order_status(内部编排以平台事件为准);终态8例外——
     * E+H 侧已业务结束的单,平台内部也置 8-装卸车工单结束,保证不出现"E+H结束了平台还占着IC卡"的错位。
     */
    private void syncStatusFromEh(String orderNo, EhResponse resp) {
        try {
            if (resp == null || !resp.isSuccess() || resp.getData() == null
                    || resp.getData().get("status") == null || resp.getData().get("status").isMissingNode()) {
                return;
            }
            int ehStatusVal = resp.getData().get("status").asInt();
            if (ehStatusVal != 1 && ehStatusVal != 5 && ehStatusVal != 8) {
                return;   // 非法值不写
            }
            LoadingOrder order = loadingOrderService.getOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<LoadingOrder>()
                            .eq("order_no", orderNo).last("LIMIT 1"));
            if (order == null) {
                return;
            }
            order.setStatus(ehStatusVal);
            if (ehStatusVal == 8 && (order.getOrderStatus() == null || order.getOrderStatus() != 8)) {
                order.setOrderStatus(8);   // E+H侧已业务结束 → 平台内部 8-装卸车工单结束
            }
            loadingOrderService.updateById(order);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(BusinessDetailsServiceImpl.class)
                    .warn("[API16] 同步公共状态失败 orderNo={}", orderNo, e);
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }
}
