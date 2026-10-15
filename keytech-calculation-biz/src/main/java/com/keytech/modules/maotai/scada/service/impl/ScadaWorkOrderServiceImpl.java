package com.keytech.modules.maotai.scada.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.modules.maotai.eh.ehLoading.entity.LoadingOrder;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingOrderService;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;
import com.keytech.modules.maotai.scada.client.ScadaClient;
import com.keytech.modules.maotai.scada.dto.ScadaResponse;
import com.keytech.modules.maotai.scada.dto.WorkOrderStartRequest;
import com.keytech.modules.maotai.scada.dto.WorkOrderSyncLoadRequest;
import com.keytech.modules.maotai.scada.entity.TankTransferOrder;
import com.keytech.modules.maotai.scada.service.ScadaWorkOrderService;
import com.keytech.modules.maotai.scada.service.TankTransferOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 中集自控(SCADA)工单服务实现。
 * <p>出站防错(2026-08-21 对标 E+H):API05/14 全部记操作日志(mes3_maotai_api_op_log 复用,
 * apiCode=API05/API14);失败挂账 retry_status=0 由 EhPushRetryJob 3次×60s 重发。
 *
 * @author maotai
 * @since 2026-07-27
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScadaWorkOrderServiceImpl implements ScadaWorkOrderService {

    private static final String API05_START = "API05";
    private static final String API14_SYNC_LOAD = "API14";
    /** 平台内部工单状态:6-启动工单(API14 成功) 8-装卸车工单结束(API19,终态不覆盖) */
    private static final int ORDER_STATUS_STARTED = 6;
    private static final int ORDER_STATUS_FINISH = 8;
    /** 罐转罐工单状态:9-罐转罐启动(API05 成功) 10-罐转罐结束(API06,终态) */
    private static final int TRANSFER_STATUS_STARTED = 9;
    private static final int TRANSFER_STATUS_FINISH = 10;

    private final ScadaClient scadaClient;
    private final LoadingOrderService loadingOrderService;
    private final ApiOpLogService opLogService;
    private final ObjectMapper objectMapper;
    private final TankTransferOrderService tankTransferOrderService;

    @Override
    public ScadaResponse startWorkOrder(WorkOrderStartRequest req) {
        String reqBody = toJson(req);
        String operator = loadingOrderService.currentModifyor();
        ScadaResponse resp;
        try {
            resp = scadaClient.startWorkOrder(req);
        } catch (Exception e) {
            // client 内部已降级返回 fail,这里是兜底(理论不进)
            log.warn("[API05] 启动工单异常 rqi={}", req.getRqi(), e);
            opLogService.recordPendingRetry(operator, API05_START, req.getWorkOrderNo(), null, reqBody, "启动工单异常: " + e.getMessage());
            return ScadaResponse.fail(req.getRqi(), "启动工单异常: " + e.getMessage());
        }
        recordOut(operator, API05_START, req.getWorkOrderNo(), reqBody, resp);
        return resp;
    }

    /** 装卸车工单同步(API14):按 orderNo 从 loading_order 取数组装,推中集自控。 */
    @Override
    public ScadaResponse syncLoadWorkOrder(String orderNo, String rqi) {
        LoadingOrder order = loadingOrderService.getOne(new QueryWrapper<LoadingOrder>()
                .eq("order_no", orderNo).last("LIMIT 1"));
        if (order == null) {
            return ScadaResponse.fail(rqi, "工单不存在: " + orderNo);
        }
        WorkOrderSyncLoadRequest req = new WorkOrderSyncLoadRequest();
        req.setRqi(rqi);
        req.setOrderNo(order.getOrderNo());
        req.setBusiType(order.getBusiType());
        req.setCarNo(order.getCarNo());
        req.setPreWeight(order.getPreWeight());
        req.setProductName(order.getProductName());
        // 罐号照单全传(2026-08-27):TankNameBefore/TankName 两字段都带上,必填性由 API04 建档时
        // 按 busiType 校验保证——装车必有来源罐,卸车必有目标罐;另一头无值推空串 ""(中集 .NET 不收 null)
        req.setTankNameBefore(StrUtil.nullToEmpty(order.getTankNameBefore()));
        req.setTankName(StrUtil.nullToEmpty(order.getTankName()));
        req.setDriverName(order.getDriverName());
        req.setValidate(order.getValidate());
        req.setSyncTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        String reqBody = toJson(req);
        String operator = loadingOrderService.currentModifyor();
        ScadaResponse resp;
        try {
            resp = scadaClient.syncLoad(req);
        } catch (Exception e) {
            log.warn("[API14] 同步工单异常 orderNo={}", orderNo, e);
            opLogService.recordPendingRetry(operator, API14_SYNC_LOAD, orderNo, null, reqBody, "同步工单异常: " + e.getMessage());
            return ScadaResponse.fail(rqi, "同步工单异常: " + e.getMessage());
        }
        recordOut(operator, API14_SYNC_LOAD, orderNo, reqBody, resp);
        return resp;
    }

    /** 启动工单(API05)通用入口:工单任务模块(倒罐工艺)组装报文;成功落罐转罐记录,失败挂账重试。 */
    @Override
    public ScadaResponse startWorkOrderPayload(WorkOrderStartRequest req, String operator) {
        String reqBody = toJson(req);
        ScadaResponse resp;
        try {
            resp = scadaClient.startWorkOrder(req);
        } catch (Exception e) {
            log.warn("[API05] 工单任务启动异常 orderNo={}", req.getWorkOrderNo(), e);
            opLogService.recordPendingRetry(operator, API05_START, req.getWorkOrderNo(), null, reqBody, "启动工单异常: " + e.getMessage());
            return ScadaResponse.fail(req.getRqi(), "启动工单异常: " + e.getMessage());
        }
        recordOut(operator, API05_START, req.getWorkOrderNo(), reqBody, resp);
        return resp;
    }

    /** 装卸车同步(API14)通用入口:工单任务模块(卸车/装车工艺)组装报文。
     *  recordOut 的联动对 WO 号自动空转(loading_order 查无此单),不影响工单任务自身状态管理。 */
    @Override
    public ScadaResponse syncLoadPayload(WorkOrderSyncLoadRequest req, String operator) {
        String reqBody = toJson(req);
        ScadaResponse resp;
        try {
            resp = scadaClient.syncLoad(req);
        } catch (Exception e) {
            log.warn("[API14] 工单任务同步异常 orderNo={}", req.getOrderNo(), e);
            opLogService.recordPendingRetry(operator, API14_SYNC_LOAD, req.getOrderNo(), null, reqBody, "同步工单异常: " + e.getMessage());
            return ScadaResponse.fail(req.getRqi(), "同步工单异常: " + e.getMessage());
        }
        recordOut(operator, API14_SYNC_LOAD, req.getOrderNo(), reqBody, resp);
        return resp;
    }

    /** 成功记审计,失败挂账重试(API05/14 均可重试:rqi 幂等由中集侧或我方唯一性兜底,重发安全)。
     * API14 成功联动 loading_order:平台内部状态置 6-启动工单(公共状态不动——SCADA 报文无 status 字段)。
     * API05 成功落罐转罐工单表:INSERT 一条 9-罐转罐启动(uk_work_order_no 幂等,重复启动忽略)。 */
    private void recordOut(String operator, String apiCode, String orderNo, String reqBody, ScadaResponse resp) {
        if (resp != null && resp.isSuccess()) {
            opLogService.recordOutbound(operator, apiCode, orderNo, null, reqBody, true, resp.getResultDesp());
            if (API14_SYNC_LOAD.equals(apiCode)) {
                updateOrderStatusStarted(orderNo);
            } else if (API05_START.equals(apiCode)) {
                saveTankTransferOrder(reqBody);
            }
        } else {
            opLogService.recordPendingRetry(operator, apiCode, orderNo, null, reqBody,
                    resp == null ? "无响应" : resp.getResultDesp());
        }
    }

    /** API05 推送成功 → 落罐转罐工单表(mes3_maotai_tank_transfer_order)一条"9-启动"。
     * uk_work_order_no 幂等:同工单重复启动(DuplicateKey)忽略不报错;重试链路重放成功也安全。 */
    private void saveTankTransferOrder(String reqBody) {
        try {
            WorkOrderStartRequest req = objectMapper.readValue(reqBody, WorkOrderStartRequest.class);
            TankTransferOrder order = new TankTransferOrder();
            order.setWorkOrderNo(req.getWorkOrderNo());
            order.setOrderStatus(TRANSFER_STATUS_STARTED);
            order.setStartRqi(req.getRqi());
            order.setCurrentVehicleQty(req.getCurrentVehicleQty());
            order.setTargetTankCode(req.getTargetTankCode());
            order.setTransferSetpoint(req.getTransferSetpoint());
            order.setAlcoholContent(req.getAlcoholContent());
            order.setLoadingPath(req.getLoadingPath());
            order.setSourceTankCode(req.getSourceTankCode());
            order.setOperatorName(req.getOperatorName());
            order.setStartTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            tankTransferOrderService.save(order);
            log.info("[API05] 罐转罐工单已建 workOrderNo={} status=9", req.getWorkOrderNo());
        } catch (DuplicateKeyException e) {
            log.info("[API05] 罐转罐工单已存在,忽略重复启动 workOrderNo={}", extractWorkOrderNo(reqBody));
        } catch (Exception e) {
            // 建单失败不影响推送结果(审计已有),但要看见
            log.error("[API05] 罐转罐工单建单失败 reqBody={}", reqBody, e);
        }
    }

    /** 日志用:从 req_body 里抠 workOrderNo(反序列化失败时的兜底) */
    private String extractWorkOrderNo(String reqBody) {
        try {
            return objectMapper.readTree(reqBody).path("workOrderNo").asText("");
        } catch (Exception e) {
            return "";
        }
    }

    /** API14 推送成功 → 平台内部状态 6-启动工单(仅装卸车单;终态8不覆盖) */
    private void updateOrderStatusStarted(String orderNo) {
        try {
            LoadingOrder order = loadingOrderService.getOne(new QueryWrapper<LoadingOrder>()
                    .eq("order_no", orderNo).last("LIMIT 1"));
            if (order == null || (order.getOrderStatus() != null && order.getOrderStatus() == ORDER_STATUS_FINISH)) {
                return;
            }
            order.setOrderStatus(ORDER_STATUS_STARTED);
            loadingOrderService.updateById(order);
        } catch (Exception e) {
            log.warn("[API14] 回写工单启动状态失败 orderNo={}", orderNo, e);
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
