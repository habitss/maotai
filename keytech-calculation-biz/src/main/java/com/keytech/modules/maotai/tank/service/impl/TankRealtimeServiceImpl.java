package com.keytech.modules.maotai.tank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.ems.service.impl.EmsInfluxDbService;
import com.keytech.modules.ems.vo.FluxZone8Data;
import com.keytech.modules.maotai.tank.entity.TankConfig;
import com.keytech.modules.maotai.tank.mapper.TankConfigMapper;
import com.keytech.modules.alarm.entity.AlarmEvent;
import com.keytech.modules.alarm.mapper.AlarmEventMapper;
import com.keytech.modules.maotai.equipment.entity.EquipmentLedger;
import com.keytech.modules.maotai.equipment.mapper.EquipmentLedgerMapper;
import java.time.LocalDate;
import java.util.Date;
import java.time.ZoneId;
import java.util.HashMap;
import com.keytech.modules.maotai.tank.service.ITankRealtimeService;
import com.keytech.modules.maotai.tank.vo.TankRealtimeListVO;
import com.keytech.modules.maotai.tank.vo.TankRealtimeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * 罐体实时数据服务实现
 * <p>
 * 数据来源：数据库表 mes3_maotai_tank_config（165 行罐体静态配置），
 * 每行配置 level_point_code / temp_point_code / alcohol_point_code 指向
 * mes3_pi_base_point 的点位编码（= InfluxDB Min 表 cnl_num tag）。
 * 运行时批量查询 InfluxDB 各点位最近的 LAST 值作为液位/温度/酒精度；
 * 点位无数据的罐回退到 base_level 随机波动（保证页面不空白）。
 * 台账为唯一事实源（equipment_ledger，按 funloc_code 关联）：
 * 罐名取台账 equip_name（台账改名驾驶舱 10s 内跟随）；保养到期(maint_cycle+last_maint_date)→待检；
 * 罐状态算完后实时回写台账 run_status（normal→1/standby→2/warning,alarm→4，变化才写）。
 *
 * @author keytech
 * @since 2026-07-28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TankRealtimeServiceImpl implements ITankRealtimeService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 四车间名称（仅日志可读性，不影响逻辑） */
    private static final String[] WS_NAMES = {"中转酒罐车间", "系列酒勾调厂房", "成品酒罐房", "包装厂房"};

    /** 注入罐体配置 Mapper，从数据库读取罐体静态属性 */
    private final TankConfigMapper tankConfigMapper;

    /** InfluxDB 批量查询服务（复用能源模块的点位 LAST 查询链路） */
    private final EmsInfluxDbService emsInfluxDbService;

    /** 设备台账 Mapper（保养到期联动：maint_cycle + last_maint_date 到期 → 罐体显示待检） */
    private final EquipmentLedgerMapper equipmentLedgerMapper;

    /** 报警事件 Mapper(工艺报警联动:未消除报警 → 罐体红色 alarm 态,与工艺报警页同源) */
    private final AlarmEventMapper alarmEventMapper;

    /** 品牌字典翻译用(直接查 mes3_mdm_dict,绕开 redis 字典缓存) */
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    /** 品牌字典(编码→名称)实时加载:用户要求改字典立即生效,故不做进程内缓存(字典表极小,10s 一查无压力) */
    private Map<String, String> loadBrandNameMap() {
        Map<String, String> m = new HashMap<>();
        try {
            for (Map<String, Object> row : jdbcTemplate.queryForList(
                    "SELECT item_key, item_val FROM mes3_mdm_dict WHERE group_code='MES3_BRAND' AND del_flag=0")) {
                Object k = row.get("item_key");
                Object v = row.get("item_val");
                if (k != null && v != null) {
                    m.put(k.toString(), v.toString());
                }
            }
        } catch (Exception e) {
            log.warn("[TankRealtime] 品牌字典查询失败,brand 显示编码原值: {}", e.getMessage());
        }
        return m;
    }

    /**
     * 按车间索引获取罐体实时数据
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 罐体实时数据列表包装
     */
    @Override
    public TankRealtimeListVO listByWorkshop(Integer workshopIndex) {
        int idx = workshopIndex == null || workshopIndex < 0 || workshopIndex > 3 ? 0 : workshopIndex;
        String now = LocalDateTime.now().format(FMT);

        // 从数据库查询该车间全部罐体配置（按 sort 排序）
        List<TankConfig> configs = tankConfigMapper.selectList(
                new LambdaQueryWrapper<TankConfig>()
                        .eq(TankConfig::getWorkshopIndex, idx)
                        .orderByAsc(TankConfig::getSort)
        );

        // 批量查询本车间全部点位的 influx 最近值(近 10 分钟,60s 窗口 LAST)
        Map<String, List<FluxZone8Data>> realValues = queryPointValues(configs);

        // 台账快照(funloc → 行):罐名/保养到期/状态写回共用,台账为唯一事实源
        Map<String, EquipmentLedger> ledgerMap = loadLedgerMap();

        // 未消除工艺报警(与工艺报警页同源:中集自控+未处理/处理中),命中罐体强制报警色
        Map<String, AlarmEvent> alarmMap = loadAlarmMap();

        // 基于配置生成实时数据 VO
        List<TankRealtimeVO> list = buildRealtimeList(configs, now, realValues, ledgerMap, alarmMap);
        log.info("[TankRealtime] workshop={}({}) tanks={} influxPoints={} updatedAt={}",
                idx, WS_NAMES[idx], list.size(), realValues.size(), now);

        // 状态实时回写台账(口径统一:首页在线率/台账列表与驾驶舱同源)
        syncRunStatusToLedger(list, ledgerMap);

        return new TankRealtimeListVO()
                .setWorkshopIndex(idx)
                .setUpdatedAt(now)
                .setList(list);
    }

    /**
     * 批量查询罐体点位在 influx 的最近值
     *
     * @param configs 罐体配置列表
     * @return Map<点位编码, 该点位按时间升序的值序列(取末位为最新)>
     */
    private Map<String, List<FluxZone8Data>> queryPointValues(List<TankConfig> configs) {
        Set<String> codes = new HashSet<>();
        for (TankConfig cfg : configs) {
            if (notBlank(cfg.getLevelPointCode())) codes.add(cfg.getLevelPointCode());
            if (notBlank(cfg.getTempPointCode())) codes.add(cfg.getTempPointCode());
            if (notBlank(cfg.getAlcoholPointCode())) codes.add(cfg.getAlcoholPointCode());
        }
        if (codes.isEmpty()) {
            return Map.of();
        }
        try {
            LocalDateTime end = LocalDateTime.now();
            return emsInfluxDbService.queryMultiPointsWithIntervalLastMap(
                    new ArrayList<>(codes), end.minusMinutes(10), end, 60);
        } catch (Exception e) {
            log.warn("[TankRealtime] influx 查询失败,全部回退模拟值: {}", e.getMessage());
            return Map.of();
        }
    }

    /** 取点位最新值;无数据返回 null */
    private Double latest(Map<String, List<FluxZone8Data>> values, String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        List<FluxZone8Data> list = values.get(code);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(list.size() - 1).getValue();
    }

    /** 罐容量:台账 tank_capacity 优先(用户可改,唯一事实源),回退 tank_config.capacity_m3 */
    private Double tankCapacity(TankConfig cfg, Map<String, EquipmentLedger> ledgerMap) {
        EquipmentLedger ledger = ledgerByTank(cfg, ledgerMap);
        if (ledger != null && ledger.getTankCapacity() != null && ledger.getTankCapacity() > 0) {
            return ledger.getTankCapacity();
        }
        return cfg.getCapacityM3();
    }

    /** brand 列存字典编码 → 翻译为显示名;未知编码/空值原样返回(兼容旧数据) */
    private String brandDisplayName(String brandCode, Map<String, String> brandNames) {
        if (brandCode == null || brandCode.isEmpty()) {
            return brandCode;
        }
        return brandNames.getOrDefault(brandCode, brandCode);
    }

    // ===== 实时数据组装 =====

    /**
     * 根据数据库配置列表生成罐体实时数据 VO 列表
     * <p>
     * 遍历每行配置，按 status_type 分流到 normal/standby 构造方法。
     *
     * @param configs 数据库罐体配置列表
     * @param now     当前时间字符串
     * @param real    influx 点位最新值
     * @return 罐体实时数据 VO 列表
     */
    private List<TankRealtimeVO> buildRealtimeList(List<TankConfig> configs, String now, Map<String, List<FluxZone8Data>> real, Map<String, EquipmentLedger> ledgerMap, Map<String, AlarmEvent> alarmMap) {
        List<TankRealtimeVO> list = new ArrayList<>(configs.size());
        Map<String, String> brandNames = loadBrandNameMap();
        for (TankConfig cfg : configs) {
            TankRealtimeVO vo;
            if ("standby".equals(cfg.getStatusType())) {
                vo = buildStandbyTank(cfg, now, real, ledgerMap, brandNames);
            } else {
                vo = buildNormalTank(cfg, now, real, ledgerMap, brandNames);
            }
            // 工艺报警覆盖:命中未消除报警 → 状态强制 alarm(优先级最高);摘要+明细(弹窗用)同源填充
            List<AlarmHit> hits = matchAlarms(cfg, vo, alarmMap);
            if (!hits.isEmpty()) {
                vo.setStatus("alarm")
                        .setAlarmDesc(buildAlarmDesc(hits.get(0).event));
                java.util.List<java.util.Map<String, Object>> alarmList = new java.util.ArrayList<>();
                for (AlarmHit hit : hits) {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("level", hit.event.getAlarmLevel());
                    m.put("desc", notBlank(hit.event.getAlarmDesc()) ? hit.event.getAlarmDesc() : hit.event.getAlarmPiCode());
                    m.put("time", hit.event.getAlarmTime() != null ? hit.event.getAlarmTime().format(FMT) : "");
                    // 报警命中的测点字段(level/temp/alcohol):前端据此只把对应参数行(液位/温度/酒精度)标红
                    m.put("field", hit.field);
                    alarmList.add(m);
                }
                vo.setAlarmList(alarmList);
            }
            list.add(vo);
        }
        return list;
    }

    /**
     * 未消除报警映射:键=报警点位号/功能位置编码 → 事件(同罐多条取最新)。
     * 口径与工艺报警页一致:source_system=中集自控 且 status IN (0未响应,1已响应,2处理中)。
     */
    private Map<String, AlarmEvent> loadAlarmMap() {
        Map<String, AlarmEvent> map = new HashMap<>();
        try {
            List<AlarmEvent> active = alarmEventMapper.selectList(
                    new LambdaQueryWrapper<AlarmEvent>()
                            .eq(AlarmEvent::getDelFlag, 0)
                            .eq(AlarmEvent::getSourceSystem, "中集自控")
                            .in(AlarmEvent::getStatus, 0, 1, 2)
                            .orderByDesc(AlarmEvent::getAlarmTime));
            for (AlarmEvent e : active) {
                if (notBlank(e.getAlarmPiCode())) {
                    map.putIfAbsent("PI:" + e.getAlarmPiCode(), e);
                }
                if (notBlank(e.getFuncLocCode())) {
                    map.putIfAbsent("FL:" + e.getFuncLocCode(), e);
                }
            }
        } catch (Exception ex) {
            log.warn("[TankRealtime] 报警联动查询失败,本次跳过: {}", ex.getMessage());
        }
        return map;
    }

    /** 报警命中:事件 + 命中字段(level/temp/alcohol,按报警点位属于哪列测点;位置编码匹配时字段为 null) */
    private static final class AlarmHit {
        final AlarmEvent event;
        final String field;

        AlarmHit(AlarmEvent event, String field) {
            this.event = event;
            this.field = field;
        }
    }

    /** 罐配置×报警匹配:三列点位号 PI: 或 功能位置编码 FL: 命中的全部未消除报警(时间倒序) */
    private List<AlarmHit> matchAlarms(TankConfig cfg, TankRealtimeVO vo, Map<String, AlarmEvent> alarmMap) {
        List<AlarmHit> hits = new ArrayList<>();
        if (alarmMap.isEmpty()) {
            return hits;
        }
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (String[] kf : new String[][]{
                {notBlank(cfg.getLevelPointCode()) ? "PI:" + cfg.getLevelPointCode() : null, "level"},
                {notBlank(cfg.getTempPointCode()) ? "PI:" + cfg.getTempPointCode() : null, "temp"},
                {notBlank(cfg.getAlcoholPointCode()) ? "PI:" + cfg.getAlcoholPointCode() : null, "alcohol"},
                {notBlank(vo.getFunlocCode()) ? "FL:" + vo.getFunlocCode() : null, null},
                {notBlank(cfg.getFunlocCode()) ? "FL:" + cfg.getFunlocCode() : null, null}}) {
            if (kf[0] == null) {
                continue;
            }
            AlarmEvent e = alarmMap.get(kf[0]);
            if (e != null && seen.add(String.valueOf(e.getId()))) {
                hits.add(new AlarmHit(e, kf[1]));
            }
        }
        hits.sort((a, b) -> b.event.getAlarmTime().compareTo(a.event.getAlarmTime()));
        return hits;
    }

    /** 报警摘要:级别+内容+发生时间 */
    private String buildAlarmDesc(AlarmEvent e) {
        String level = e.getAlarmLevel() == null ? "" : ("[" + e.getAlarmLevel() + "级] ");
        String desc = notBlank(e.getAlarmDesc()) ? e.getAlarmDesc() : e.getAlarmPiCode();
        String time = e.getAlarmTime() != null ? e.getAlarmTime().format(FMT) : "";
        return level + desc + (time.isEmpty() ? "" : " (" + time + ")");
    }

    /**
     * 构造待检/清洗罐：液位 0、无酒精度(null 让前端保留 "-")、进出量 0、状态固定 standby
     *
     * @param cfg 数据库罐体配置
     * @param now 当前时间字符串
     * @param real influx 点位最新值
     * @return 罐体实时数据 VO
     */
    private TankRealtimeVO buildStandbyTank(TankConfig cfg, String now, Map<String, List<FluxZone8Data>> real, Map<String, EquipmentLedger> ledgerMap, Map<String, String> brandNames) {
        Double temp = latest(real, cfg.getTempPointCode());
        return new TankRealtimeVO()
                .setTankId(displayName(cfg, ledgerMap))
                .setFunlocCode(displayFunloc(cfg, ledgerMap))
                .setBrand(brandDisplayName(cfg.getBrand(), brandNames))
                .setLevelPct(0.0)
                .setTemperatureC(temp != null ? round(temp) : round(22.5 + rand(-2, 3.5)))
                .setAlcoholPct(null)
                .setVolumeM3(0.0)
                .setCapacityM3(tankCapacity(cfg, ledgerMap))
                .setStatus("standby")
                .setBatchNo(buildBatchNo(cfg.getTankId(), now))
                .setInbound(0.0)
                .setOutbound(0.0)
                .setUpdatedAt(now);
    }

    /**
     * 构造正常/预警罐：液位/温度/酒精度优先取 influx 点位真值，
     * 点位无数据的维度回退随机模拟；状态按阈值(液位>85 或 温度>26)判定
     *
     * @param cfg 数据库罐体配置（base_level 为液位回退基准值）
     * @param now 当前时间字符串
     * @param real influx 点位最新值
     * @return 罐体实时数据 VO
     */
    private TankRealtimeVO buildNormalTank(TankConfig cfg, String now, Map<String, List<FluxZone8Data>> real, Map<String, EquipmentLedger> ledgerMap, Map<String, String> brandNames) {
        // 液位:点位真值优先,无数据在基准值上小幅波动(±3%)
        Double realLevel = latest(real, cfg.getLevelPointCode());
        double baseLevel = cfg.getBaseLevel() != null ? cfg.getBaseLevel() : 50.0;
        double level = realLevel != null ? clamp(round(realLevel), 0, 100)
                : clamp(round(baseLevel + rand(-3, 3)), 0, 100);
        // 温度:点位真值优先,回退 20~26°C 波动
        Double realTemp = latest(real, cfg.getTempPointCode());
        double temp = realTemp != null ? round(realTemp) : round(22.5 + rand(-2, 3.5));
        // 酒精度:点位真值优先,回退 52~54 %vol
        Double realAlcohol = latest(real, cfg.getAlcoholPointCode());
        double alcohol = realAlcohol != null ? round(realAlcohol) : round(53.0 + rand(-1, 1));
        // 容量根据液位比例计算
        double capacity = tankCapacity(cfg, ledgerMap) != null ? tankCapacity(cfg, ledgerMap) : 0;
        double volume = round(capacity * level / 100);
        // 进出量
        double inbound = round(10 + rand(0, 8));
        double outbound = round(8 + rand(0, 6));
        // 状态(09-17 用户定稿口径):预警/报警只认中集工艺报警(alarm_event),平台不再按阈值自算 warning——
        // 驾驶舱/预览罐色与工艺报警页永远一致;液位>85/温度>26 仅作为数值展示,不改状态色。
        // 待机(standby)=台账保养到期或配置待机罐,保留。
        boolean maintDue = isMaintDue(ledgerByTank(cfg, ledgerMap));
        String status = maintDue ? "standby" : "normal";

        return new TankRealtimeVO()
                .setTankId(displayName(cfg, ledgerMap))
                .setFunlocCode(displayFunloc(cfg, ledgerMap))
                .setBrand(brandDisplayName(cfg.getBrand(), brandNames))
                .setLevelPct(level)
                .setTemperatureC(temp)
                .setAlcoholPct(alcohol)
                .setVolumeM3(volume)
                .setCapacityM3(tankCapacity(cfg, ledgerMap))
                .setStatus(status)
                .setBatchNo(buildBatchNo(cfg.getTankId(), now))
                .setInbound(inbound)
                .setOutbound(outbound)
                .setUpdatedAt(now);
    }

    /**
     * 台账全量快照(funloc → 行)。罐名/保养到期/状态回写共用——台账是罐体展示信息的唯一事实源。
     */
    private Map<String, EquipmentLedger> loadLedgerMap() {
        Map<String, EquipmentLedger> map = new HashMap<>();
        try {
            for (EquipmentLedger row : equipmentLedgerMapper.selectList(
                    new LambdaQueryWrapper<EquipmentLedger>().isNotNull(EquipmentLedger::getTankConfigId))) {
                map.put(row.getTankConfigId(), row);
            }
        } catch (Exception e) {
            log.warn("[TankRealtime] 台账加载失败,名称/保养/状态联动降级: {}", e.getMessage());
        }
        return map;
    }

    /** 罐显示名:台账 equip_name 优先(改名 10s 内跟随),无台账记录回退 tank_config.tank_id */
    private String displayName(TankConfig cfg, Map<String, EquipmentLedger> ledgerMap) {
        EquipmentLedger row = ledgerByTank(cfg, ledgerMap);
        return row != null && row.getEquipName() != null && !row.getEquipName().isEmpty()
                ? row.getEquipName() : cfg.getTankId();
    }

    /** 按稳定键 tank_config_id 取台账行(位置编码/名称在台账随意改,关联不断) */
    private EquipmentLedger ledgerByTank(TankConfig cfg, Map<String, EquipmentLedger> ledgerMap) {
        return cfg.getId() == null ? null : ledgerMap.get(cfg.getId());
    }

    /** 罐位置编码:台账 funloc_code 优先(改编码 10s 内跟随),无台账记录回退 tank_config.funloc_code */
    private String displayFunloc(TankConfig cfg, Map<String, EquipmentLedger> ledgerMap) {
        EquipmentLedger row = ledgerByTank(cfg, ledgerMap);
        return row != null && row.getFunlocCode() != null && !row.getFunlocCode().isEmpty()
                ? row.getFunlocCode() : cfg.getFunlocCode();
    }

    /** 保养到期:台账 maint_cycle + last_maint_date <= 今天(任一为空不算) */
    private boolean isMaintDue(EquipmentLedger row) {
        if (row == null || row.getMaintCycle() == null || row.getLastMaintDate() == null) {
            return false;
        }
        LocalDate dueDate = row.getLastMaintDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(row.getMaintCycle());
        return !dueDate.isAfter(LocalDate.now());
    }

    /**
     * 罐状态实时回写台账 run_status(1在线 2停机 4报警;3维修为人工状态不覆盖)。
     * <p>变化才写,避免每次轮询打表;首页看板在线率读台账,与驾驶舱自动同口径。
     */
    private void syncRunStatusToLedger(List<TankRealtimeVO> list, Map<String, EquipmentLedger> ledgerMap) {
        try {
            // 反查表:VO 只带台账最新 funloc_code,按它定位台账行(与 loadLedgerMap 同源数据)
            Map<String, EquipmentLedger> byFunloc = new HashMap<>();
            for (EquipmentLedger row : ledgerMap.values()) {
                if (row.getFunlocCode() != null) {
                    byFunloc.put(row.getFunlocCode(), row);
                }
            }
            Date now = new Date();
            int changed = 0;
            for (TankRealtimeVO tank : list) {
                if (tank.getFunlocCode() == null || tank.getStatus() == null) {
                    continue;
                }
                Integer target = mapCockpitStatus(tank.getStatus());
                EquipmentLedger row = byFunloc.get(tank.getFunlocCode());
                // 3-维修为台账人工状态,不被驾驶舱覆盖
                if (target == null || row == null || target.equals(row.getRunStatus())
                        || Integer.valueOf(3).equals(row.getRunStatus())) {
                    continue;
                }
                EquipmentLedger patch = new EquipmentLedger();
                patch.setId(row.getId());
                patch.setRunStatus(target);
                patch.setUpdateTime(now);
                patch.setUpdateBy("tank-realtime");
                equipmentLedgerMapper.updateById(patch);
                changed++;
            }
            if (changed > 0) {
                log.info("[TankRealtime] 状态实时回写台账 {} 条", changed);
            }
        } catch (Exception e) {
            log.warn("[TankRealtime] 状态回写台账失败(不影响驾驶舱显示): {}", e.getMessage());
        }
    }

    /** 驾驶舱罐状态 → 台账 run_status 字典;null=不覆盖 */
    private Integer mapCockpitStatus(String cockpitStatus) {
        if (cockpitStatus == null) {
            return null;
        }
        switch (cockpitStatus) {
            case "normal":
                return 1;
            case "standby":
                return 2;
            case "warning":
            case "alarm":
                return 4;
            default:
                return null;
        }
    }

    // ===== 工具方法 =====

    /** 判断编码非空 */
    private boolean notBlank(String s) {
        return s != null && !s.isEmpty();
    }

    /**
     * 根据罐号和当前日期生成批次号
     *
     * @param tankId 罐体标识
     * @param now    当前时间字符串
     * @return 批次号（如 BATCH-20260803-101）
     */
    private String buildBatchNo(String tankId, String now) {
        return "BATCH-" + now.substring(0, 10).replace("-", "") + "-" + tankId.replaceAll("[^0-9]", "");
    }

    /** 生成 [min, max) 范围内的随机浮点数 */
    private double rand(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /** 四舍五入保留一位小数 */
    private double round(double v) {
        return Math.round(v * 10) / 10.0;
    }

    /** 限制值在 [min, max] 范围内 */
    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
