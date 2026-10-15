package com.keytech.modules.maotai.cockpit.service.impl;

import com.keytech.modules.ems.service.impl.EmsInfluxDbService;
import com.keytech.modules.ems.vo.FluxZone8Data;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.modules.maotai.cockpit.entity.CockpitEnergy;
import com.keytech.modules.maotai.cockpit.entity.CockpitInventory;
import com.keytech.modules.maotai.cockpit.entity.CockpitInventoryTrend;
import com.keytech.modules.maotai.cockpit.entity.CockpitTankRisk;
import com.keytech.modules.maotai.cockpit.entity.CockpitTransferDaily;
import com.keytech.modules.maotai.cockpit.entity.CockpitTransferPlan;
import com.keytech.modules.maotai.cockpit.mapper.CockpitEnergyMapper;
import com.keytech.modules.maotai.cockpit.mapper.CockpitInventoryMapper;
import com.keytech.modules.maotai.cockpit.mapper.CockpitInventoryTrendMapper;
import com.keytech.modules.maotai.cockpit.mapper.CockpitTankRiskMapper;
import com.keytech.modules.maotai.cockpit.mapper.CockpitTransferDailyMapper;
import com.keytech.modules.maotai.cockpit.mapper.CockpitTransferPlanMapper;
import com.keytech.modules.maotai.cockpit.service.ICockpitCardService;
import com.keytech.modules.maotai.cockpit.vo.EnergyItemVO;
import com.keytech.modules.maotai.cockpit.vo.InventoryTrendItemVO;
import com.keytech.modules.maotai.cockpit.vo.InventoryTrendPointVO;
import com.keytech.modules.maotai.cockpit.vo.EnvironmentVO;
import com.keytech.modules.maotai.cockpit.vo.InventoryItemVO;
import com.keytech.modules.maotai.cockpit.vo.InventoryVO;
import com.keytech.modules.maotai.cockpit.vo.MaterialStatusRowVO;
import com.keytech.modules.maotai.cockpit.vo.MaterialStatusVO;
import com.keytech.modules.maotai.cockpit.vo.TankRiskVO;
import com.keytech.modules.maotai.cockpit.vo.TransferExecutionVO;
import com.keytech.modules.maotai.cockpit.vo.TransferItemVO;
import com.keytech.modules.maotai.cockpit.vo.TransferPlanVO;
import com.keytech.modules.maotai.cockpit.vo.TrendPeriodsVO;
import com.keytech.modules.maotai.cockpit.vo.TrendVO;
import com.keytech.modules.maotai.tank.service.ITankRealtimeService;
import com.keytech.modules.maotai.tank.vo.TankRealtimeListVO;
import com.keytech.modules.maotai.tank.vo.TankRealtimeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * 驾驶舱卡片数据服务实现
 * <p>
 * 数据架构（接口隔离模式，按车间隔离）：
 * <ul>
 *   <li><b>同源派生（永久）</b>：tankRisk / materialStatus / trend 直接从当前车间 3D 罐体数据派生，
 *       接入真实数据源后逻辑不变。</li>
 *   <li><b>近似派生（临时）</b>：inventory / transfer 当前用当前车间罐体数据近似，
 *       真实数据阶段替换为对应业务系统直查（已标注 TODO）。</li>
 *   <li><b>独立 Mock（车间专属基准）</b>：energy / environment / monthlyTransfer / annualTransfer
 *       与罐体无直接数学关系，按车间使用固定基准值生成（无随机波动，保证同一车间每次返回一致）。</li>
 * </ul>
 *
 * @author keytech
 * @since 2026-07-29
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CockpitCardServiceImpl implements ICockpitCardService {

    /** 注入罐体实时服务，作为驾驶舱卡片的统一数据锚点 */
    private final ITankRealtimeService tankRealtimeService;

    /** InfluxDB 批量查询服务（环境温湿度点位真值） */
    private final EmsInfluxDbService emsInfluxDbService;

    // ===== 驾驶舱 MySQL 数据表(2026-09-10 起 7 模块数据源,无数据回退原派生/Mock 逻辑) =====
    private final CockpitTransferDailyMapper transferDailyMapper;
    private final CockpitTransferPlanMapper transferPlanMapper;
    private final CockpitInventoryMapper inventoryMapper;
    private final CockpitInventoryTrendMapper inventoryTrendMapper;
    private final CockpitEnergyMapper energyMapper;
    private final CockpitTankRiskMapper tankRiskMapper;

    /** 六大品牌固定配置（名称、简称、颜色） */
    private static final String[][] BRAND_DEFS = {
            {"飞天茅台",   "飞天",   "#ff5058"},
            {"茅台王子酒", "王子酒", "#3e89ff"},
            {"茅台迎宾酒", "迎宾酒", "#42caa5"},
            {"汉酱酒",     "汉酱酒", "#8058ed"},
            {"赖茅酒",     "赖茅酒", "#f4b526"},
            {"仁酒",       "仁酒",   "#28bfcf"},
    };

    /** 车间总数（索引 0~3） */
    private static final int WORKSHOP_COUNT = 4;

    // ===== 车间专属 Mock 基准（固定常量，无随机波动） =====

    /**
     * 月度调拨计划基准（吨，6 品牌）——按车间工艺特点差异化
     * <p>车间0 中转酒罐车间：中转吞吐量大，飞天/迎宾为主
     * <br>车间1 系列酒勾调厂房：王子酒/汉酱勾调输出高
     * <br>车间2 成品酒罐房：罐数最多(61)，成品出货量最大
     * <br>车间3 包装厂房：高位输送，整体量中等
     */
    private static final double[][] MONTHLY_PLANNED_BY_WS = {
            {320, 210, 240, 150, 180, 110},  // 车间0 中转
            {180, 280, 150, 120, 100,  80},  // 车间1 勾调
            {450, 290, 360, 220, 280, 160},  // 车间2 成品（最大）
            {280, 180, 200, 130, 150,  90},  // 车间3 高位
    };

    /**
     * 年度调拨计划基准（吨，6 品牌）——月度基准 × 12
     */
    private static final double[][] ANNUAL_PLANNED_BY_WS = {
            {3840, 2520, 2880, 1800, 2160, 1320},  // 车间0
            {2160, 3360, 1800, 1440, 1200,  960},  // 车间1
            {5400, 3480, 4320, 2640, 3360, 1920},  // 车间2（最大）
            {3360, 2160, 2400, 1560, 1800, 1080},  // 车间3
    };

    /**
     * 月度调拨柱状图纵坐标最大值（吨）——按各车间最大单品牌计划值 × 1.2 取整
     * <p>保证4个车间的柱子都在 60%~85% 高度区间，不超限也不过矮
     */
    private static final double[] MONTHLY_AXIS_MAX_BY_WS = {400, 350, 550, 350};

    /**
     * 年度调拨柱状图纵坐标最大值（吨）——按各车间最大单品牌计划值 × 1.2 取整
     */
    private static final double[] ANNUAL_AXIS_MAX_BY_WS = {4800, 4200, 6600, 4200};

    /**
     * 能耗计划基准（吨）——[水m³, 电kWh, 压缩空气m³]
     * <p>车间0 中转(39罐)：中等规模
     * <br>车间1 勾调(31罐)：罐数少但勾调需蒸汽温控→压缩空气偏高
     * <br>车间2 成品(61罐)：罐数最多→水电气全面最高
     * <br>车间3 高位(34罐)：半开放高位区，整体能耗偏低
     */
    private static final double[][] ENERGY_PLANNED_BY_WS = {
            {194, 3314,  768},  // 车间0 中转
            {165, 2840,  920},  // 车间1 勾调（压缩空气高）
            {298, 5120, 1180},  // 车间2 成品（全面最高）
            {142, 2460,  520},  // 车间3 高位（最低）
    };

    /** 能耗实际完成率（固定 88%，无随机波动） */
    private static final double ENERGY_ACTUAL_RATIO = 0.88;

    /**
     * 室内环境基准（固定值）——[温度℃, 湿度%]
     * <p>车间0 中转：标准存储环境
     * <br>车间1 勾调：温控严格、湿度略高
     * <br>车间2 成品：大车间常规环境
     * <br>车间3 高位：半开放区温度略高、湿度偏低
     */
    private static final double[][] ENVIRONMENT_BY_WS = {
            {22.5, 58.0},  // 车间0 中转
            {20.8, 62.0},  // 车间1 勾调（温度低、湿度高）
            {24.2, 55.0},  // 车间2 成品
            {25.6, 48.0},  // 车间3 高位（温度高、湿度低）
    };

    // ===== 罐体数据锚点 =====

    /**
     * 获取指定车间的罐体实时快照
     * <p>
     * 作为 tankRisk / materialStatus / trend / inventory / transfer 的统一数据源，
     * 确保驾驶舱卡片与 3D 罐体视图基于同一套数据生成逻辑，且按车间隔离。
     *
     * @param workshopIndex 车间索引（0~3，越界归 0）
     * @return 当前车间罐体实时数据列表
     */
    private List<TankRealtimeVO> getTanksByWorkshop(int workshopIndex) {
        int idx = clampWorkshop(workshopIndex);
        TankRealtimeListVO ws = tankRealtimeService.listByWorkshop(idx);
        List<TankRealtimeVO> list = (ws != null && ws.getList() != null) ? ws.getList() : new ArrayList<>();
        log.debug("[CockpitCard] getTanksByWorkshop idx={} size={}", idx, list.size());
        return list;
    }

    // ===== 调拨计划（独立 Mock：车间专属固定基准）=====

    /**
     * 获取月度调拨数据
     * <p>
     * 按车间返回专属固定基准的调拨计划/实际量。
     * TODO: 真实数据阶段接入 ERP 月度调拨计划单查询。
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 各品牌月度计划/实际调拨量、完成率、偏差
     */
    @Override
    public TransferPlanVO getMonthlyTransfer(int workshopIndex, Integer month) {
        int idx = clampWorkshop(workshopIndex);
        // 优先查月度调拨表(mes3_maotai_cockpit_transfer_plan);无数据回退固定基准 Mock
        TransferPlanVO fromDb = queryTransferPlan(idx, java.time.LocalDate.now().getYear(),
                month == null ? java.time.LocalDate.now().getMonthValue() : month, MONTHLY_AXIS_MAX_BY_WS[idx]);
        if (fromDb != null) {
            return fromDb;
        }
        List<TransferItemVO> items = buildTransferItems(MONTHLY_PLANNED_BY_WS[idx], idx);
        double totalPlanned = sum(items, TransferItemVO::getPlanned);
        double totalActual = sum(items, TransferItemVO::getActual);
        return new TransferPlanVO()
                .setCompletion(round(totalActual / totalPlanned * 100, 1))
                .setVariance(round(totalActual - totalPlanned, 1))
                .setAxisMax(MONTHLY_AXIS_MAX_BY_WS[idx])
                .setUnit("吨")
                .setItems(items);
    }

    /**
     * 获取年度调拨数据
     * <p>
     * 按车间返回专属固定基准的年度调拨计划/实际量。
     * TODO: 真实数据阶段接入 ERP 年度调拨计划单查询。
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 各品牌年度计划/实际调拨量、完成率、偏差
     */
    @Override
    public TransferPlanVO getAnnualTransfer(int workshopIndex) {
        int idx = clampWorkshop(workshopIndex);
        // 优先查调拨表按年聚合(月度 SUM);无数据回退固定基准 Mock
        TransferPlanVO fromDb = queryTransferPlanYear(idx, java.time.LocalDate.now().getYear(), ANNUAL_AXIS_MAX_BY_WS[idx]);
        if (fromDb != null) {
            return fromDb;
        }
        List<TransferItemVO> items = buildTransferItems(ANNUAL_PLANNED_BY_WS[idx], idx);
        double totalPlanned = sum(items, TransferItemVO::getPlanned);
        double totalActual = sum(items, TransferItemVO::getActual);
        return new TransferPlanVO()
                .setCompletion(round(totalActual / totalPlanned * 100, 1))
                .setVariance(round(totalActual - totalPlanned, 1))
                .setAxisMax(ANNUAL_AXIS_MAX_BY_WS[idx])
                .setUnit("吨")
                .setItems(items);
    }

    // ===== 品牌库存（近似派生：按品牌汇总当前车间在罐量）=====

    /**
     * 获取品牌库存数据
     * <p>
     * 按品牌汇总当前车间罐体在罐容量（m³≈吨），保证库存与罐体液位联动。
     * <p>
     * TODO: 真实数据阶段替换为 ERP 库存台账直查。台账量 ≠ 在罐量（含在途/已灌装未出库差额）。
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 品牌库存数据
     */
    @Override
    public InventoryVO getInventory(int workshopIndex) {
        int idx0 = clampWorkshop(workshopIndex);
        // 优先查库存结构表(当天轮换组);无数据回退罐体派生
        List<CockpitInventory> invRows = inventoryMapper.selectList(new QueryWrapper<CockpitInventory>()
                .eq("stat_date", java.time.LocalDate.now())
                .eq("workshop_index", idx0)
                .eq("group_no", currentGroup())
                .eq("del_flag", 0));
        if (!invRows.isEmpty()) {
            double totalDb = invRows.stream().filter(r -> r.getQuantity() != null).mapToDouble(CockpitInventory::getQuantity).sum();
            List<InventoryItemVO> dbItems = new ArrayList<>();
            for (CockpitInventory r : invRows) {
                if (r.getQuantity() == null) continue;
                String color = brandColor(r.getBrand());
                dbItems.add(new InventoryItemVO()
                        .setName(r.getBrand())
                        .setQuantity(round(r.getQuantity(), 1))
                        .setPercentage(totalDb > 0 ? round(r.getQuantity() / totalDb * 100, 1) : 0)
                        .setColor(color));
            }
            return new InventoryVO().setTotal(round(totalDb, 1)).setUnit("吨").setItems(dbItems);
        }
        List<TankRealtimeVO> tanks = getTanksByWorkshop(workshopIndex);
        // 按品牌汇总在罐容量（排除"纯水"等非产品品牌）
        Map<String, Double> brandVolume = new LinkedHashMap<>();
        for (String[] def : BRAND_DEFS) {
            brandVolume.put(def[0], 0.0);
        }
        for (TankRealtimeVO t : tanks) {
            String brand = t.getBrand();
            if (!brandVolume.containsKey(brand)) {
                continue;
            }
            double vol = t.getVolumeM3() != null ? t.getVolumeM3() : 0;
            brandVolume.merge(brand, vol, Double::sum);
        }
        // 构造返回列表
        List<InventoryItemVO> items = new ArrayList<>();
        double total = 0;
        for (String[] def : BRAND_DEFS) {
            double qty = round(brandVolume.getOrDefault(def[0], 0.0), 1);
            total += qty;
            items.add(new InventoryItemVO()
                    .setName(def[0])
                    .setQuantity(qty)
                    .setColor(def[2]));
        }
        // 计算各品牌占比
        for (InventoryItemVO item : items) {
            item.setPercentage(total > 0 ? round(item.getQuantity() / total * 100, 1) : 0);
        }
        log.debug("[CockpitCard] inventory ws={} total={} items={}", workshopIndex, total, items.size());
        return new InventoryVO()
                .setTotal(round(total, 1))
                .setUnit("吨")
                .setItems(items);
    }

    /** 每品牌最多取多少个代表罐，用于前端轮播展示 */
    private static final int MAX_TANKS_PER_BRAND = 4;

    // ===== 实时物料明细（同源派生：取当前车间罐体代表行）=====

    /**
     * 获取实时物料数据
     * <p>
     * 每个品牌取当前车间前 MAX_TANKS_PER_BRAND 个罐作为代表行，供前端轮播展示。
     * 数据与 3D 罐体视图完全同源。接入真实数据源后逻辑不变。
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 实时物料明细
     */
    @Override
    public MaterialStatusVO getMaterialStatus(int workshopIndex) {
        List<TankRealtimeVO> tanks = getTanksByWorkshop(workshopIndex);
        String now = nowString();
        // 构建产品品牌集合，用于过滤非产品罐（纯水等）
        java.util.Set<String> productBrands = new java.util.HashSet<>();
        for (String[] def : BRAND_DEFS) {
            productBrands.add(def[0]);
        }
        // 每品牌取前 MAX_TANKS_PER_BRAND 个罐（按出现顺序），放入有序 Map
        Map<String, List<TankRealtimeVO>> brandTanks = new LinkedHashMap<>();
        for (String[] def : BRAND_DEFS) {
            brandTanks.put(def[0], new ArrayList<>());
        }
        for (TankRealtimeVO t : tanks) {
            String brand = t.getBrand();
            if (!productBrands.contains(brand)) {
                continue;
            }
            List<TankRealtimeVO> list = brandTanks.get(brand);
            if (list != null && list.size() < MAX_TANKS_PER_BRAND) {
                list.add(t);
            }
        }
        // 展开为行列表
        List<MaterialStatusRowVO> rows = new ArrayList<>();
        for (List<TankRealtimeVO> list : brandTanks.values()) {
            for (TankRealtimeVO t : list) {
                rows.add(new MaterialStatusRowVO()
                        .setBrand(t.getBrand())
                        .setTank(t.getTankId())
                        .setBatch(t.getBatchNo())
                        .setInventory(t.getVolumeM3() != null ? round(t.getVolumeM3(), 1) : 0)
                        .setStatus(t.getStatus())
                        .setUpdatedAt(now));
            }
        }
        log.debug("[CockpitCard] materialStatus ws={} rows={}", workshopIndex, rows.size());
        return new MaterialStatusVO().setUnit("吨").setRows(rows);
    }

    // ===== 能耗（独立 Mock：车间专属固定基准）=====

    /**
     * 获取能耗数据
     * <p>
     * 返回当前车间水/电/压缩空气三项能源的今日消耗（固定 88% 完成率）、计划量、单耗、评价。
     * 评价规则：实际/计划 > 95% → warning，> 100% → alarm。
     * <p>
     * TODO: 真实数据阶段接入能源管理系统（水/电/压缩空气独立计量仪表，与罐体无直接数学关系）。
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 能耗数据列表
     */
    @Override
    public List<EnergyItemVO> getEnergy(int workshopIndex) {
        int idx = clampWorkshop(workshopIndex);
        // 优先查能耗表(当天轮换组);无数据回退固定基准 Mock
        List<CockpitEnergy> enRows = energyMapper.selectList(new QueryWrapper<CockpitEnergy>()
                .eq("stat_date", java.time.LocalDate.now())
                .eq("workshop_index", idx)
                .eq("group_no", currentGroup())
                .eq("del_flag", 0)
                .orderByAsc("energy_type"));
        if (enRows.size() >= 3) {
            String[][] defs = {{"生产水", "m³"}, {"电", "kWh"}, {"压缩空气", "m³"}};
            List<EnergyItemVO> dbItems = new ArrayList<>();
            for (CockpitEnergy r : enRows) {
                int t = r.getEnergyType() == null ? 1 : r.getEnergyType();
                if (t < 1 || t > 3 || r.getValue() == null) continue;
                double plan = r.getPlan() == null ? 1.0 : r.getPlan();
                double ratio = r.getValue() / plan;
                String state = ratio > 1.0 ? "alarm" : ratio > 0.95 ? "warning" : "normal";
                dbItems.add(new EnergyItemVO()
                        .setName(defs[t - 1][0])
                        .setValue(round(r.getValue(), 1))
                        .setUnit(defs[t - 1][1])
                        .setPlan(round(plan, 0))
                        .setPerUnit(String.valueOf(round(r.getValue() / 333.0, 2)))
                        .setState(state));
            }
            if (dbItems.size() == 3) {
                return dbItems;
            }
        }
        double[] planned = ENERGY_PLANNED_BY_WS[idx];
        // 能耗定义：名称、单位
        String[][] energyDefs = {
                {"生产水", "m³"},
                {"电",     "kWh"},
                {"压缩空气", "m³"},
        };
        List<EnergyItemVO> items = new ArrayList<>();
        for (int i = 0; i < energyDefs.length; i++) {
            double plan = planned[i];
            // 实际量 = 计划量 × 固定完成率（无随机波动）
            double value = round(plan * ENERGY_ACTUAL_RATIO, 1);
            // 单耗 = 今日消耗 / 产量(假设333吨)
            String perUnit = String.valueOf(round(value / 333.0, 2));
            // 评价：超计划95%预警，超100%报警
            double ratio = value / plan;
            String state = ratio > 1.0 ? "alarm" : (ratio > 0.95 ? "warning" : "normal");
            items.add(new EnergyItemVO()
                    .setName(energyDefs[i][0])
                    .setValue(value)
                    .setUnit(energyDefs[i][1])
                    .setPlan(plan)
                    .setPerUnit(perUnit)
                    .setState(state));
        }
        return items;
    }

    // ===== 储罐风险（同源派生：统计当前车间罐体状态）=====

    /**
     * 获取储罐风险数据
     * <p>
     * 遍历当前车间罐体，按 status 统计运行/清洗/预警数量，计算平均装载率。
     * 与 3D 罐体视图完全同源，接入真实数据源后逻辑不变。
     * <p>
     * 状态映射：normal → 运行中，standby → 待检/清洗，warning → 预警。
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 储罐风险统计数据
     */
    @Override
    public TankRiskVO getTankRisk(int workshopIndex) {
        int idxRk = clampWorkshop(workshopIndex);
        // 优先查风险表(当天轮换组);无数据回退罐体实时统计
        CockpitTankRisk rk = tankRiskMapper.selectOne(new QueryWrapper<CockpitTankRisk>()
                .eq("stat_date", java.time.LocalDate.now())
                .eq("workshop_index", idxRk)
                .eq("group_no", currentGroup())
                .eq("del_flag", 0).last("LIMIT 1"));
        if (rk != null && rk.getRunning() != null) {
            return new TankRiskVO()
                    .setRunning(rk.getRunning())
                    .setIdle(rk.getIdle() == null ? 0 : rk.getIdle())
                    .setCleaning(rk.getCleaning() == null ? 0 : rk.getCleaning())
                    .setAlarm(rk.getAlarm() == null ? 0 : rk.getAlarm())
                    .setLoadingRate(rk.getLoadingRate() == null ? 0 : round(rk.getLoadingRate(), 1));
        }
        List<TankRealtimeVO> tanks = getTanksByWorkshop(workshopIndex);
        int running = 0;
        int cleaning = 0;
        int alarm = 0;
        double levelSum = 0;
        int levelCount = 0;
        for (TankRealtimeVO t : tanks) {
            String status = t.getStatus();
            if ("standby".equals(status)) {
                cleaning++;
            } else if ("warning".equals(status) || "alarm".equals(status)) {
                alarm++;
            } else {
                // normal 及其余均计为运行中
                running++;
            }
            if (t.getLevelPct() != null) {
                levelSum += t.getLevelPct();
                levelCount++;
            }
        }
        // 平均装载率 = 当前车间罐体液位平均值
        double loadingRate = levelCount > 0 ? round(levelSum / levelCount, 1) : 0;
        log.debug("[CockpitCard] tankRisk ws={} total={} running={} cleaning={} alarm={} loadingRate={}",
                workshopIndex, tanks.size(), running, cleaning, alarm, loadingRate);
        return new TankRiskVO()
                .setRunning(running)
                .setIdle(0)
                .setCleaning(cleaning)
                .setAlarm(alarm)
                .setLoadingRate(loadingRate);
    }

    // ===== 室内环境（influx 点位真值优先，无数据回落车间固定基准）=====

    /** 环境温度点位编码基准（cnl_num = 1001~1004 按车间）；湿度 = 1011~1014 */
    private static final int ENV_TEMP_CODE_BASE = 1001;
    private static final int ENV_HUMI_CODE_BASE = 1011;

    /**
     * 获取室内环境数据
     * <p>
     * 温度/湿度优先取 influx 环境点位（基础点位表 1001~1004/1011~1014）最近值，
     * 点位无数据时回落 ENVIRONMENT_BY_WS 固定基准。阈值配置全车间一致。
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 室内环境数据
     */
    @Override
    public EnvironmentVO getEnvironment(int workshopIndex) {
        int idx = clampWorkshop(workshopIndex);
        double[] env = ENVIRONMENT_BY_WS[idx];
        Double temperature = queryEnvPoint(ENV_TEMP_CODE_BASE + idx);
        Double humidity = queryEnvPoint(ENV_HUMI_CODE_BASE + idx);
        log.debug("[CockpitEnv] ws={} temperature={} humidity={}", idx, temperature, humidity);
        return new EnvironmentVO()
                .setTemperature(temperature != null ? Math.round(temperature * 10) / 10.0 : env[0])
                .setHumidity(humidity != null ? Math.round(humidity * 10) / 10.0 : env[1])
                .setTemperatureRange(Arrays.asList(18, 28))
                .setHumidityRange(Arrays.asList(40, 70))
                .setTemperatureScale(Arrays.asList(0, 40))
                .setHumidityScale(Arrays.asList(0, 100))
                .setTemperatureWarning(26)
                .setTemperatureAlarm(28)
                .setHumidityWarning(65)
                .setHumidityAlarm(70);
    }

    /**
     * 查询单个环境点位在 influx 的最近值(近 10 分钟 LAST)
     *
     * @param code 点位编码(cnl_num)
     * @return 最新值;点位无数据或查询异常返回 null
     */
    private Double queryEnvPoint(int code) {
        try {
            LocalDateTime end = LocalDateTime.now();
            Map<String, List<FluxZone8Data>> map = emsInfluxDbService.queryMultiPointsWithIntervalLastMap(
                    List.of(String.valueOf(code)), end.minusMinutes(10), end, 60);
            List<FluxZone8Data> list = map.get(String.valueOf(code));
            return (list == null || list.isEmpty()) ? null : list.get(list.size() - 1).getValue();
        } catch (Exception e) {
            log.warn("[CockpitEnv] influx 环境点位 {} 查询失败,回退固定基准: {}", code, e.getMessage());
            return null;
        }
    }

    // ===== 今日调拨执行（近似派生：汇总当前车间罐体进出量）=====

    /**
     * 获取今日调拨执行数据
     * <p>
     * 汇总当前车间罐体的 inbound/outbound，保证调拨数据与罐体活动联动。
     * <p>
     * TODO: 真实数据阶段替换为 MES 调拨单累计查询。
     * 罐体 inbound 是瞬时流量，调拨单是日累计值，两者维度不同。
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 今日调拨执行数据
     */
    @Override
    public TransferExecutionVO getTransfer(int workshopIndex) {
        int idx = clampWorkshop(workshopIndex);
        // 优先查今日调拨表(当天轮换组);无数据回退罐体派生
        CockpitTransferDaily d = transferDailyMapper.selectOne(new QueryWrapper<CockpitTransferDaily>()
                .eq("stat_date", java.time.LocalDate.now())
                .eq("workshop_index", idx)
                .eq("group_no", currentGroup())
                .eq("del_flag", 0).last("LIMIT 1"));
        if (d != null && d.getInbound() != null && d.getOutbound() != null) {
            double inbound = d.getInbound(), outbound = d.getOutbound();
            double ip = d.getInboundPlan() != null ? d.getInboundPlan() : 1.0;
            double op = d.getOutboundPlan() != null ? d.getOutboundPlan() : 1.0;
            return new TransferExecutionVO()
                    .setInbound(round(inbound, 1))
                    .setOutbound(round(outbound, 1))
                    .setInboundPlan(round(ip, 1))
                    .setOutboundPlan(round(op, 1))
                    .setNet(round(inbound - outbound, 1))
                    .setCompletion(round((inbound + outbound) / (ip + op) * 100, 1));
        }
        List<TankRealtimeVO> tanks = getTanksByWorkshop(workshopIndex);
        double inboundSum = 0;
        double outboundSum = 0;
        for (TankRealtimeVO t : tanks) {
            inboundSum += t.getInbound() != null ? t.getInbound() : 0;
            outboundSum += t.getOutbound() != null ? t.getOutbound() : 0;
        }
        double inbound = round(inboundSum, 1);
        double outbound = round(outboundSum, 1);
        double net = round(inbound - outbound, 1);
        // 计划量按当前总量反推，使完成率维持在 ~87%（Mock 近似）
        double inboundPlan = round(inbound / 0.87, 1);
        double outboundPlan = round(outbound / 0.87, 1);
        double completion = round((inbound + outbound) / (inboundPlan + outboundPlan) * 100, 0);
        log.debug("[CockpitCard] transfer ws={} inbound={} outbound={} completion={}",
                workshopIndex, inbound, outbound, completion);
        return new TransferExecutionVO()
                .setInbound(inbound)
                .setOutbound(outbound)
                .setInboundPlan(inboundPlan)
                .setOutboundPlan(outboundPlan)
                .setNet(net)
                .setCompletion(completion);
    }

    // ===== 趋势数据（同源派生：终点锚定当前车间罐体平均值）=====

    /**
     * 获取趋势数据
     * <p>
     * 生成 6h/12h/24h/48h 四个时段的液位和温度时序数据。
     * 最后一个数据点锚定当前车间罐体平均液位/温度，保证趋势终点与罐体实时状态一致。
     * 接入真实数据源后逻辑不变（历史段替换为 InfluxDB 时序查询）。
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 趋势数据
     */
    @Override
    public TrendVO getTrend(int workshopIndex) {
        List<TankRealtimeVO> tanks = getTanksByWorkshop(workshopIndex);
        // 计算当前车间罐体平均液位/温度，作为趋势终点锚定值
        double avgLevel = tanks.stream()
                .filter(t -> t.getLevelPct() != null)
                .mapToDouble(TankRealtimeVO::getLevelPct)
                .average().orElse(72.0);
        double avgTemp = tanks.stream()
                .filter(t -> t.getTemperatureC() != null)
                .mapToDouble(TankRealtimeVO::getTemperatureC)
                .average().orElse(22.0);
        return new TrendVO()
                .setCurrent("12h")
                .setLiquidNormalRange(Arrays.asList(60, 90))
                .setTemperatureNormalRange(Arrays.asList(18, 28))
                .setPeriods(new TrendPeriodsVO()
                        .setT6h(buildTrendPoint(13, new String[]{"-6h", "-5h", "-4h", "-3h", "-2h", "-1h", "现在"}, avgLevel, avgTemp))
                        .setT12h(buildTrendPoint(16, new String[]{"-12h", "-10h", "-8h", "-6h", "-4h", "-2h", "现在"}, avgLevel, avgTemp))
                        .setT24h(buildTrendPoint(16, new String[]{"-24h", "-20h", "-16h", "-12h", "-8h", "-4h", "现在"}, avgLevel, avgTemp))
                        .setT48h(buildTrendPoint(16, new String[]{"-48h", "-40h", "-32h", "-24h", "-16h", "-8h", "现在"}, avgLevel, avgTemp)));
    }

    // ===== 内部工具方法 =====

    /**
     * 生成单个时段的趋势数据点（液位+温度）
     * <p>
     * 最后一个数据点使用当前车间罐体平均值，其余点使用围绕平均值的固定波动序列
     * （无随机，保证同一车间每次返回一致）。
     * TODO: 真实数据阶段历史段替换为 InfluxDB 时序查询。
     *
     * @param pointCount 数据点数量
     * @param labels     X轴标签（7个，前端均匀分布）
     * @param endLevel   终点液位（当前车间平均值）
     * @param endTemp    终点温度（当前车间平均值）
     * @return 该时段的趋势数据点
     */
    private TrendPeriodsVO.TrendPointVO buildTrendPoint(int pointCount, String[] labels, double endLevel, double endTemp) {
        // 固定波动偏移序列（相对终点），保证历史曲线形态稳定、无随机跳变
        double[] levelOffsets = {-6, -4, -2, -5, -3, -1, 0};
        double[] tempOffsets  = {-1.5, -1.0, -0.5, -1.2, -0.8, -0.3, 0};
        List<Double> liquid = new ArrayList<>(pointCount);
        List<Double> temperature = new ArrayList<>(pointCount);
        for (int i = 0; i < pointCount; i++) {
            if (i == pointCount - 1) {
                // 最后一个数据点锚定当前车间罐体平均值
                liquid.add(round(endLevel, 1));
                temperature.add(round(endTemp, 1));
            } else {
                // 使用固定偏移序列循环取值（pointCount 可能 > offsets.length）
                int oi = i % levelOffsets.length;
                liquid.add(round(endLevel + levelOffsets[oi], 1));
                temperature.add(round(endTemp + tempOffsets[oi], 1));
            }
        }
        return new TrendPeriodsVO.TrendPointVO()
                .setLiquid(liquid)
                .setTemperature(temperature)
                .setLabels(Arrays.asList(labels));
    }

    /**
     * 根据计划量基准生成调拨明细列表
     * <p>
     * 实际量 = 计划量 × 固定完成率（按车间差异化，无随机波动）
     *
     * @param plannedBase   各品牌计划量基准数组
     * @param workshopIndex 车间索引（用于确定完成率）
     * @return 调拨明细列表
     */
    private List<TransferItemVO> buildTransferItems(double[] plannedBase, int workshopIndex) {
        // 各车间固定完成率（85%~92%区间，无随机）
        double[] actualRatioByWs = {0.88, 0.90, 0.87, 0.86};
        double actualRatio = actualRatioByWs[clampWorkshop(workshopIndex)];
        List<TransferItemVO> items = new ArrayList<>();
        for (int i = 0; i < BRAND_DEFS.length; i++) {
            double planned = plannedBase[i];
            // 实际量 = 计划量 × 固定完成率
            double actual = round(planned * actualRatio, 1);
            items.add(new TransferItemVO()
                    .setName(BRAND_DEFS[i][0])
                    .setShortName(BRAND_DEFS[i][1])
                    .setPlanned(planned)
                    .setActual(actual));
        }
        return items;
    }

    /**
     * 对列表中某字段求和
     */
    private double sum(List<TransferItemVO> items, java.util.function.ToDoubleFunction<TransferItemVO> extractor) {
        return items.stream().mapToDouble(extractor).sum();
    }

    /** 车间索引越界归 0 */
    private int clampWorkshop(int workshopIndex) {
        return (workshopIndex < 0 || workshopIndex >= WORKSHOP_COUNT) ? 0 : workshopIndex;
    }

    /** 四舍五入保留指定小数位 */
    // ===== MySQL 数据源辅助(2026-09-10) =====

    /** 轮换组号:每 5 分钟换一组(0~4),与前端卡片轮询同频,制造动态效果 */
    private int currentGroup() {
        return (int) ((System.currentTimeMillis() / 300000) % 5);
    }

    /** 品牌颜色(BRAND_DEFS 查表,查不到返回 null 由前端用默认色板) */
    private String brandColor(String brandName) {
        if (brandName == null) {
            return null;
        }
        for (String[] def : BRAND_DEFS) {
            if (brandName.equals(def[0])) {
                return def[2];
            }
        }
        return null;
    }

    /** 品牌简称(BRAND_DEFS 查表) */
    private String brandShortName(String brandName) {
        for (String[] def : BRAND_DEFS) {
            if (brandName.equals(def[0])) {
                return def[1];
            }
        }
        return brandName;
    }

    /**
     * 查月度调拨表(年+月+车间,按品牌 GROUP 后在内存组装)
     *
     * @return 组装好的 VO;当月无数据返回 null(调用方回退 Mock)
     */
    private TransferPlanVO queryTransferPlan(int workshopIndex, int year, int month, double axisMaxFallback) {
        List<CockpitTransferPlan> rows = transferPlanMapper.selectList(new QueryWrapper<CockpitTransferPlan>()
                .eq("stat_year", year)
                .eq("stat_month", month)
                .eq("workshop_index", workshopIndex)
                .eq("del_flag", 0));
        if (rows.isEmpty()) {
            return null;
        }
        List<TransferItemVO> items = new ArrayList<>();
        double totalPlanned = 0, totalActual = 0;
        for (CockpitTransferPlan r : rows) {
            double planned = r.getPlanned() == null ? 0 : r.getPlanned();
            double actual = r.getActual() == null ? 0 : r.getActual();
            totalPlanned += planned;
            totalActual += actual;
            items.add(new TransferItemVO()
                    .setName(r.getBrand())
                    .setShortName(brandShortName(r.getBrand()))
                    .setPlanned(round(planned, 1))
                    .setActual(round(actual, 1)));
        }
        if (totalPlanned <= 0) {
            return null;
        }
        return new TransferPlanVO()
                .setCompletion(round(totalActual / totalPlanned * 100, 1))
                .setVariance(round(totalActual - totalPlanned, 1))
                .setAxisMax(round(Math.max(axisMaxFallback, totalPlanned * 1.15), 0))
                .setUnit("吨")
                .setItems(items);
    }

    /** 查调拨表按年聚合(12 个月 SUM,品牌维度) */
    private TransferPlanVO queryTransferPlanYear(int workshopIndex, int year, double axisMaxFallback) {
        List<CockpitTransferPlan> rows = transferPlanMapper.selectList(new QueryWrapper<CockpitTransferPlan>()
                .eq("stat_year", year)
                .eq("workshop_index", workshopIndex)
                .eq("del_flag", 0));
        if (rows.isEmpty()) {
            return null;
        }
        // 按品牌聚合 12 个月
        java.util.Map<String, double[]> byBrand = new java.util.LinkedHashMap<>();
        for (CockpitTransferPlan r : rows) {
            double[] acc = byBrand.computeIfAbsent(r.getBrand(), k -> new double[2]);
            acc[0] += r.getPlanned() == null ? 0 : r.getPlanned();
            acc[1] += r.getActual() == null ? 0 : r.getActual();
        }
        List<TransferItemVO> items = new ArrayList<>();
        double totalPlanned = 0, totalActual = 0;
        for (java.util.Map.Entry<String, double[]> e : byBrand.entrySet()) {
            totalPlanned += e.getValue()[0];
            totalActual += e.getValue()[1];
            items.add(new TransferItemVO()
                    .setName(e.getKey())
                    .setShortName(brandShortName(e.getKey()))
                    .setPlanned(round(e.getValue()[0], 1))
                    .setActual(round(e.getValue()[1], 1)));
        }
        if (totalPlanned <= 0) {
            return null;
        }
        return new TransferPlanVO()
                .setCompletion(round(totalActual / totalPlanned * 100, 1))
                .setVariance(round(totalActual - totalPlanned, 1))
                .setAxisMax(round(Math.max(axisMaxFallback, totalPlanned * 1.1), 0))
                .setUnit("吨")
                .setItems(items);
    }

    /**
     * 品牌库存趋势(日粒度,品牌库存趋势面板数据源)
     *
     * @param startDate yyyy-MM-dd(可空=默认近 90 天)
     * @param endDate   yyyy-MM-dd(可空=今天)
     */
    @Override
    public List<InventoryTrendPointVO> getInventoryTrend(int workshopIndex, String startDate, String endDate) {
        int idx = clampWorkshop(workshopIndex);
        java.time.LocalDate start = startDate == null ? java.time.LocalDate.now().minusDays(89)
                : java.time.LocalDate.parse(startDate);
        java.time.LocalDate end = endDate == null ? java.time.LocalDate.now() : java.time.LocalDate.parse(endDate);
        List<CockpitInventoryTrend> rows = inventoryTrendMapper.selectList(new QueryWrapper<CockpitInventoryTrend>()
                .eq("workshop_index", idx)
                .eq("del_flag", 0)
                .ge("stat_date", start)
                .le("stat_date", end)
                .orderByAsc("stat_date"));
        // 组装:date -> (brand -> quantity)
        java.util.Map<java.time.LocalDate, java.util.List<InventoryTrendItemVO>> byDate = new java.util.TreeMap<>();
        for (CockpitInventoryTrend r : rows) {
            if (r.getQuantity() == null) continue;
            byDate.computeIfAbsent(r.getStatDate(), k -> new ArrayList<>())
                    .add(new InventoryTrendItemVO().setName(r.getBrand()).setQuantity(round(r.getQuantity(), 1)));
        }
        List<InventoryTrendPointVO> points = new ArrayList<>();
        for (java.util.Map.Entry<java.time.LocalDate, java.util.List<InventoryTrendItemVO>> e : byDate.entrySet()) {
            points.add(new InventoryTrendPointVO().setDate(e.getKey().toString()).setItems(e.getValue()));
        }
        return points;
    }

    private double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }

    /** 当前时间格式化为 yyyy-MM-dd HH:mm:ss */
    private String nowString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
