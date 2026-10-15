package com.keytech.modules.maotai.scada.service;

import com.keytech.modules.maotai.scada.dto.ScadaResponse;
import com.keytech.modules.maotai.scada.dto.WorkOrderInfoFeedbackRequest;

/**
 * 工单信息回传服务(API17: 中集自控 → 平台)。
 *
 * @author maotai
 * @since 2026-07-29
 */
public interface WorkOrderFeedbackService {

    /** 接收中集自控工单信息回传(API17):落库通知记录 + 回中集自控公共响应(rqi/resultCode/resultDesp) */
    ScadaResponse receive(WorkOrderInfoFeedbackRequest dto);
}
