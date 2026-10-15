package com.keytech.modules.maotai.scada.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.alarm.constants.AlarmStatusEnum;
import com.keytech.modules.alarm.entity.AlarmEvent;
import com.keytech.modules.alarm.mapper.AlarmEventMapper;
import com.keytech.modules.alarm.service.IAlarmEventService;
import com.keytech.modules.maotai.equipment.entity.EquipmentLedger;
import com.keytech.modules.maotai.equipment.mapper.EquipmentLedgerMapper;
import com.keytech.modules.maotai.scada.dto.AlarmHandleRequest;
import com.keytech.modules.maotai.tank.entity.TankConfig;
import com.keytech.modules.maotai.tank.mapper.TankConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 中集自控(SCADA)工艺报警查询(平台内部:前端"工艺报警"页拉取展示)。
 * <p>挂 /maotai/scada/alarm,走 Shiro JWT(同 maotai 其他内部端点),与入站
 * {@link ScadaAlarmReceiveController} 同包,整个中集报警功能(收+查)内聚在 maotai.scada。
 *
 * <p><b>为什么不复用通用 /alarm/alarmEvent/list/page:</b>通用 controller 有两层按登录用户
 * 的强制数据隔离 —— ① tenant_id = X-Tenant-Id 请求头 ② factory_code likeRight(登录用户 orgCode)。
 * 这套隔离是给"全厂通用报警中心、按部门隔离"设计的;但中集工艺报警是外部系统推上来的、
 * 面向全厂值班响应,一个车间操作员应能看到全厂中集报警,不该因自己 orgCode 不是 A10 开头而看不到。
 * 故新建本端点,固定 sourceSystem="中集自控" 过滤、不做 orgCode/tenant 强制隔离。
 *
 * <p><b>部门/工厂/产线名称翻译:</b>不实体 @Dict(jeecg 3.7.1 @Dict(sys_depart)+多租户拦截器会拼出
 * <code>tenant_id = 1006,1006</code> 非法 SQL 的 bug),改在本 controller 手动 join sys_depart:
 * 查完分页后收集本页涉及的 org_code,一次性 <code>SELECT org_code, depart_name FROM sys_depart</code>
 * where in,填回 AlarmEvent 的 factoryCodeDictText/deptCodeDictText/lineCodeDictText(非持久化字段,
 * JSON 序列化为驼峰 factoryCode_dictText 等,前端 _dictText 契约沿用)。
 *
 * @author maotai
 * @since 2026-08-12
 */
@Tag(name = "中集自控-工艺报警查询")
@RestController
@RequestMapping("/maotai/scada/alarm")
@RequiredArgsConstructor
@Slf4j
public class ScadaAlarmQueryController {

    private final IAlarmEventService alarmEventService;
    private final AlarmEventMapper alarmEventMapper;
    private final TankConfigMapper tankConfigMapper;
    private final EquipmentLedgerMapper equipmentLedgerMapper;

    /** 固定来源:中集自控工艺报警(前端按此拉取,与入站 ScadaAlarmReceiveController 写入的 sourceSystem 对齐)。 */
    private static final String SOURCE_SYSTEM = "中集自控";

