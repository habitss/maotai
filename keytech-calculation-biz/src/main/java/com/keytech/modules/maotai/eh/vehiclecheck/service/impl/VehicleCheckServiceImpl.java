package com.keytech.modules.maotai.eh.vehiclecheck.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.common.api.ApiResponse;
import com.keytech.modules.maotai.eh.ehLoading.client.EhLoadingClient;
import com.keytech.modules.maotai.eh.ehLoading.dto.EhResponse;
import com.keytech.modules.maotai.eh.ehLoading.entity.LoadingOrder;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingOrderService;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;
import com.keytech.modules.maotai.oplog.service.impl.ApiOpLogServiceImpl;
import com.keytech.modules.maotai.eh.vehiclecheck.dto.VehicleCheckDTO;
import com.keytech.modules.maotai.eh.vehiclecheck.dto.VehicleCheckItemDTO;
import com.keytech.modules.maotai.eh.vehiclecheck.dto.VehicleCheckResultRequest;
import com.keytech.modules.maotai.eh.vehiclecheck.entity.VehicleCheck;
import com.keytech.modules.maotai.eh.vehiclecheck.entity.VehicleCheckItem;
import com.keytech.modules.maotai.eh.vehiclecheck.mapper.VehicleCheckItemMapper;
import com.keytech.modules.maotai.eh.vehiclecheck.mapper.VehicleCheckMapper;
import com.keytech.modules.maotai.eh.vehiclecheck.service.VehicleCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 车检信息服务实现。
 * API08 入站:幂等靠 uk_check_no 唯一索引 + catch DuplicateKeyException(原 count 预查已取消,并发安全);
 *   首次成功→落库(含明细)+联动工单+flag=1 触发 API11;重复→fail"重复提交",不触发明细/联动/API11。
 * API11 出站:单独回传(ModifyType 固定 5,ModifyValue 由调用方给),调 client.sendVehicleCheckResult,
 *   超时/异常降级返回 fail;失败挂重试(op_log retry_status=0)。
 * 全部调用记操作日志(mes3_maotai_api_op_log):入站 operator=E+H,自动触发 API11 operator=system,手动=登录用户。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Service
@RequiredArgsConstructor
public class VehicleCheckServiceImpl extends BaseServiceImpl<VehicleCheckMapper, VehicleCheck> implements VehicleCheckService {

    /** 0-待推 1-已推 2-推送失败 */
    private static final int STATUS_PUSHED = 1;
    private static final int CHECK_TYPE_IN = 1;
    private static final int CHECK_TYPE_OUT = 2;
    private static final int BIZ_IN = 1;
    private static final int BIZ_OUT = 2;
    /** flag:1-直接走 API11 */
    private static final int FLAG_TRIGGER_API11 = 1;
    /** API11 的 ModifyType 固定值 5 */
    private static final int MODIFY_TYPE_FIXED = 5;
    /** 平台内部工单状态 order_status(按业务顺序):2-车检待校验(API08联动置,MES校验回路未建前的中间态) 3-车检成功(API11回传动作发出即置,默认成功) 4-车检失败(预留,MES回路接入后写) 8-装卸车工单结束(终态) */
    private static final int ORDER_STATUS_CHECK_PENDING = 2;
    private static final int ORDER_STATUS_CHECK_PASS = 3;
    private static final int ORDER_STATUS_CHECK_FAIL = 4;
    private static final int ORDER_STATUS_FINISH = 8;
    /** API11 的 ModifyValue 默认值 "1"(车检通过)——API08 报文未带 ModifyValue 时兜底,后续逻辑变更再调 */
    private static final String MODIFY_VALUE_DEFAULT = "1";

    private static final String API08_UPLOAD = "API08";
    private static final String API11_VEHICLE_CHECK = "API11";

    private final LoadingOrderService loadingOrderService;
    private final EhLoadingClient ehLoadingClient;
    private final VehicleCheckItemMapper vehicleCheckItemMapper;
    private final ApiOpLogService opLogService;
    private final ObjectMapper objectMapper;

