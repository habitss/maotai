package com.keytech.modules.maotai.eh.ehLoading.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.maotai.eh.ehLoading.client.EhLoadingClient;
import com.keytech.modules.maotai.eh.ehLoading.dto.CloseRequest;
import com.keytech.modules.maotai.eh.ehLoading.dto.EhResponse;
import com.keytech.modules.maotai.eh.ehLoading.dto.LoadingPlanDTO;
import com.keytech.modules.maotai.eh.ehLoading.entity.LoadingOrder;
import com.keytech.modules.maotai.eh.ehLoading.mapper.LoadingOrderMapper;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingOrderService;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 装卸车工单服务实现。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoadingOrderServiceImpl extends BaseServiceImpl<LoadingOrderMapper, LoadingOrder> implements LoadingOrderService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_PUSHED = 1;
    private static final int STATUS_FAILED = 2;
    /** 公共状态 status(对齐 E+H 字典,E+H 请求/响应写入):1-新建业务 5-装卸车结束 8-业务结束 */
    private static final int STATUS_EH_NEW = 1;
    private static final int STATUS_EH_FINISH = 8;
    /** 平台内部工单状态 order_status(全字典):1-工单推送(API04) 2-车检待校验(API08) 3-车检成功(API11) 4-车检失败(API11) 5-叫号入场(API12) 6-启动工单(API14) 7-装车/卸车结束(API15) 8-装卸车工单结束(API19,终态) 9-罐转罐工单启动(API05) 10-罐转罐工单结束(API06) */
    private static final int ORDER_STATUS_NEW = 1;
    private static final int ORDER_STATUS_FINISH = 8;
    private static final int BUSI_TYPE_UNLOAD = 2; // 采购/入库/卸车(装车=1 不传 ABV)
    private static final long UINT32_MAX = 4294967295L;
    private static final long MAX_VALID_HOURS = 48L;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EhLoadingClient client;
    private final ApiOpLogService opLogService;
    private final ObjectMapper objectMapper;

    private static final String API04_CREATE = "API04";
    private static final String API19_CLOSE = "API19";

    @Override
    public LoadingOrder pushOrder(String id) {
        LoadingOrder order = getById(id);
        if (order == null) {
            throw new JeecgBootBizTipException("工单不存在: " + id);
        }
        validateOrder(order, id); // 推送前再校验(IC 卡活跃唯一/48h 等),避免无意义推送
        LoadingPlanDTO plan = toPlan(order);
        String reqBody = toJson(plan);
        String operator = currentModifyor();
        EhResponse resp;
        try {
            resp = client.createPlan(plan);
        } catch (Exception e) {
            // getToken 等异常也按失败处理,挂重试(原逻辑无 catch,异常会直接 500)
            log.warn("[推送工单] 调 E+H 异常 orderNo={}", order.getOrderNo(), e);
            opLogService.recordPendingRetry(operator, API04_CREATE, order.getOrderNo(), null, reqBody, "推送异常: " + e.getMessage());
            order.setPushTime(new Date());
            order.setPushStatus(STATUS_FAILED);
            order.setPushMsg("推送异常: " + e.getMessage());
            updateById(order);
            return order;
        }
        order.setPushTime(new Date());
        order.setPushStatus(resp.isSuccess() ? STATUS_PUSHED : STATUS_FAILED);
        order.setPushMsg(resp.getContent());
        if (resp.isSuccess()) {
            // API04 推送成功:公共状态=1新建业务(对齐E+H);平台内部状态=1新建工单
            order.setStatus(STATUS_EH_NEW);
            order.setOrderStatus(ORDER_STATUS_NEW);
        }
        updateById(order);
        if (resp.isSuccess()) {
            opLogService.recordOutbound(operator, API04_CREATE, order.getOrderNo(), null, reqBody, resp != null && resp.isSuccess(), resp == null ? null : resp.getContent());
        } else {
            // 失败挂重试(API04 可重试接口),重试 job 成功后会回写 push_status=1
            opLogService.recordPendingRetry(operator, API04_CREATE, order.getOrderNo(), null, reqBody, resp.getContent());
        }
        return order;
    }

    /**
     * 手动结束工单(API19):平台操作员手动触发,调 E+H /api/Bussiness/Close。
     * 仅"已推"工单可结束;不传 CloseType(由 E+H 内部判断);ABV 按装卸规则;Modifyor 取当前登录用户。
     */
    @Override
    public LoadingOrder closeOrder(String id) {
        LoadingOrder order = getById(id);
        if (order == null) {
            throw new JeecgBootBizTipException("工单不存在: " + id);
        }
        if (order.getPushStatus() == null || order.getPushStatus() != STATUS_PUSHED) {
            throw new JeecgBootBizTipException("仅已推工单可结束,当前推送状态: " + order.getPushStatus());
        }
        CloseRequest close = new CloseRequest();
        close.setBusiType(order.getBusiType());
        close.setOrderNo(order.getOrderNo());
        close.setCarNo(order.getCarNo());
        close.setModifyor(currentModifyor());
        close.setModifyTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        // ABV:仅卸车(采购/入库)传值,装车(销售/出库)传空
        boolean unload = order.getBusiType() != null && order.getBusiType() == BUSI_TYPE_UNLOAD;
        close.setAbv(unload ? StrUtil.nullToEmpty(order.getAbv()) : "");
        String reqBody = toJson(close);
        String operator = currentModifyor();
        EhResponse resp;
        try {
            resp = client.closeOrder(close);
        } catch (Exception e) {
            log.warn("[结束工单] 调 E+H 异常 orderNo={}", order.getOrderNo(), e);
            opLogService.recordPendingRetry(operator, API19_CLOSE, order.getOrderNo(), null, reqBody, "结束工单异常: " + e.getMessage());
            order.setPushTime(new Date());
            order.setPushMsg("结束工单异常: " + e.getMessage());
            updateById(order);
            return order;
        }
        order.setPushTime(new Date());
        order.setPushMsg("结束工单: " + resp.getContent());
        if (resp.isSuccess()) {
            // API19 成功:公共状态=8业务结束;平台内部状态=8工单结束(终态,IC卡随之释放)
            order.setStatus(STATUS_EH_FINISH);
            order.setOrderStatus(ORDER_STATUS_FINISH);
        }
        updateById(order);
        if (resp.isSuccess()) {
            opLogService.recordOutbound(operator, API19_CLOSE, order.getOrderNo(), null, reqBody, resp != null && resp.isSuccess(), resp == null ? null : resp.getContent());
        } else {
            opLogService.recordPendingRetry(operator, API19_CLOSE, order.getOrderNo(), null, reqBody, resp.getContent());
        }
        return order;
    }

    /** 取当前登录用户真实姓名作为 Modifyor,取不到记 platform */
    public String currentModifyor() {
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal instanceof LoginUser) {
                String name = ((LoginUser) principal).getRealname();
                if (StrUtil.isNotBlank(name)) {
                    return name;
                }
            }
        } catch (Exception e) {
            log.warn("[E+H出站] 取当前用户失败,记 platform", e);
        }
        return "platform";
    }

    /** 请求体 JSON(操作日志 req_body + 重试 job 反序列化用) */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("[E+H出站] 序列化请求体失败 {}", obj.getClass().getSimpleName(), e);
            return null;
        }
    }

    // 车牌交换已停用:待确认"一车绑定一单"业务规则,确定后连同 handleSwapped 整体删除
    /*
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean swapCarNo(String id1, String id2) {
        LoadingOrder o1 = getById(id1);
        LoadingOrder o2 = getById(id2);
        if (o1 == null || o2 == null) {
            throw new JeecgBootBizTipException("工单不存在: " + (o1 == null ? id1 : id2));
        }
        // 记住 E+H 旧单的车牌(Close 时要用),然后交换
        String oldCar1 = o1.getCarNo();
        String oldCar2 = o2.getCarNo();
        o1.setCarNo(oldCar2);
        o2.setCarNo(oldCar1);
        // 按各自状态分别处理
        handleSwapped(o1, oldCar1);
        handleSwapped(o2, oldCar2);
        updateById(o1);
        updateById(o2);
        return true;
    }
    */

    @Override
    public void validateOrder(LoadingOrder order, String excludeId) {
        Integer tenantId = order.getTenantId();
        // 1) OrderNo 租户内唯一:不再 count 预查,靠 uk_tenant_order 唯一索引兜底,
        //    save/edit 时 catch DuplicateKeyException 返回"工单号已存在"(见 LoadingOrderController)
        // 2) IC 卡号(UInt32)范围 0–4294967295
        if (StrUtil.isNotBlank(order.getValidate())) {
            long ic;
            try {
                ic = Long.parseLong(order.getValidate());
            } catch (NumberFormatException e) {
                throw new JeecgBootBizTipException("IC 卡号必须是数字: " + order.getValidate());
            }
            if (ic < 0 || ic > UINT32_MAX) {
                throw new JeecgBootBizTipException("IC 卡号需在 0~4294967295 之间: " + order.getValidate());
            }
        }
        // 3) IC 卡号业务结束前不可重复(活跃=已推且平台内部状态≠8工单结束;order_status=8 的单不占卡)
        if (StrUtil.isNotBlank(order.getValidate())) {
            QueryWrapper<LoadingOrder> qw = new QueryWrapper<LoadingOrder>()
                    .eq("validate", order.getValidate())
                    .eq("push_status", STATUS_PUSHED)
                    .and(w -> w.ne("order_status", ORDER_STATUS_FINISH).or().isNull("order_status"));
            if (tenantId != null) {
                qw.eq("tenant_id", tenantId);
            }
            if (StrUtil.isNotBlank(excludeId)) {
                qw.ne("id", excludeId);
            }
            if (this.count(qw) > 0) {
                throw new JeecgBootBizTipException("IC 卡号 " + order.getValidate() + " 存在未结束的已推工单,业务结束前不可重复");
            }
        }
        // 4) 罐号按业务类型条件必填(2026-08-27):装车(1/销售出库)必填来源罐 TankNameBefore;
        //    卸车(2/采购入库)必填目标罐 TankName——无差别校验会逼装车单填不存在的目标罐,反之亦然
        boolean unload = order.getBusiType() != null && order.getBusiType() == BUSI_TYPE_UNLOAD;
        if (!unload && StrUtil.isBlank(order.getTankNameBefore())) {
            throw new JeecgBootBizTipException("装车工单必须填写来源罐(TankNameBefore)");
        }
        if (unload && StrUtil.isBlank(order.getTankName())) {
            throw new JeecgBootBizTipException("卸车工单必须填写预目标罐(TankName)");
        }
        // 5) 有效期:EndDate 晚于 BeginDate,且 ≤ 48 小时
        if (StrUtil.isNotBlank(order.getValidateBeginDate()) && StrUtil.isNotBlank(order.getValidateEndDate())) {
            LocalDateTime b;
            LocalDateTime e;
            try {
                b = LocalDateTime.parse(order.getValidateBeginDate(), DT_FMT);
                e = LocalDateTime.parse(order.getValidateEndDate(), DT_FMT);
            } catch (Exception ex) {
                throw new JeecgBootBizTipException("时间格式应为 yyyy-MM-dd HH:mm:ss");
            }
            if (!e.isAfter(b)) {
                throw new JeecgBootBizTipException("工单过期时间需晚于计划启动时间");
            }
            long hours = Duration.between(b, e).toHours();
            if (hours > MAX_VALID_HOURS) {
                throw new JeecgBootBizTipException("有效期超过 48 小时(当前 " + hours + " 小时)");
            }
        }
    }

    // 车牌交换已停用(待确认"一车绑定一单"后整体删除)
    // /**
    //  * 处理交换后的单个工单(order.carNo 已是新值,oldCar 是 E+H 旧单记录的车牌)。
    //  * 已推 → Close 作废 + 新 OrderNo 重新 Create;待推/失败 → 只改 MES,不碰 E+H。
    //  */
    /*
    private void handleSwapped(LoadingOrder order, String oldCar) {
        Integer st = order.getPushStatus();
        if (st != null && st == STATUS_PUSHED) {
            String oldOrderNo = order.getOrderNo();
            CloseRequest close = new CloseRequest();
            close.setBusiType(order.getBusiType());
            close.setOrderNo(oldOrderNo);
            close.setCarNo(oldCar);
            close.setModifyor("mes-swap");
            close.setModifyTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            // ABV:仅卸车(采购/入库)传值,装车(销售/出库)传空
            boolean unload = order.getBusiType() != null && order.getBusiType() == BUSI_TYPE_UNLOAD;
            close.setAbv(unload ? StrUtil.nullToEmpty(order.getAbv()) : "");
            close.setCloseType(CLOSE_TYPE_CANCEL);
            EhResponse closeResp = client.closeOrder(close);
            if (!closeResp.isSuccess()) {
                log.warn("[交换] Close 旧单返回非成功 orderNo={} resp={}", oldOrderNo, closeResp.getContent());
            }
            String newOrderNo = buildNewOrderNo(oldOrderNo);
            order.setOrderNo(newOrderNo);
            EhResponse createResp = client.createPlan(toPlan(order));
            order.setPushTime(new Date());
            if (createResp.isSuccess()) {
                order.setPushStatus(STATUS_PUSHED);
                order.setPushMsg("交换重推成功,新单号 " + newOrderNo);
            } else {
                order.setPushStatus(STATUS_FAILED);
                order.setPushMsg("交换重推失败(Close=" + closeResp.getContent() + " | Create=" + createResp.getContent() + ")");
            }
        } else {
            order.setPushMsg("已交换车牌,待重新推送");
        }
    }
    */

    private LoadingPlanDTO toPlan(LoadingOrder o) {
        LoadingPlanDTO p = new LoadingPlanDTO();
        BeanUtils.copyProperties(o, p);
        // ABV:仅卸车(采购/入库)传值,装车(销售/出库)传空
        boolean unload = o.getBusiType() != null && o.getBusiType() == BUSI_TYPE_UNLOAD;
        p.setAbv(unload ? StrUtil.nullToEmpty(o.getAbv()) : "");
        // 罐号如无推空串 ""(2026-08-27 与中集对齐:JSON null 中集 .NET 报 wrong data type)
        p.setTankNameBefore(StrUtil.nullToEmpty(o.getTankNameBefore()));
        p.setTankName(StrUtil.nullToEmpty(o.getTankName()));
        return p;
    }

    /** 生成新 OrderNo:原号(截断) + "SW" + 6 位,总长 ≤ 50 */
    private String buildNewOrderNo(String old) {
        String suffix = "SW" + (System.currentTimeMillis() % 1_000_000L);
        int max = 50 - suffix.length();
        String base = old.length() > max ? old.substring(0, max) : old;
        return base + suffix;
    }
}
