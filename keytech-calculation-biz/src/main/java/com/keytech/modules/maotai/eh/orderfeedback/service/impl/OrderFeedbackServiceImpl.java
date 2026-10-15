package com.keytech.modules.maotai.eh.orderfeedback.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.common.api.ApiResponse;
import com.keytech.modules.maotai.eh.ehLoading.entity.LoadingOrder;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingOrderService;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;
import com.keytech.modules.maotai.eh.orderfeedback.dto.OrderFeedbackDTO;
import com.keytech.modules.maotai.eh.orderfeedback.entity.OrderFeedback;
import com.keytech.modules.maotai.eh.orderfeedback.mapper.OrderFeedbackMapper;
import com.keytech.modules.maotai.eh.orderfeedback.service.OrderFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 工单信息回传实现(API15)。
 * 落库 + 联动 loading_order(装卸实际:actualWeight/loadStartTime/loadEndTime/ehStatus)。
 * 语义是 UPDATE(重复推送=重放写同值,天然无害),不做幂等,不建唯一索引;重复只多落一行回传记录供追溯。
 * 全部调用记操作日志(mes3_maotai_api_op_log,operator=E+H)。
 *
 * @author maotai
 * @since 2026-07-22
 */
@Service
@RequiredArgsConstructor
public class OrderFeedbackServiceImpl extends BaseServiceImpl<OrderFeedbackMapper, OrderFeedback> implements OrderFeedbackService {

    private static final String API15_FINISH = "API15";
    /** 公共状态:E+H 字典 5-装卸车结束 */
    private static final int STATUS_LOAD_FINISHED = 5;
    /** 平台内部工单状态:7-装车/卸车结束(API15 联动) 8-装卸车工单结束(API19,终态) */
    private static final int ORDER_STATUS_LOAD_FINISHED = 7;
    private static final int ORDER_STATUS_FINISH = 8;

    private final LoadingOrderService loadingOrderService;
    private final ApiOpLogService opLogService;
    private final ObjectMapper objectMapper;

    @Override
    public ApiResponse receive(OrderFeedbackDTO dto) {
        String reqBody = toJson(dto);
        // 落回传记录
        OrderFeedback rec = new OrderFeedback();
        BeanUtils.copyProperties(dto, rec);
        this.save(rec);
        // 联动工单
        String msg = linkOrder(dto);
        opLogService.recordInbound(ApiOpLogService.OPERATOR_INBOUND, API15_FINISH, dto.getOrderNo(), null, 1, reqBody, msg);
        // 已结束工单/工单不存在的迟到回传:内容留痕但返回失败,让 E+H 侧感知联动未生效
        if (msg.startsWith("工单已结束") || msg.startsWith("工单不存在")) {
            return ApiResponse.fail(msg);
        }
        return ApiResponse.ok("成功;" + msg);
    }

    /** 按 OrderNo 更新 loading_order 的装卸实际(actualWeight/loadStartTime/loadEndTime/ehStatus)与状态。
     * E+H 只回传 status="5"(装卸车结束):公共状态写5、平台内部状态写5;
     * 工单已结束(order_status=8)时不再覆盖任何字段,返回异常提示(防迟到报文把终态改回去)。 */
    private String linkOrder(OrderFeedbackDTO dto) {
        LoadingOrder order = loadingOrderService.getOne(new QueryWrapper<LoadingOrder>()
                .eq("order_no", dto.getOrderNo()).last("LIMIT 1"));
        if (order == null) {
            return "工单不存在: " + dto.getOrderNo() + ",仅记录";
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() == ORDER_STATUS_FINISH) {
            return "工单已结束,不能更新实际量/装卸时间/业务状态,仅记录";
        }
        if (dto.getActualWeight() != null) {
            order.setActualWeight(dto.getActualWeight());
        }
        if (StrUtil.isNotBlank(dto.getLoadStartTime())) {
            order.setLoadStartTime(dto.getLoadStartTime());
        }
        if (StrUtil.isNotBlank(dto.getLoadEndTime())) {
            order.setLoadEndTime(dto.getLoadEndTime());
        }
        if (StrUtil.isNotBlank(dto.getStatus())) {
            // E+H 回传 status="5"=装卸车结束:公共状态与平台内部状态同步置5
            order.setStatus(STATUS_LOAD_FINISHED);
            order.setOrderStatus(ORDER_STATUS_LOAD_FINISHED);
        }
        loadingOrderService.updateById(order);
        return "已更新工单 " + order.getOrderNo();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }
}