    /**
     * 分页查询中集自控工艺报警。
     * <p>固定 sourceSystem="中集自控";可选按级别(alarmLevel 1/2/3)、状态(status 0未响应/1已响应/2处理中/3已处理)、
     * 关键词(alarmDesc 模糊)过滤;按 alarm_time 倒序。不做 tenant/orgCode 强制隔离(见类注释)。
     * 部门/工厂/产线名称在分页结果上手动 join sys_depart 翻译(避开 @Dict 多租户 bug)。
     *
     * @param pageNo   页码,默认 1
     * @param pageSize 每页条数,默认 10
     * @param level    报警等级(可选)1低 2中 3高
     * @param status   状态(可选)0未响应 1已响应 2处理中 3已处理
     * @param keyword  关键词(可选)模糊匹配 alarm_desc
     */
    @Operation(summary = "中集自控工艺报警分页查询", description = "固定 sourceSystem=中集自控,不按登录用户隔离")
    @GetMapping("/list")
    public Result<IPage<AlarmEvent>> list(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "alarmLevel", required = false) Integer level,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "keyword", required = false) String keyword) {

        LambdaQueryWrapper<AlarmEvent> qw = new LambdaQueryWrapper<>();
        qw.eq(AlarmEvent::getSourceSystem, SOURCE_SYSTEM);
        qw.eq(level != null, AlarmEvent::getAlarmLevel, level);
        qw.eq(status != null, AlarmEvent::getStatus, status);
        qw.like(StringUtils.isNotBlank(keyword), AlarmEvent::getAlarmDesc, keyword);
        qw.orderByDesc(AlarmEvent::getAlarmTime);

        Page<AlarmEvent> page = new Page<>(pageNo, pageSize);
        IPage<AlarmEvent> result = alarmEventService.page(page, qw);
        fillDeptNames(result.getRecords());
        fillTankEquipment(result.getRecords());
        return Result.OK(result);
    }

    /**
     * 处理工艺报警(前端"处理报警"弹窗提交)。
     * <p>status 必传(2-处理中 {@link AlarmStatusEnum#PROCESSING} / 3-已处理 {@link AlarmStatusEnum#PROCESSED}),
     * 由前端按当前报警状态给出可选项(未处理→处理中/已处理;处理中→仅已处理),后端不再按 handleWay 兜底映射——
     * 处理方式只是记录性字段,与处理状态无关。
     * 同时落 handle_way/handle_desc/update_by/update_time。
     * <p>靠 MyBatis-Plus 默认 NOT_NULL 策略,只 set 要改的字段再 updateById,不会把其它列刷成 null。
     * maotai 模块约定不加 @RequiresPermissions,靠 Shiro JWT 登录态保护。
     *
     * @param req 处理请求(id/handleWay/handleDesc/status)
     * @return 操作结果
     */
    @AutoLog(value = "中集自控-工艺报警-处理")
    @Operation(summary = "处理工艺报警", description = "status 必传(2处理中/3已处理),与处理方式解耦")
    @PostMapping("/handle")
    public Result<String> handle(@Valid @RequestBody AlarmHandleRequest req) {
        // 1. 校验报警存在,且是中集自控的(防误处理别源报警)
        AlarmEvent exists = alarmEventService.getById(req.getId());
        if (exists == null) {
            return Result.error("报警不存在或已删除");
        }
        if (!SOURCE_SYSTEM.equals(exists.getSourceSystem())) {
            return Result.error("非中集自控报警,不允许在此处理");
        }

        // 2. status 必传(前端弹窗按当前状态动态给出选项),不再按 handleWay 兜底
        Integer targetStatus = req.getStatus();
        if (targetStatus == null) {
            return Result.error("处理状态不能为空");
        }

        // 3. 取登录人(处理人)
        LoginUser u = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        // 4. 只 set 要改的字段,updateById 靠 NOT_NULL 策略只更新这些列
        AlarmEvent update = new AlarmEvent();
        update.setId(req.getId());
        update.setStatus(targetStatus);
        update.setHandleWay(req.getHandleWay());
        update.setHandleDesc(req.getHandleDesc());
        if (u != null) {
            update.setUpdateBy(u.getUsername());
        }
        update.setUpdateTime(LocalDateTime.now());

        alarmEventService.updateById(update);
        return Result.OK("处理成功");
    }

    /** 车间索引→车间名(与驾驶舱 WS_NAMES 同口径;字典改名时同步此处) */
    private static final String[] WS_NAMES = {"中转酒罐车间", "系列酒勾调厂房", "成品酒罐房", "包装厂房"};

    /**
     * 设备统一:报警关联到罐(匹配键与驾驶舱报警联动完全一致——报警点位号命中罐测点点号,
     * 或报警功能位置编码命中罐位置编码)时,设备列覆盖为台账罐名、车间列覆盖为罐所在车间,
     * 工艺报警页与驾驶舱红色罐体天然对齐。只改查询结果内存值,不落库。
     */
    private void fillTankEquipment(List<AlarmEvent> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        // 罐索引:点号/位置编码 → 罐配置;台账快照:funloc→台账(罐移动后台账位置编码是新值,优先)
        List<TankConfig> configs = tankConfigMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, TankConfig> byPi = new java.util.HashMap<>();
        Map<String, TankConfig> byFunloc = new java.util.HashMap<>();
        for (TankConfig cfg : configs) {
            if (StringUtils.isNotBlank(cfg.getLevelPointCode())) byPi.put(cfg.getLevelPointCode(), cfg);
            if (StringUtils.isNotBlank(cfg.getTempPointCode())) byPi.put(cfg.getTempPointCode(), cfg);
            if (StringUtils.isNotBlank(cfg.getAlcoholPointCode())) byPi.put(cfg.getAlcoholPointCode(), cfg);
        }
        List<EquipmentLedger> ledgers = equipmentLedgerMapper.selectList(
                new LambdaQueryWrapper<EquipmentLedger>().isNotNull(EquipmentLedger::getTankConfigId));
        Map<String, EquipmentLedger> byTankCfgId = new java.util.HashMap<>();
        Map<String, EquipmentLedger> byLedgerFunloc = new java.util.HashMap<>();
        for (EquipmentLedger l : ledgers) {
            byTankCfgId.put(l.getTankConfigId(), l);
            if (StringUtils.isNotBlank(l.getFunlocCode())) byLedgerFunloc.put(l.getFunlocCode(), l);
        }
        for (TankConfig cfg : configs) {
            if (StringUtils.isNotBlank(cfg.getFunlocCode()) && !byFunloc.containsKey(cfg.getFunlocCode())) {
                byFunloc.put(cfg.getFunlocCode(), cfg);
            }
        }
        for (AlarmEvent e : records) {
            TankConfig hit = StringUtils.isNotBlank(e.getAlarmPiCode()) ? byPi.get(e.getAlarmPiCode()) : null;
            if (hit == null && StringUtils.isNotBlank(e.getFuncLocCode())) {
                EquipmentLedger ledger = byLedgerFunloc.get(e.getFuncLocCode());
                if (ledger != null && ledger.getTankConfigId() != null) {
                    for (TankConfig cfg : configs) {
                        if (cfg.getId().equals(ledger.getTankConfigId())) { hit = cfg; break; }
                    }
                }
                if (hit == null) hit = byFunloc.get(e.getFuncLocCode());
            }
            if (hit == null) {
                continue;
            }
            EquipmentLedger ledger = byTankCfgId.get(hit.getId());
            // 设备=台账罐名(无台账回退罐配置名);车间=罐所在车间
            e.setDeviceCode(ledger != null && StringUtils.isNotBlank(ledger.getEquipName())
                    ? ledger.getEquipName() : hit.getTankId());
            if (hit.getWorkshopIndex() != null && hit.getWorkshopIndex() >= 0 && hit.getWorkshopIndex() < WS_NAMES.length) {
                e.setDeptCodeDictText(WS_NAMES[hit.getWorkshopIndex()]);
            }
        }
    }

    /**
     * 手动翻译部门/工厂/产线名称:收集本页所有 org_code(factory/dept/line),一次性查 sys_depart,
     * 填回 AlarmEvent 的 factoryCodeDictText/deptCodeDictText/lineCodeDictText。
     * <p><b>走 {@link AlarmEventMapper#selectDeptNameByOrgCodes}</b>(@InterceptorIgnore(tenantLine=true) 关多租户),
     * 不用 sysDepartService.list —— 那个会被 jeecg 多租户拦截器拼出 {@code tenant_id = 1006,1006} 非法 SQL
     * (maotai 单租户上下文租户值是逗号串)。部门表是跨租户字典表,本该忽略租户隔离。
     * 查不到的 code 填 null,前端 toRecord 的 || 兜底显示原 code。
     */
    private void fillDeptNames(List<AlarmEvent> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        // 收集本页涉及的所有非空 org_code(factory/dept/line 合集)
        Set<String> codeSet = new HashSet<>();
        for (AlarmEvent e : records) {
            if (StringUtils.isNotBlank(e.getFactoryCode())) codeSet.add(e.getFactoryCode());
            if (StringUtils.isNotBlank(e.getDeptCode())) codeSet.add(e.getDeptCode());
            if (StringUtils.isNotBlank(e.getLineCode())) codeSet.add(e.getLineCode());
        }
        if (codeSet.isEmpty()) {
            return;
        }
        // 关租户拦截查 sys_depart(mapper 方法带 @InterceptorIgnore)
        List<Map<String, Object>> rows = alarmEventMapper.selectDeptNameByOrgCodes(new ArrayList<>(codeSet));
        Map<String, String> codeToName = new java.util.HashMap<>();
        for (Map<String, Object> row : rows) {
            Object code = row.get("org_code");
            Object name = row.get("depart_name");
            if (code != null && name != null) {
                codeToName.put(code.toString(), name.toString());
            }
        }
        // 填回(查不到的填 null,前端兜底显示原 code)
        for (AlarmEvent e : records) {
            e.setFactoryCodeDictText(codeToName.get(e.getFactoryCode()));
            e.setDeptCodeDictText(codeToName.get(e.getDeptCode()));
            e.setLineCodeDictText(codeToName.get(e.getLineCode()));
        }
    }
}
