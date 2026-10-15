package com.keytech.modules.maotai.mes.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.modules.maotai.eh.ehLoading.entity.LoadingOrder;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingOrderService;
import com.keytech.modules.maotai.mes.client.MesClient;
import com.keytech.modules.maotai.mes.dto.MesResponse;
import com.keytech.modules.maotai.mes.dto.OrderFinishSummaryRequest;
import com.keytech.modules.maotai.mes.service.OrderFinishSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.stereotype.Service;

/**
 * 工单结束汇总实现(API18):按 orderNo 从 loading_order 取数组装,推勾储MES。
 *
 * @author maotai
 * @since 2026-07-31
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderFinishSummaryServiceImpl implements OrderFinishSummaryService {

    private static final String STATUS_FINISH = "8"; // 8-业务结束(通知MES)

    private final MesClient mesClient;
    private final LoadingOrderService loadingOrderService;

    @Override
    public MesResponse pushOrderFinish(String orderNo, String rqi) {
        LoadingOrder order = loadingOrderService.getOne(new QueryWrapper<LoadingOrder>()
                .eq("order_no", orderNo).last("LIMIT 1"));
        if (order == null) {
            return MesResponse.fail(rqi, "工单不存在: " + orderNo);
        }
        OrderFinishSummaryRequest req = new OrderFinishSummaryRequest();
        req.setRqi(rqi);
        req.setOrderId(order.getOrderNo());
        req.setActualWeight(order.getActualWeight());
        req.setLoadStartTime(order.getLoadStartTime());
        req.setLoadEndTime(order.getLoadEndTime());
        req.setAbv(order.getAbv());
        req.setStatus(STATUS_FINISH);
        req.setSummaryInfo(null); // 平台汇总摘要,结构待定,暂留空
        req.setModifyor(currentModifyor());
        return mesClient.orderFinishPush(req);
    }

    /** 取当前登录用户真实姓名作为 Modifyor,取不到记 platform(镜像 LoadingOrderServiceImpl.closeOrder) */
    private String currentModifyor() {
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal instanceof LoginUser) {
                String name = ((LoginUser) principal).getRealname();
                if (StrUtil.isNotBlank(name)) {
                    return name;
                }
            }
        } catch (Exception e) {
            log.warn("[API18] 取当前用户失败,记 platform", e);
        }
        return "platform";
    }
}
