package com.keytech.modules.maotai.eh.orderfeedback.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.common.api.ApiResponse;
import com.keytech.modules.maotai.eh.orderfeedback.dto.OrderFeedbackDTO;
import com.keytech.modules.maotai.eh.orderfeedback.entity.OrderFeedback;

/**
 * 工单信息回传服务(API15)。
 *
 * @author maotai
 * @since 2026-07-22
 */
public interface OrderFeedbackService extends BaseService<OrderFeedback> {

    /**
     * 接收 E+H 工单信息回传:落库。
     * @param dto 回传信息(OrderNo + CarNo 必填,actualWeight/loadStartTime/loadEndTime/status 选填)
     * @return 平台通用响应
     */
    ApiResponse receive(OrderFeedbackDTO dto);
}
