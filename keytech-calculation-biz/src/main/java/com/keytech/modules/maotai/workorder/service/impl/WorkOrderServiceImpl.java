package com.keytech.modules.maotai.workorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.maotai.equipment.entity.EquipmentLedger;
import com.keytech.modules.maotai.equipment.mapper.EquipmentLedgerMapper;
import com.keytech.modules.maotai.scada.dto.ScadaResponse;
import com.keytech.modules.maotai.scada.dto.WorkOrderStartRequest;
import com.keytech.modules.maotai.scada.dto.WorkOrderSyncLoadRequest;
import com.keytech.modules.maotai.scada.service.ScadaWorkOrderService;
import com.keytech.modules.maotai.workorder.entity.TransferRoute;
import com.keytech.modules.maotai.workorder.entity.WorkOrder;
import com.keytech.modules.maotai.workorder.entity.WorkOrderLog;
import com.keytech.modules.maotai.workorder.mapper.TransferRouteMapper;
import com.keytech.modules.maotai.workorder.mapper.WorkOrderLogMapper;
import com.keytech.modules.maotai.workorder.mapper.WorkOrderMapper;
import com.keytech.modules.maotai.workorder.service.WorkOrderService;
import com.keytech.modules.maotai.workorder.vo.TransferPathVO;
import com.keytech.modules.maotai.workorder.vo.WorkOrderStatisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工单任务服务实现
 * <p>
 * 状态机:pending --start--> running --pause--> paused --resume--> running;
 * running/paused --stop--> abandoned;任意 --delete--> abandoned;abandoned 终态。
 * 每个人工操作记 work_order_log(操作人=登录 realname)。
 *
 * @author workorder
 * @since 2026-09-15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkOrderServiceImpl extends BaseServiceImpl<WorkOrderMapper, WorkOrder> implements WorkOrderService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final List<String> ALL_STATUS = Arrays.asList("pending", "running", "paused", "completed", "abandoned");

    private final WorkOrderLogMapper logMapper;
    private final TransferRouteMapper transferRouteMapper;
    private final EquipmentLedgerMapper equipmentLedgerMapper;
    private final ScadaWorkOrderService scadaService;

    @Override
    public List<WorkOrder> listValid() {
        return this.list(new QueryWrapper<WorkOrder>().eq("del_flag", 0));
    }

    @Override
    public WorkOrderStatisticsVO statistics(String workshopCode, String status) {
        List<String> statuses = status == null || status.isEmpty()
                ? List.of() : Arrays.asList(status.split(","));
        WorkOrderStatisticsVO vo = new WorkOrderStatisticsVO();
        // 状态计数:按当前车间过滤(车间为空=全车间)
        Map<String, Long> statusCount = new HashMap<>();
        for (String s : ALL_STATUS) {
            statusCount.put(s, this.count(new QueryWrapper<WorkOrder>().eq("del_flag", 0)
                    .eq(workshopCode != null && !workshopCode.isEmpty(), "workshop_code", workshopCode)
                    .eq("status", s)));
        }
        vo.setStatusCount(statusCount);
        // 车间计数:按当前状态过滤(状态为空=全状态;逗号多值 IN)
        Map<String, Long> workshopCount = new HashMap<>();
        for (String ws : Arrays.asList("ZZG", "XLG", "CPG", "GWG")) {
            workshopCount.put(ws, this.count(new QueryWrapper<WorkOrder>().eq("del_flag", 0)
                    .in(!statuses.isEmpty(), "status", statuses)
                    .eq("workshop_code", ws)));
        }
        vo.setWorkshopCount(workshopCount);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder createOrder(WorkOrder order, String operator) {
        validateForCreate(order);
        // 工单号是否系统生成:冲突时自动重取重试;人工指定(接口直调)冲突则报错
        boolean autoNo = order.getOrderNo() == null || order.getOrderNo().isEmpty();
        if (autoNo) {
            order.setOrderNo(nextOrderNo(order.getProcessName()));
        }
        // 批次号:一工单对应一个批次,留空则按阶段自动生成(人工指定尊重原值)
        if (order.getBatchNo() == null || order.getBatchNo().isEmpty()) {
            order.setBatchNo(nextBatchNo(order));
        }
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(WorkOrder.STATUS_PENDING).setDelFlag(0).setCreateTime(now).setCreateBy(operator);
        try {
            this.save(order);
        } catch (DuplicateKeyException e) {
            // uk_order_no 唯一索引兜底:并发建单撞号(或人工号重复)走到这里
            if (!autoNo) {
                throw new IllegalArgumentException("工单号已存在: " + order.getOrderNo());
            }
            order.setId(null);
            order.setOrderNo(nextOrderNo(order.getProcessName()));
            this.save(order);
        }
        writeLog(order.getOrderNo(), operator, "创建工单",
                "新建" + order.getProcessName() + "工单，源" + nvl(order.getSourceTank()) + "→目标" + nvl(order.getTargetTank()),
                WorkOrder.STATUS_PENDING);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WorkOrder> createBatch(WorkOrder common, List<WorkOrder> rows, String operator) {
        List<WorkOrder> created = new ArrayList<>();
        for (WorkOrder row : rows) {
            WorkOrder order = new WorkOrder();
            order.setProcessName(common.getProcessName()).setWorkshopCode(common.getWorkshopCode())
                    .setPlanDate(common.getPlanDate()).setPlanStartTime(common.getPlanStartTime())
                    .setPlanEndTime(common.getPlanEndTime()).setRemark(common.getRemark())
                    .setTransferPath(row.getTransferPath())
                    .setSourceTank(row.getSourceTank()).setTargetTank(row.getTargetTank())
                    .setSourceBrand(row.getSourceBrand()).setTargetBrand(row.getTargetBrand())
                    .setAmount(row.getAmount());
            created.add(createOrder(order, operator));
            // 批量与单个的日志区分
        }
        return created;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder editOrder(String id, WorkOrder patch, String operator) {
        WorkOrder order = this.getById(id);
        if (order == null || order.getDelFlag() != 0) {
            throw new IllegalArgumentException("工单不存在");
        }
        if (!WorkOrder.STATUS_PENDING.equals(order.getStatus())) {
            throw new IllegalArgumentException("仅待执行工单可编辑");
        }
        // 可编辑字段白名单(工艺/车间/工单号不可改);转酒路径经 applyRoute 校验并带出名称/撬
        order.setSourceTank(patch.getSourceTank()).setSourceBrand(patch.getSourceBrand())
                .setTargetTank(patch.getTargetTank()).setTargetBrand(patch.getTargetBrand())
                .setTransferPath(patch.getTransferPath())
                .setAmount(patch.getAmount())
                .setEmptySource(patch.getEmptySource())
                .setPlanStartTime(patch.getPlanStartTime()).setPlanEndTime(patch.getPlanEndTime())
                .setPlateNo(patch.getPlateNo()).setDriverName(patch.getDriverName())
                .setContactPhone(patch.getContactPhone())
                .setAlcohol(patch.getAlcohol())
                .setCurrentVehicleQty(patch.getCurrentVehicleQty())
                .setRemark(patch.getRemark())
                .setUpdateTime(LocalDateTime.now()).setUpdateBy(operator);
        applyRoute(order);
        this.updateById(order);
        writeLog(order.getOrderNo(), operator, "编辑工单", "编辑" + order.getProcessName() + "工单信息", order.getStatus());
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder transition(String id, String action, String operator) {
        WorkOrder order = this.getById(id);
        if (order == null || order.getDelFlag() != 0) {
            throw new IllegalArgumentException("工单不存在");
        }
        String from = order.getStatus();
        LocalDateTime now = LocalDateTime.now();
        switch (action) {
            case "start":
                require(from, WorkOrder.STATUS_PENDING, "仅待执行工单可启动");
                // 防错机制(参数字典 API14/API05):必填不全不能启动;先推中集,成功才置执行中
                pushScadaOnStart(order, operator);
                order.setStatus(WorkOrder.STATUS_RUNNING).setStartTime(now).setOperator(operator);
                writeLog(order.getOrderNo(), operator, "状态变更",
                        order.getProcessName() + "工单由【待执行】变更为【执行中】，操作人：" + operator, WorkOrder.STATUS_RUNNING);
                break;
            case "pause":
                require(from, WorkOrder.STATUS_RUNNING, "仅执行中工单可暂停");
                order.setStatus(WorkOrder.STATUS_PAUSED);
                writeLog(order.getOrderNo(), operator, "状态变更",
                        order.getProcessName() + "工单已暂停，操作人：" + operator, WorkOrder.STATUS_PAUSED);
                break;
            case "resume":
                require(from, WorkOrder.STATUS_PAUSED, "仅已暂停工单可恢复");
                order.setStatus(WorkOrder.STATUS_RUNNING);
                writeLog(order.getOrderNo(), operator, "状态变更",
                        order.getProcessName() + "工单已恢复执行，操作人：" + operator, WorkOrder.STATUS_RUNNING);
                break;
            case "complete":
                require(from, WorkOrder.STATUS_RUNNING, "仅执行中工单可完成");
                order.setStatus(WorkOrder.STATUS_COMPLETED).setEndTime(now);
                writeLog(order.getOrderNo(), operator, "状态变更",
                        order.getProcessName() + "工单由【执行中】变更为【已完成】，操作人：" + operator, WorkOrder.STATUS_COMPLETED);
                break;
            case "stop":
                require(from, WorkOrder.STATUS_RUNNING, WorkOrder.STATUS_PAUSED, "仅执行中/已暂停工单可停止");
                order.setStatus(WorkOrder.STATUS_ABANDONED).setEndTime(now)
                        .setAbandonType("停止").setAbandonUser(operator).setAbandonTime(now.format(FMT));
                writeLog(order.getOrderNo(), operator, "工单废弃",
                        order.getProcessName() + "工单由【" + statusText(from) + "】强制停止，操作人：" + operator, WorkOrder.STATUS_ABANDONED);
                break;
            case "delete":
                order.setStatus(WorkOrder.STATUS_ABANDONED)
                        .setAbandonType("删除").setAbandonUser(operator).setAbandonTime(now.format(FMT));
                writeLog(order.getOrderNo(), operator, "工单废弃",
                        order.getProcessName() + "工单已删除，操作人：" + operator, WorkOrder.STATUS_ABANDONED);
                break;
            default:
                throw new IllegalArgumentException("不支持的操作: " + action);
        }
        order.setUpdateTime(now).setUpdateBy(operator);
        this.updateById(order);
        return order;
    }

    @Override
    public List<TransferPathVO> transferPaths(String workshopCode, String processName, String sourceTank, String targetTank) {
        List<TransferPathVO> paths = new ArrayList<>();
        // 候选全部来自 mes3_maotai_transfer_route 配置表,无代码兜底:未配置即无候选
        QueryWrapper<TransferRoute> qw = new QueryWrapper<TransferRoute>()
                .eq("workshop_code", workshopCode == null ? "" : workshopCode)
                .eq("process_type", processName == null ? "" : processName)
                .eq("enabled", 1)
                .eq("del_flag", 0);
        if (WorkOrder.PROCESS_UNLOAD.equals(processName)) {
            // 卸车:槽车→撬→目标罐,按目标罐过滤
            qw.eq("target_tank", targetTank == null ? "" : targetTank);
        } else if (WorkOrder.PROCESS_LOAD.equals(processName)) {
            // 装车:源罐→撬→槽车,按源罐过滤
            qw.eq("source_tank", sourceTank == null ? "" : sourceTank);
        } else {
            qw.eq("source_tank", sourceTank == null ? "" : sourceTank)
              .eq("target_tank", targetTank == null ? "" : targetTank);
        }
        qw.orderByAsc("sort_no").orderByAsc("route_code");
        for (TransferRoute r : transferRouteMapper.selectList(qw)) {
            paths.add(new TransferPathVO()
                    .setCode(r.getRouteCode())
                    .setName(r.getRouteName() + "（" + r.getRouteCode() + "）")
                    .setLineName(r.getRouteName())
                    .setSkidCode(r.getSkidCode()));
        }
        return paths;
    }

    @Override
    public List<Map<String, String>> listTanks(String workshopCode) {
        List<Map<String, String>> tanks = new ArrayList<>();
        QueryWrapper<EquipmentLedger> qw = new QueryWrapper<EquipmentLedger>()
                .isNotNull("tank_config_id")
                .eq("del_flag", 0)
                .eq(workshopCode != null && !workshopCode.isEmpty(), "workshop_code", workshopCode)
                .orderByAsc("equip_name");
        for (EquipmentLedger row : equipmentLedgerMapper.selectList(qw)) {
            Map<String, String> m = new HashMap<>();
            m.put("label", row.getEquipName());
            m.put("value", row.getEquipName());
            tanks.add(m);
        }
        return tanks;
    }

    // ===== 内部方法 =====

    /**
     * 点启动按工艺推中集自控(字典防错机制:必填不全不能启动工单):
     * 卸车/装车→API14 装卸车同步;倒罐→API05 启动工单(罐转罐);灌装不推。
     * 推送失败抛异常回滚(工单留在待执行),由 API05/14 挂账机制 3次×60s 自动重发。
     */
    private void pushScadaOnStart(WorkOrder order, String operator) {
        String process = order.getProcessName();
        String rqi = UUID.randomUUID().toString();
        if (WorkOrder.PROCESS_UNLOAD.equals(process) || WorkOrder.PROCESS_LOAD.equals(process)) {
            WorkOrderSyncLoadRequest req = new WorkOrderSyncLoadRequest();
            // 字典必填:rqi/orderNo/busiType/CarNo/PreWeight/ProductName/syncTime/TankNameBefore|TankName
            req.setRqi(rqi);
            req.setOrderNo(order.getOrderNo());
            req.setBusiType(WorkOrder.PROCESS_UNLOAD.equals(process) ? 2 : 1);
            req.setCarNo(requireField(order.getPlateNo(), "车牌号"));
            Double preWeight = order.getAmount() == null ? null : order.getAmount() * 1000;
            requireField(preWeight, "输送量(吨)");
            req.setPreWeight(BigDecimal.valueOf(preWeight));
            String product = WorkOrder.PROCESS_UNLOAD.equals(process) ? order.getTargetBrand() : order.getSourceBrand();
            String productLabel = WorkOrder.PROCESS_UNLOAD.equals(process) ? "目标品牌" : "源品牌";
            req.setProductName(requireField(product, productLabel));
            req.setTankNameBefore(WorkOrder.PROCESS_LOAD.equals(process) ? order.getSourceTank() : "");
            req.setTankName(WorkOrder.PROCESS_UNLOAD.equals(process) ? order.getTargetTank() : "");
            req.setDriverName(order.getDriverName());
            req.setValidate("");
            req.setSyncTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
            ScadaResponse resp = scadaService.syncLoadPayload(req, operator);
            requirePushSuccess(resp, "API14 装卸车同步");
        } else if ("倒罐工艺".equals(process)) {
            WorkOrderStartRequest req = new WorkOrderStartRequest();
            // 字典必填:workOrderNo/currentVehicleQty/targetTankCode/transferSetpoint/alcoholContent/loadingPath/sourceTankCode/operatorName/rqi
            req.setRqi(rqi);
            req.setWorkOrderNo(order.getOrderNo());
            req.setSourceTankCode(requireField(order.getSourceTank(), "源罐"));
            req.setTargetTankCode(requireField(order.getTargetTank(), "目标罐"));
            req.setCurrentVehicleQty(requireField(order.getCurrentVehicleQty(), "当前车载酒数量"));
            requireField(order.getAmount(), "输送量(吨)");
            req.setTransferSetpoint(String.valueOf(order.getAmount()));
            requireField(order.getAlcohol(), "酒精度");
            req.setAlcoholContent(String.valueOf(order.getAlcohol()));
            req.setLoadingPath(requireField(order.getTransferPath(), "转酒路径"));
            req.setOperatorName(operator);
            ScadaResponse resp = scadaService.startWorkOrderPayload(req, operator);
            requirePushSuccess(resp, "API05 启动工单");
        }
        // 灌装工艺:暂不推中集
    }

    private <T> T requireField(T v, String label) {
        if (v == null || (v instanceof String && ((String) v).isEmpty())) {
            throw new IllegalArgumentException("无法启动工单:请先完善「" + label + "」(中集接口必填)");
        }
        return v;
    }

    private void requirePushSuccess(ScadaResponse resp, String apiLabel) {
        if (resp == null || !resp.isSuccess()) {
            throw new IllegalArgumentException(apiLabel + "失败: " + (resp == null ? "无响应" : resp.getResultDesp())
                    + "(工单保持待执行,失败已挂账自动重试)");
        }
    }

    private void validateForCreate(WorkOrder order) {
        if (order.getProcessName() == null || order.getProcessName().isEmpty()) {
            throw new IllegalArgumentException("请选择工艺名称");
        }
        // 卸车=外部来酒无源罐;装车=外部去向无目标罐
        if (!WorkOrder.PROCESS_UNLOAD.equals(order.getProcessName())
                && (order.getSourceTank() == null || order.getSourceTank().isEmpty())) {
            throw new IllegalArgumentException("请填写源罐");
        }
        if (!WorkOrder.PROCESS_LOAD.equals(order.getProcessName())
                && (order.getTargetTank() == null || order.getTargetTank().isEmpty())) {
            throw new IllegalArgumentException("请填写目标罐");
        }
        applyRoute(order);
        Long dup = this.count(new QueryWrapper<WorkOrder>().eq("order_no", order.getOrderNo()).eq("del_flag", 0));
        if (dup != null && dup > 0) {
            throw new IllegalArgumentException("工单号已存在: " + order.getOrderNo());
        }
    }

    /**
     * 转酒路径必填+合法性校验:所选 route_code 必须在配置表当前候选内(防绕过 UI 提交错路径),
     * 并把配置表的路径名称/卸车撬冗余写入工单。
     */
    private void applyRoute(WorkOrder order) {
        if (order.getTransferPath() == null || order.getTransferPath().isEmpty()) {
            throw new IllegalArgumentException("请选择转酒路径");
        }
        List<TransferPathVO> candidates = transferPaths(order.getWorkshopCode(), order.getProcessName(),
                order.getSourceTank(), order.getTargetTank());
        TransferPathVO hit = null;
        for (TransferPathVO p : candidates) {
            if (order.getTransferPath().equals(p.getCode())) {
                hit = p;
                break;
            }
        }
        if (hit == null) {
            throw new IllegalArgumentException("转酒路径无效(未配置或不匹配当前罐): " + order.getTransferPath());
        }
        order.setTransferPathName(hit.getLineName());
        order.setSkidCode(hit.getSkidCode());
    }

    /** 工艺代号(工单号组成部分):卸车XC 装车ZC 倒罐DG 灌装GZ */
    private static final Map<String, String> PROCESS_CODES = Map.of(
            "卸车工艺", "XC", "装车工艺", "ZC", "倒罐工艺", "DG", "灌装工艺", "GZ");

    /**
     * 工单号 WO-{工艺码}{yyMM}-nnnn(如 倒罐工艺 2026-09 第 3 单 → WO-DG2609-0003):
     * 工艺+月份拼入单号,序号按"工艺×月"独立递增。
     * <p>FOR UPDATE 锁定读(读已提交最新值,不受事务快照影响):并发建单时后到的事务在此等前行锁,
     * 前一事务提交后才能读到新最大号——两单必得不同号;配合 uk_order_no 唯一索引双保险。
     */
    private String nextOrderNo(String processName) {
        String code = PROCESS_CODES.getOrDefault(processName == null ? "" : processName, "GN");
        String prefix = "WO-" + code + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMM")) + "-";
        WorkOrder latest = this.getOne(new QueryWrapper<WorkOrder>()
                .likeRight("order_no", prefix).orderByDesc("order_no").last("LIMIT 1 FOR UPDATE"));
        int next = 1;
        if (latest != null && latest.getOrderNo() != null) {
            try {
                next = Integer.parseInt(latest.getOrderNo().substring(prefix.length())) + 1;
            } catch (NumberFormatException ignore) {
                // 非法尾号从 1 重新计
            }
        }
        return prefix + String.format("%04d", next);
    }

    /**
     * 批次阶段码(批次号组成部分,与追溯页阶段口径一致):
     * 卸车XC 装车ZC 灌装GZ;倒罐按车间分中转ZZ/勾调GD/成品CP,其余倒罐DG;未知工艺GN。
     */
    private String stageCode(WorkOrder order) {
        String p = order.getProcessName();
        if (WorkOrder.PROCESS_UNLOAD.equals(p)) {
            return "XC";
        }
        if (WorkOrder.PROCESS_LOAD.equals(p)) {
            return "ZC";
        }
        if ("灌装工艺".equals(p)) {
            return "GZ";
        }
        if ("倒罐工艺".equals(p)) {
            switch (order.getWorkshopCode() == null ? "" : order.getWorkshopCode()) {
                case "ZZG": return "ZZ";
                case "XLG": return "GD";
                case "CPG": return "CP";
                default: return "DG";
            }
        }
        return "GN";
    }

    /**
     * 批次号 {阶段码}-{yyMM}-nn(如 勾调 2026-09 第 2 单 → GD-2609-02),一工单一批次,
     * 序号按"阶段×月"独立递增;yyMM 取工单日期,缺省用当天。FOR UPDATE 锁定读防并发重号。
     */
    private String nextBatchNo(WorkOrder order) {
        String prefix = stageCode(order) + "-" + (order.getPlanDate() != null
                ? order.getPlanDate().format(DateTimeFormatter.ofPattern("yyMM"))
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMM"))) + "-";
        WorkOrder latest = this.getOne(new QueryWrapper<WorkOrder>()
                .likeRight("batch_no", prefix).orderByDesc("batch_no").last("LIMIT 1 FOR UPDATE"));
        int next = 1;
        if (latest != null && latest.getBatchNo() != null && latest.getBatchNo().startsWith(prefix)) {
            try {
                next = Integer.parseInt(latest.getBatchNo().substring(prefix.length())) + 1;
            } catch (NumberFormatException ignore) {
                // 非法尾号从 1 重新计
            }
        }
        return prefix + String.format("%02d", next);
    }

    private void require(String actual, String expected, String message) {
        if (!expected.equals(actual)) {
            throw new IllegalArgumentException(message);
        }
    }

    private void require(String actual, String e1, String e2, String message) {
        if (!e1.equals(actual) && !e2.equals(actual)) {
            throw new IllegalArgumentException(message);
        }
    }

    private String statusText(String status) {
        switch (status == null ? "" : status) {
            case "pending": return "待执行";
            case "running": return "执行中";
            case "paused": return "已暂停";
            case "completed": return "已完成";
            case "abandoned": return "已废弃";
            default: return status;
        }
    }

    private String nvl(String v) {
        return v == null || v.isEmpty() ? "-" : v;
    }

    /** 写工单操作日志(永不抛,日志失败不影响主流程) */
    private void writeLog(String orderNo, String operator, String action, String content, String status) {
        try {
            logMapper.insert(new WorkOrderLog()
                    .setId(String.valueOf(System.nanoTime()))
                    .setOrderNo(orderNo)
                    .setOperator(operator == null || operator.isEmpty() ? "system" : operator)
                    .setAction(action)
                    .setContent(content)
                    .setOrderStatus(status)
                    .setOperateTime(LocalDateTime.now())
                    .setTenantId(1006).setDelFlag(0).setCreateTime(LocalDateTime.now()));
        } catch (Exception e) {
            log.warn("[工单日志] 写入失败 orderNo={} action={}", orderNo, action, e);
        }
    }
}