    @Override
    public ApiResponse receive(VehicleCheckDTO dto) {
        String reqBody = toJson(dto);
        // 落车检记录:幂等靠 uk_check_no 唯一索引(原 count 预查已取消),重复 catch 后直接返回,
        // 不再落明细/联动/触发 API11
        VehicleCheck vehicleCheck = new VehicleCheck();
        BeanUtils.copyProperties(dto, vehicleCheck);
        try {
            this.save(vehicleCheck);
        } catch (DuplicateKeyException e) {
            opLogService.recordInbound(ApiOpLogService.OPERATOR_INBOUND, API08_UPLOAD, dto.getOrderNo(), dto.getCheckNo(), 0, reqBody,
                    "重复提交:车检流水号已存在 " + dto.getCheckNo());
            return ApiResponse.fail("重复提交:车检流水号 " + dto.getCheckNo() + " 已存在");
        }
        // 落检查项明细
        int itemCount = saveCheckItems(dto);
        // 联动工单业务状态
        String linkMsg = linkOrder(dto);
        // flag=1 时触发 API11(ModifyValue 透传)
        String ehMsg = triggerApi11IfFlag(dto);
        opLogService.recordInbound(ApiOpLogService.OPERATOR_INBOUND, API08_UPLOAD, dto.getOrderNo(), dto.getCheckNo(), 1, reqBody,
                "成功;" + linkMsg + ";" + ehMsg + ";" + itemCount + "项明细");
        return ApiResponse.ok("成功;" + linkMsg + ";" + ehMsg + ";" + itemCount + "项明细");
    }

    @Override
    public ApiResponse sendResultToEh(VehicleCheckResultRequest req) {
        if (req.getModifyType() == null) {
            req.setModifyType(MODIFY_TYPE_FIXED);
        }
        if (StrUtil.isBlank(req.getModifyTime())) {
            req.setModifyTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        }
        String reqBody = toJson(req);
        // ModifyValue 不默认,透传调用方给的(判断通过与否交给 E+H)
        String operator = req.getModifyor() != null ? req.getModifyor() : loadingOrderService.currentModifyor();
        try {
            EhResponse resp = ehLoadingClient.sendVehicleCheckResult(req);
            recordApi11(operator, req.getOrderNo(), reqBody, resp);
            // 车检结论=回传动作完成即成功(默认口径),E+H 响应不改结论
            markCheckPassed(req.getOrderNo());
            return resp.isSuccess() ? ApiResponse.ok("已回传 E+H: " + resp.getContent())
                    : ApiResponse.fail("E+H 返回: " + resp.getContent());
        } catch (Exception e) {
            opLogService.recordPendingRetry(operator, API11_VEHICLE_CHECK, req.getOrderNo(), null, reqBody,
                    "回传 E+H 异常: " + e.getMessage());
            return ApiResponse.fail("回传 E+H 异常: " + e.getMessage());
        }
    }

    /** 记 API11 出站结果:成功记审计,失败挂重试 */
    private void recordApi11(String operator, String orderNo, String reqBody, EhResponse resp) {
        if (resp != null && resp.isSuccess()) {
            opLogService.recordOutbound(operator, API11_VEHICLE_CHECK, orderNo, null, reqBody, resp != null && resp.isSuccess(), resp == null ? null : resp.getContent());
        } else {
            opLogService.recordPendingRetry(operator, API11_VEHICLE_CHECK, orderNo, null, reqBody,
                    resp == null ? "无响应" : resp.getContent());
        }
    }

