package com.keytech.modules.maotai.cockpit.service;

import com.keytech.modules.maotai.cockpit.vo.EnergyItemVO;
import com.keytech.modules.maotai.cockpit.vo.InventoryTrendPointVO;
import com.keytech.modules.maotai.cockpit.vo.EnvironmentVO;
import com.keytech.modules.maotai.cockpit.vo.InventoryVO;
import com.keytech.modules.maotai.cockpit.vo.MaterialStatusVO;
import com.keytech.modules.maotai.cockpit.vo.TankRiskVO;
import com.keytech.modules.maotai.cockpit.vo.TransferExecutionVO;
import com.keytech.modules.maotai.cockpit.vo.TransferPlanVO;
import com.keytech.modules.maotai.cockpit.vo.TrendVO;

import java.util.List;

/**
 *
 * 驾驶舱卡片数据服务接口
 * <p>
 * 提供月度调拨、年度调拨、品牌库存、实时物料、能耗、储罐风险、
 * 室内环境、今日调拨、趋势等驾驶舱全量卡片聚合数据。
 * <p>
 * 所有卡片数据按车间隔离：5 个派生卡片基于当前车间罐体计算，
 * 4 个 Mock 卡片使用车间专属固定基准值。切换车间后全部刷新。
 *
 * @author keytech
 * @since 2026-07-29
 */
public interface ICockpitCardService {

    /**
     * 获取月度调拨数据
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 各品牌月度计划/实际调拨量、完成率、偏差
     */
    TransferPlanVO getMonthlyTransfer(int workshopIndex, Integer month);

    /**
     * 获取年度调拨数据
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 各品牌年度计划/实际调拨量、完成率、偏差
     */
    TransferPlanVO getAnnualTransfer(int workshopIndex);

    /**
     * 获取品牌库存数据
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 各品牌当前库存量、占比、颜色
     */
    InventoryVO getInventory(int workshopIndex);

    /**
     * 获取实时物料数据
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 各罐区品牌-批次-库存-状态明细
     */
    MaterialStatusVO getMaterialStatus(int workshopIndex);

    /**
     * 获取能耗数据
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 水/电/气今日消耗、计划、单耗
     */
    List<EnergyItemVO> getEnergy(int workshopIndex);

    /**
     * 品牌库存趋势(日粒度,mes3_maotai_cockpit_inventory_trend)
     *
     * @param workshopIndex 车间索引(0~3)
     * @param startDate     开始日期 yyyy-MM-dd(空=近90天)
     * @param endDate       结束日期 yyyy-MM-dd(空=今天)
     */
    List<InventoryTrendPointVO> getInventoryTrend(int workshopIndex, String startDate, String endDate);

    /**
     * 获取储罐风险数据
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 运行/空闲/清洗/预警罐数及平均装载率
     */
    TankRiskVO getTankRisk(int workshopIndex);

    /**
     * 获取室内环境数据
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 温度、湿度及其正常范围、预警/报警阈值
     */
    EnvironmentVO getEnvironment(int workshopIndex);

    /**
     * 获取今日调拨执行数据
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 入罐/出罐量、计划量、净流入、完成率
     */
    TransferExecutionVO getTransfer(int workshopIndex);

    /**
     * 获取趋势数据
     *
     * @param workshopIndex 车间索引（0~3）
     * @return 6h/12h/24h/48h 四个时段的液位和温度时序
     */
    TrendVO getTrend(int workshopIndex);
}
