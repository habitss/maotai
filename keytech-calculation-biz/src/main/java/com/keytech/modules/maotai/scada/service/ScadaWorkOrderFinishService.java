package com.keytech.modules.maotai.scada.service;

import com.keytech.modules.maotai.scada.dto.ScadaResponse;
import com.keytech.modules.maotai.scada.dto.WorkOrderFinishNotify;

/**
 * 中集自控(SCADA)工单结束通知服务()。
 *
 * @author maotai
 * @since 2026-07-27
 */
public interface ScadaWorkOrderFinishService {

    /** 接收中集自控工单结束通知(API06):落库 + 回中集自控公共响应(rqi/resultCode/resultDesp) */
    ScadaResponse receive(WorkOrderFinishNotify dto);
}
