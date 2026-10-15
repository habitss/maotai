package com.keytech.modules.maotai.eh.vehiclecheck.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.common.api.ApiResponse;
import com.keytech.modules.maotai.eh.vehiclecheck.dto.VehicleCheckDTO;
import com.keytech.modules.maotai.eh.vehiclecheck.dto.VehicleCheckResultRequest;
import com.keytech.modules.maotai.eh.vehiclecheck.entity.VehicleCheck;

/**
 * 车检信息服务(API08 接收 + API11 回传)。
 *
 * @author maotai
 * @since 2026-07-21
 */
public interface VehicleCheckService extends BaseService<VehicleCheck> {

    /**
     * API08 接收 E+H 车检信息:落库 + 联动工单 business_status + 自动回传 E+H(默认通过)。
     * @param dto 车检信息(checkNo 唯一;carNo 找工单;checkType 决定入厂/出厂)
     * @return 平台通用响应,content 含 落库/联动/回传 三段结果
     */
    ApiResponse receive(VehicleCheckDTO dto);

    /**
     * API11 车检结果回传 E+H,passFlag 默认 1(通过),可传覆盖。
     * @param req 车检结果(checkNo + carNo 必填;passFlag 空则补 1;allowWeight 可选)
     * @return 平台通用响应,成功时 content 带 E+H 返回内容
     */
    ApiResponse sendResultToEh(VehicleCheckResultRequest req);
}
