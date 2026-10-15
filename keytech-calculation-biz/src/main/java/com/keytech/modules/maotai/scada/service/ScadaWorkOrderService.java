package com.keytech.modules.maotai.scada.service;

import com.keytech.modules.maotai.scada.dto.ScadaResponse;
import com.keytech.modules.maotai.scada.dto.WorkOrderStartRequest;
import com.keytech.modules.maotai.scada.dto.WorkOrderSyncLoadRequest;

/**
 * 中集自控(SCADA)工单服务。
 *
 * @author maotai
 * @since 2026-07-27
 */
public interface ScadaWorkOrderService {

    /** 启动工单(API05):把请求转发给中集自控 */
    ScadaResponse startWorkOrder(WorkOrderStartRequest req);

    /** 装卸车工单同步(API14):按 orderNo 从 loading_order 组装后推中集自控 */
    ScadaResponse syncLoadWorkOrder(String orderNo, String rqi);

    /** 启动工单(API05)通用入口:调用方自行组装报文(工单任务模块的倒罐工艺用);审计/挂账/落罐转罐记录复用统一链路 */
    ScadaResponse startWorkOrderPayload(WorkOrderStartRequest req, String operator);

    /** 装卸车同步(API14)通用入口:调用方自行组装报文(工单任务模块的卸车/装车工艺用);不联动 loading_order(WO 号查不到自动跳过) */
    ScadaResponse syncLoadPayload(WorkOrderSyncLoadRequest req, String operator);
}
