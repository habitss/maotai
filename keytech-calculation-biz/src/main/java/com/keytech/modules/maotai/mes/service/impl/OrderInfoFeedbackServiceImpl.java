package com.keytech.modules.maotai.mes.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.modules.maotai.eh.ehLoading.entity.LoadingOrder;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingOrderService;
import com.keytech.modules.maotai.mes.client.MesClient;
import com.keytech.modules.maotai.mes.dto.MesResponse;
import com.keytech.modules.maotai.mes.dto.OrderInfoFeedbackRequest;
import com.keytech.modules.maotai.mes.service.OrderInfoFeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 工单信息回传实现(API07):按 orderNo 从 loading_order 取数组装,推勾储MES。
 * status 由调用方传(与 loading_order.business_status 语义不一致,不映射)。
 *
 * @author maotai
 * @since 2026-08-06
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderInfoFeedbackServiceImpl implements OrderInfoFeedbackService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MesClient mesClient;
    private final LoadingOrderService loadingOrderService;

    @Override
    public MesResponse pushOrderInfo(String orderNo, String rqi, String status, String execData, String feedbackTime) {
        LoadingOrder order = loadingOrderService.getOne(new QueryWrapper<LoadingOrder>()
                .eq("order_no", orderNo).last("LIMIT 1"));
        if (order == null) {
            return MesResponse.fail(rqi, "工单不存在: " + orderNo);
        }
        OrderInfoFeedbackRequest req = new OrderInfoFeedbackRequest();
        req.setRqi(rqi);
        req.setOrderId(order.getOrderNo());
        req.setStatus(status);
        req.setActualWeight(order.getActualWeight());
        req.setExecData(execData);
        req.setFeedbackTime(StrUtil.isNotBlank(feedbackTime) ? feedbackTime : LocalDateTime.now().format(FMT));
        return mesClient.orderInfoPush(req);
    }
}
