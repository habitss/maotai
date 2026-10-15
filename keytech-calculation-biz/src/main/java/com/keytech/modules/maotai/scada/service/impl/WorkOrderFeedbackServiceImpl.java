package com.keytech.modules.maotai.scada.service.impl;

import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.maotai.scada.dto.ScadaResponse;
import com.keytech.modules.maotai.scada.dto.WorkOrderInfoFeedbackRequest;
import com.keytech.modules.maotai.scada.entity.WorkOrderFeedback;
import com.keytech.modules.maotai.scada.mapper.WorkOrderFeedbackMapper;
import com.keytech.modules.maotai.scada.service.WorkOrderFeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingOrderService;
import com.keytech.modules.maotai.scada.dto.WorkOrderInfoFeedbackRequest;
import com.keytech.modules.maotai.eh.ehLoading.entity.LoadingOrder;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;

/**
 * 工单信息回传实现(API17):落库通知记录 + 回中集自控公共响应,rqi 幂等(uk_rqi)。
 * <p>联动 loading_order 的专字段 scada_status(2026-08-26):中集改传运行态字典
 * 0-空闲 1-运行 7-暂停 10-异常终止 11-正常结束,原样承接;公共 status(E+H 字典)与
 * 内部 order_status 不动;原 status=5→内部7 联动作废。infoData 仍原样存 JSON。
 *
 * @author maotai
 * @since 2026-07-29
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkOrderFeedbackServiceImpl extends BaseServiceImpl<WorkOrderFeedbackMapper, WorkOrderFeedback> implements WorkOrderFeedbackService {

    private static final String API17_FEEDBACK = "API17";
    /** SCADA 入站操作人(暂时统一 SCADA,后续拿中集真实用户名后替换) */
    private static final String OPERATOR_SCADA_IN = "SCADA";
    /** 平台内部工单状态:8-装卸车工单结束(终态不覆盖);内部状态机不由 API17 推进 */
    private static final int ORDER_STATUS_FINISH = 8;

    private final ApiOpLogService opLogService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final LoadingOrderService loadingOrderService;

    @Override
    public ScadaResponse receive(WorkOrderInfoFeedbackRequest dto) {
        String reqBody = toJson(dto);
        WorkOrderFeedback rec = new WorkOrderFeedback();
        BeanUtils.copyProperties(dto, rec);
        try {
            this.save(rec);
        } catch (DuplicateKeyException e) {
            log.info("[API17] 重复请求已忽略 rqi={}", dto.getRqi());
            opLogService.recordInbound(OPERATOR_SCADA_IN, API17_FEEDBACK, dto.getOrderNo(), null, 0, reqBody, "重复请求已忽略 rqi=" + dto.getRqi());
            return ScadaResponse.ok(dto.getRqi(), "重复请求已忽略");
        } catch (Exception e) {
            log.error("[API17] 落库失败 rqi={} OrderNo={}", dto.getRqi(), dto.getOrderNo(), e);
            opLogService.recordInbound(OPERATOR_SCADA_IN, API17_FEEDBACK, dto.getOrderNo(), null, 0, reqBody, "落库失败: " + e.getMessage());
            return ScadaResponse.fail(dto.getRqi(), "落库失败: " + e.getMessage());
        }
        // 联动 loading_order(2026-08-22:SCADA 的 OrderNo 即装卸车工单号,同 E+H API15 口径)
        String link = linkOrder(dto);
        String msg = "已落库工单信息回传 OrderNo=" + dto.getOrderNo() + " scadaStatus=" + dto.getScadaStatus();
        if (!link.isEmpty()) {
            msg = msg + ";" + link;
        }
        opLogService.recordInbound(OPERATOR_SCADA_IN, API17_FEEDBACK, dto.getOrderNo(), null, 1, reqBody, msg);
        // 工单不存在/已结束:通知已留痕但联动未生效,返回 resultCode=0 让中集侧感知(同 E+H API15 口径)
        if (link.startsWith("工单不存在") || link.startsWith("工单已结束")) {
            return ScadaResponse.fail(dto.getRqi(), msg);
        }
        return ScadaResponse.ok(dto.getRqi(), msg);
    }

    /** 按 OrderNo 联动 loading_order:中集运行状态写专字段 scada_status(2026-08-26 与中集沟定,
     *  报文字段名也改为 scadaStatus:0-空闲 1-运行 7-暂停 10-异常终止 11-正常结束,原样承接),
     *  公共 status(E+H 字典)与内部 order_status 均不动——两套字典互不污染;终态8不覆盖守卫保留。 */
    private String linkOrder(WorkOrderInfoFeedbackRequest dto) {
        try {
            LoadingOrder order = loadingOrderService.getOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<LoadingOrder>()
                            .eq("order_no", dto.getOrderNo()).last("LIMIT 1"));
            if (order == null) {
                return "工单不存在: " + dto.getOrderNo();
            }
            if (order.getOrderStatus() != null && order.getOrderStatus() == ORDER_STATUS_FINISH) {
                return "工单已结束,不更新状态,仅记录";
            }
            if (dto.getScadaStatus() != null) {
                // 中集运行态字典(0/1/7/10/11)与公共状态(E+H 1/5/8)不是一套——写专字段 scada_status
                order.setScadaStatus(dto.getScadaStatus());
            }
            loadingOrderService.updateById(order);
            return "已更新工单 " + dto.getOrderNo() + " scadaStatus=" + dto.getScadaStatus();
        } catch (Exception e) {
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
