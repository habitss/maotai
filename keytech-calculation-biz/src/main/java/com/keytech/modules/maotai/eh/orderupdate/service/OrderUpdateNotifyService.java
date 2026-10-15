package com.keytech.modules.maotai.eh.orderupdate.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.common.api.ApiResponse;
import com.keytech.modules.maotai.eh.orderupdate.dto.OrderUpdateNotifyDTO;
import com.keytech.modules.maotai.eh.orderupdate.entity.OrderUpdateNotify;

/**
 * 工单更新通知服务(API12)。
 *
 * @author maotai
 * @since 2026-07-21
 */
public interface OrderUpdateNotifyService extends BaseService<OrderUpdateNotify> {

    /**
     * 接收 E+H 更新通知:落库 + 按 batchNo(鹤位)/seatNo(车位)联动更新 loading_order。
     * @param dto 更新通知(OrderNo 定位工单;batchNo 鹤位;seatNo 车位)
     * @return 平台通用响应,content 含联动结果(工单不存在/未携带更新值时也会落记录并说明)
     */
    ApiResponse receive(OrderUpdateNotifyDTO dto);
}
