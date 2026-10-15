package com.keytech.modules.maotai.scada.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.maotai.eh.ehLoading.entity.LoadingOrder;
import com.keytech.modules.maotai.scada.dto.ScadaResponse;
import com.keytech.modules.maotai.scada.dto.WorkOrderStatusUpdateRequest;
import com.keytech.modules.maotai.scada.entity.WorkOrderStatus;
import com.keytech.modules.maotai.scada.mapper.WorkOrderStatusMapper;
import com.keytech.modules.maotai.scada.service.WorkOrderStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingOrderService;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;

/**
 * 工单状态更新实现(API13):落库通知记录 + 回中集自控公共响应,rqi 幂等(uk_rqi)。
 * <p>联动 loading_order:公共 status 原样承接中集回传值(传多少记多少,不做白名单);
 * 平台内部 order_status 不动(状态机以平台侧事件为准)。
 *
 * @author maotai
 * @since 2026-07-28
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkOrderStatusServiceImpl extends BaseServiceImpl<WorkOrderStatusMapper, WorkOrderStatus> implements WorkOrderStatusService {

    private static final String API13_STATUS = "API13";
    /** SCADA 入站操作人(暂时统一 SCADA,后续拿中集真实用户名后替换) */
    private static final String OPERATOR_SCADA_IN = "SCADA";
    /** 平台内部工单状态:8-装卸车工单结束(API19,终态不覆盖) */
    private static final int ORDER_STATUS_FINISH = 8;

    private final ApiOpLogService opLogService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final LoadingOrderService loadingOrderService;

    @Override
    public ScadaResponse receive(WorkOrderStatusUpdateRequest dto) {
        String reqBody = toJson(dto);
        WorkOrderStatus rec = new WorkOrderStatus();
        BeanUtils.copyProperties(dto, rec);
        // 请求 updateTime 与 DataBase.updateTime(Date) 冲突,实体用 statusUpdateTime 承接
        rec.setStatusUpdateTime(dto.getUpdateTime());
        try {
            this.save(rec);
        } catch (DuplicateKeyException e) {
            log.info("[API13] 重复请求已忽略 rqi={}", dto.getRqi());
            opLogService.recordInbound(OPERATOR_SCADA_IN, API13_STATUS, dto.getOrderNo(), null, 0, reqBody, "重复请求已忽略 rqi=" + dto.getRqi());
            return ScadaResponse.ok(dto.getRqi(), "重复请求已忽略");
        } catch (Exception e) {
            log.error("[API13] 落库失败 rqi={} OrderNo={}", dto.getRqi(), dto.getOrderNo(), e);
            opLogService.recordInbound(OPERATOR_SCADA_IN, API13_STATUS, dto.getOrderNo(), null, 0, reqBody, "落库失败: " + e.getMessage());
            return ScadaResponse.fail(dto.getRqi(), "落库失败: " + e.getMessage());
        }
        // 联动 loading_order(2026-08-22:SCADA 的 OrderNo 即装卸车工单号,同 E+H API12 口径)
        String link = linkOrder(dto);
        String msg = "已落库工单状态更新 OrderNo=" + dto.getOrderNo() + " status=" + dto.getStatus();
        if (!link.isEmpty()) {
            msg = msg + ";" + link;
        }
        opLogService.recordInbound(OPERATOR_SCADA_IN, API13_STATUS, dto.getOrderNo(), null, 1, reqBody, msg);
        // 工单不存在/已结束:通知已留痕但联动未生效,返回 resultCode=0 让中集侧感知(同 E+H API12 口径)
        if (link.startsWith("工单不存在") || link.startsWith("工单已结束")) {
            return ScadaResponse.fail(dto.getRqi(), msg);
        }
        return ScadaResponse.ok(dto.getRqi(), msg);
    }

    /** 按 OrderNo 联动 loading_order:公共状态 status 原样接收中集回传值(传多少记多少,不做白名单),终态8不覆盖。
     *  状态映射规则同 E+H:公共状态只认对方报文的 status;平台内部 order_status 不在此推进
     *  (内部状态机以平台侧事件为准——API14 成功置6、API19 成功置8)。 */
    private String linkOrder(WorkOrderStatusUpdateRequest dto) {
        try {
            LoadingOrder order = loadingOrderService.getOne(new QueryWrapper<LoadingOrder>()
                    .eq("order_no", dto.getOrderNo()).last("LIMIT 1"));
            if (order == null) {
                return "工单不存在: " + dto.getOrderNo();
            }
            if (order.getOrderStatus() != null && order.getOrderStatus() == ORDER_STATUS_FINISH) {
                return "工单已结束,不更新状态,仅记录";
            }
            String s = dto.getStatus();
            if (s != null && !s.isEmpty()) {
                // 公共状态原样接收中集回传值——传多少记多少,平台不解释不做白名单
                // (终态守卫在上方 order_status=8 已拦,不会覆盖已结束单)
                order.setStatus(Integer.valueOf(s));
            }
            if (dto.getBatchNo() != null && !dto.getBatchNo().isEmpty()) {
                order.setBatchNo(dto.getBatchNo());
            }
            loadingOrderService.updateById(order);
            return "已更新工单 " + dto.getOrderNo() + " status=" + s;
        } catch (Exception e) {
            log.warn("[API13] 联动工单失败 OrderNo={}", dto.getOrderNo(), e);
            return "联动失败: " + e.getMessage();
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }
}
