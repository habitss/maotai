package com.keytech.modules.maotai.scada.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.maotai.scada.dto.ScadaResponse;
import com.keytech.modules.maotai.scada.dto.WorkOrderFinishNotify;
import com.keytech.modules.maotai.scada.entity.TankTransferOrder;
import com.keytech.modules.maotai.scada.entity.WorkOrderFinish;
import com.keytech.modules.maotai.scada.mapper.WorkOrderFinishMapper;
import com.keytech.modules.maotai.scada.service.ScadaWorkOrderFinishService;
import com.keytech.modules.maotai.scada.service.TankTransferOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;

/**
 * 工单结束通知实现(API06):落通知记录 + 关罐转罐工单单 + 回中集自控公共响应,rqi 幂等(uk_rqi)。
 *
 * @author maotai
 * @since 2026-07-27
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScadaWorkOrderFinishServiceImpl extends BaseServiceImpl<WorkOrderFinishMapper, WorkOrderFinish> implements ScadaWorkOrderFinishService {

    private static final String API06_FINISH = "API06";
    /** SCADA 入站操作人(暂时统一 SCADA,后续拿中集真实用户名后替换) */
    private static final String OPERATOR_SCADA_IN = "SCADA";
    /** 罐转罐工单状态:10-罐转罐结束(API06,终态) */
    private static final int TRANSFER_STATUS_FINISH = 10;

    private final ApiOpLogService opLogService;
    private final TankTransferOrderService tankTransferOrderService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    public ScadaResponse receive(WorkOrderFinishNotify dto) {
        String reqBody = toJson(dto);
        WorkOrderFinish rec = new WorkOrderFinish();
        BeanUtils.copyProperties(dto, rec);
        try {
            this.save(rec);
        } catch (DuplicateKeyException e) {
            log.info("[API06] 重复请求已忽略 rqi={}", dto.getRqi());
            opLogService.recordInbound(OPERATOR_SCADA_IN, API06_FINISH, dto.getWorkOrderNo(), null, 0, reqBody, "重复请求已忽略 rqi=" + dto.getRqi());
            return ScadaResponse.ok(dto.getRqi(), "重复请求已忽略");
        } catch (Exception e) {
            log.error("[API06] 落库失败 rqi={} workOrderNo={}", dto.getRqi(), dto.getWorkOrderNo(), e);
            opLogService.recordInbound(OPERATOR_SCADA_IN, API06_FINISH, dto.getWorkOrderNo(), null, 0, reqBody, "落库失败: " + e.getMessage());
            return ScadaResponse.fail(dto.getRqi(), "落库失败: " + e.getMessage());
        }
        // 关罐转罐工单:启动单不存在 → resultCode=0 明确回告中集(通知已留痕),对齐 E+H"工单不存在→code=0"口径
        TankTransferOrder transferOrder = findTransferOrder(dto.getWorkOrderNo());
        if (transferOrder == null) {
            log.warn("[API06] 罐转罐工单号不存在(未在平台启动),仅落通知 workOrderNo={}", dto.getWorkOrderNo());
            String notFoundMsg = "罐转罐工单号不存在: " + dto.getWorkOrderNo() + "(通知已留痕)";
            opLogService.recordInbound(OPERATOR_SCADA_IN, API06_FINISH, dto.getWorkOrderNo(), null, 0, reqBody, notFoundMsg);
            return ScadaResponse.fail(dto.getRqi(), notFoundMsg);
        }
        String closeResult = closeTankTransferOrder(transferOrder, dto);
        String msg = "已落库工单结束通知 workOrderNo=" + dto.getWorkOrderNo() + closeResult;
        opLogService.recordInbound(OPERATOR_SCADA_IN, API06_FINISH, dto.getWorkOrderNo(), null, 1, reqBody, msg);
        return ScadaResponse.ok(dto.getRqi(), msg);
    }

    /** 按 workOrderNo 查罐转罐工单(不存在=中集先于 API05 到达/未走平台启动) */
    private TankTransferOrder findTransferOrder(String workOrderNo) {
        return tankTransferOrderService.getOne(new QueryWrapper<TankTransferOrder>()
                .eq("work_order_no", workOrderNo).last("LIMIT 1"));
    }

    /** 关单:9-启动 改 10-结束;终态不可覆盖;关单失败不吞结果(通知已留痕,响应如实说明)。
     * @return 关单结果描述(拼进响应 resultDesp) */
    private String closeTankTransferOrder(TankTransferOrder order, WorkOrderFinishNotify dto) {
        if (order.getOrderStatus() != null && order.getOrderStatus() == TRANSFER_STATUS_FINISH) {
            log.info("[API06] 罐转罐工单已是终态,不覆盖 workOrderNo={}", dto.getWorkOrderNo());
            return "(罐转罐工单已是终态,不重复关单)";
        }
        try {
            order.setOrderStatus(TRANSFER_STATUS_FINISH);
            order.setFinishRqi(dto.getRqi());
            order.setFinishTime(dto.getSyncTime());
            tankTransferOrderService.updateById(order);
        } catch (Exception e) {
            log.error("[API06] 关罐转罐工单失败 workOrderNo={}", dto.getWorkOrderNo(), e);
            return "(关单失败,通知已留痕待人工核对)";
        }
        log.info("[API06] 罐转罐工单已关单 workOrderNo={} status=10", dto.getWorkOrderNo());
        return ",罐转罐工单已关单 status=10";
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }
}
