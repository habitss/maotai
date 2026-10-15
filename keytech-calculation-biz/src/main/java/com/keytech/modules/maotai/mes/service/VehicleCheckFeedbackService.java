package com.keytech.modules.maotai.mes.service;

import com.keytech.modules.maotai.mes.dto.MesResponse;
import com.keytech.modules.maotai.mes.dto.VehicleCheckFeedbackRequest;

/**
 * 车检反馈服务(API10: 勾储MES → 平台)。
 *
 * @author maotai
 * @since 2026-07-31
 */
public interface VehicleCheckFeedbackService {

    /** 接收勾储MES 车检反馈(API10):落库通知记录 + 回公共响应(rqi/resultCode/resultDesp) */
    MesResponse receive(VehicleCheckFeedbackRequest dto);
}
