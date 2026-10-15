package com.keytech.modules.maotai.scada.service;

import com.keytech.modules.maotai.scada.dto.ScadaResponse;
import com.keytech.modules.maotai.scada.dto.WorkOrderStatusUpdateRequest;

/**
 * 工单状态更新服务(API13: 中集自控 → 平台)。
 *
 * @author maotai
 * @since 2026-07-28
 */
public interface WorkOrderStatusService {

    /** 接收中集自控工单状态更新(API13):落库通知记录 + 回中集自控公共响应(rqi/resultCode/resultDesp) */
    ScadaResponse receive(WorkOrderStatusUpdateRequest dto);
}
