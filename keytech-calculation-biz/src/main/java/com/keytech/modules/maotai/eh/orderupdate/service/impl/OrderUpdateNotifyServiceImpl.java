package com.keytech.modules.maotai.eh.orderupdate.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.common.api.ApiResponse;
import com.keytech.modules.maotai.eh.ehLoading.entity.LoadingOrder;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingOrderService;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;
import com.keytech.modules.maotai.eh.orderupdate.dto.OrderUpdateNotifyDTO;
import com.keytech.modules.maotai.eh.orderupdate.entity.OrderUpdateNotify;
import com.keytech.modules.maotai.eh.orderupdate.mapper.OrderUpdateNotifyMapper;
import com.keytech.modules.maotai.eh.orderupdate.service.OrderUpdateNotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 工单更新通知实现(API12):落通知记录 + 联动更新 loading_order。
 * 语义是 UPDATE(重复推送=重放写同值,天然无害),不做幂等,不建唯一索引;重复只多落一行通知供追溯。
 * 全部调用记操作日志(mes3_maotai_api_op_log,operator=E+H)。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Service
@RequiredArgsConstructor
public class OrderUpdateNotifyServiceImpl extends BaseServiceImpl<OrderUpdateNotifyMapper, OrderUpdateNotify> implements OrderUpdateNotifyService {

    private static final String API12_UPDATE_NOTIFY = "API12";
    /** 平台内部工单状态:5-叫号入场(API12 联动) 8-装卸车工单结束(API19,终态不覆盖) */
    private static final int ORDER_STATUS_CALLED = 5;
    private static final int ORDER_STATUS_FINISH = 8;

    private final LoadingOrderService loadingOrderService;
    private final ApiOpLogService opLogService;
    private final ObjectMapper objectMapper;

    @Override
    public ApiResponse receive(OrderUpdateNotifyDTO dto) {
        OrderUpdateNotify rec = new OrderUpdateNotify();
        BeanUtils.copyProperties(dto, rec);

        String msg;
        LoadingOrder order = loadingOrderService.getOne(new QueryWrapper<LoadingOrder>()
                .eq("order_no", dto.getOrderNo()).last("LIMIT 1"));
        if (order == null) {
            msg = "工单不存在: " + dto.getOrderNo();
        } else if (order.getOrderStatus() != null && order.getOrderStatus() == ORDER_STATUS_FINISH) {
            // 已结束单(终态):鹤位/车位不覆盖,留痕返回失败
            msg = "工单已结束,不能更新鹤位/车位,仅记录";
        } else {
            // 鹤位/车位为独立字段,非空才更新(一次通知可同时带两者)
            boolean changed = false;
            StringBuilder sb = new StringBuilder();
            if (StrUtil.isNotBlank(dto.getBatchNo())) {
                order.setBatchNo(dto.getBatchNo());
                sb.append("鹤位 batchNo=").append(dto.getBatchNo()).append(" ");
                changed = true;
            }
            if (StrUtil.isNotBlank(dto.getSeatNo())) {
                order.setSeatNo(dto.getSeatNo());
                sb.append("车位 seatNo=").append(dto.getSeatNo());
                changed = true;
            }
            if (changed) {
                order.setOrderStatus(ORDER_STATUS_CALLED); // API12 联动成功 → 平台内部状态 5-叫号入场
                loadingOrderService.updateById(order);
                msg = "已更新 " + sb.toString().trim();
            } else {
                msg = "未携带鹤位/车位更新值";
            }
        }

        rec.setProcessMsg(msg);
        this.save(rec);
        opLogService.recordInbound(ApiOpLogService.OPERATOR_INBOUND, API12_UPDATE_NOTIFY, dto.getOrderNo(), null, 1, toJson(dto), msg);
        // 工单不存在/已结束:内容留痕但返回失败(code=0),让 E+H 侧感知联动未生效
        if (order == null || msg.startsWith("工单已结束")) {
            return ApiResponse.fail(msg);
        }
        return ApiResponse.ok(msg);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }
}
