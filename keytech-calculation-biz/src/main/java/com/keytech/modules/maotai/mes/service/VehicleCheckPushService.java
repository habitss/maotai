package com.keytech.modules.maotai.mes.service;

import com.keytech.modules.maotai.mes.dto.MesResponse;

/**
 * 车检信息推送服务(API09: 平台 → 勾储MES)。
 *
 * @author maotai
 * @since 2026-07-30
 */
public interface VehicleCheckPushService {

    /** 车检信息推送(API09):按 checkNo 从 vehicle_check 组装后推勾储MES */
    MesResponse pushVehicleCheck(String checkNo, String rqi);
}