    /** flag=1(默认)时触发 API11,ModifyValue 从 dto 透传(不写死 1);自动触发记 operator=system */
    private String triggerApi11IfFlag(VehicleCheckDTO dto) {
        Integer flag = dto.getFlag() == null ? FLAG_TRIGGER_API11 : dto.getFlag();
        if (flag != FLAG_TRIGGER_API11) {
            return "flag=" + flag + ",未触发 API11";
        }
        try {
            VehicleCheckResultRequest r = new VehicleCheckResultRequest();
            r.setBusiType(dto.getBusiType());
            r.setOrderNo(dto.getOrderNo());
            r.setCarNo(dto.getCarNo());
            // API08 联动触发属自动操作,operator 记 system(手动入口 /api/VehicleCheck/Send 走 currentModifyor)
            r.setModifyor(ApiOpLogServiceImpl.OPERATOR_SYSTEM);
            r.setModifyType(MODIFY_TYPE_FIXED);
            // ModifyValue:优先透传 API08 报文里的值;联调阶段 E+H 未传,默认 "1"(通过)——
            // 车检通过才允许继续装车,后续逻辑变更(如按明细推导)再调整这里
            Integer mv = dto.getModifyValue();
            r.setModifyValue(mv == null ? MODIFY_VALUE_DEFAULT : String.valueOf(mv));
            // API11 回传动作发出即视为车检确认完成 → 置 3-车检成功(默认成功口径,不受E+H响应影响)
            markCheckPassed(r.getOrderNo());
            return sendResultToEh(r).getContent();
        } catch (Exception e) {
            return "触发 API11 异常: " + e.getMessage();
        }
    }

    /** 置 3-车检成功(仅 2/NULL 的单;终态不覆盖)。API11 联动触发与手动入口共用。 */
    private void markCheckPassed(String orderNo) {
        try {
            LoadingOrder order = loadingOrderService.getOne(new QueryWrapper<LoadingOrder>()
                    .eq("order_no", orderNo).last("LIMIT 1"));
            if (order == null || (order.getOrderStatus() != null && order.getOrderStatus() == ORDER_STATUS_FINISH)) {
                return;
            }
            if (order.getOrderStatus() == null || order.getOrderStatus() == ORDER_STATUS_CHECK_PENDING) {
                order.setOrderStatus(ORDER_STATUS_CHECK_PASS);
                loadingOrderService.updateById(order);
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(VehicleCheckServiceImpl.class)
                    .warn("[车检] 置车检成功状态失败 orderNo={}", orderNo, e);
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }

    /** 落检查项明细(关联 checkNo) */
    private int saveCheckItems(VehicleCheckDTO dto) {
        List<VehicleCheckItemDTO> items = dto.getCheckItems();
        if (items == null || items.isEmpty()) {
            return 0;
        }
        int n = 0;
        for (VehicleCheckItemDTO item : items) {
            VehicleCheckItem it = new VehicleCheckItem();
            it.setCheckNo(dto.getCheckNo());
            it.setItemName(item.getItemName());
            it.setPassFlag(item.getPassFlag());
            it.setRemark(item.getRemark());
            vehicleCheckItemMapper.insert(it);
            n++;
        }
        return n;
    }

    /** 按 carNo 找最近一条已推工单,按 checkType 更新 business_status */
    private String linkOrder(VehicleCheckDTO dto) {
        if (dto.getCheckType() == null) {
            return "checkType 为空,未联动";
        }
        Integer bizStatus;
        if (dto.getCheckType() == CHECK_TYPE_IN) {
            bizStatus = BIZ_IN;
        } else if (dto.getCheckType() == CHECK_TYPE_OUT) {
            bizStatus = BIZ_OUT;
        } else {
            return "未识别 checkType=" + dto.getCheckType() + ",未联动";
        }
        LoadingOrder order = loadingOrderService.getOne(new QueryWrapper<LoadingOrder>()
                .eq("car_no", dto.getCarNo())
                .eq("push_status", STATUS_PUSHED) //已推订单
                .and(w -> w.ne("order_status", ORDER_STATUS_FINISH).or().isNull("order_status")) //未结束(已结束单不再联动,车检迟到不覆盖终态)
                .orderByDesc("create_time")
                .last("LIMIT 1"));
        if (order == null) {
            return "未找到车牌 " + dto.getCarNo() + " 的已推工单,未联动";
        }
        order.setBusinessStatus(bizStatus);
        // API08 收到的是未经勾储MES校验的原始车检信息 → 先置 2-车检结果待校验;
        // 随后 flag=1 联动调 API11(回传动作)时再置 3-车检成功(默认成功口径,MES回路接入后由校验结果改写)
        order.setOrderStatus(ORDER_STATUS_CHECK_PENDING);
        loadingOrderService.updateById(order);
        return "已更新工单 " + order.getOrderNo() + " 业务状态=" + bizStatus;
    }
}
