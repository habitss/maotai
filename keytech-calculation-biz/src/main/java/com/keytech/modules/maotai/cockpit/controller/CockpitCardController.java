package com.keytech.modules.maotai.cockpit.controller;

import com.keytech.modules.maotai.cockpit.service.ICockpitCardService;
import com.keytech.modules.maotai.cockpit.vo.EnergyItemVO;
import com.keytech.modules.maotai.cockpit.vo.InventoryTrendPointVO;
import com.keytech.modules.maotai.cockpit.vo.EnvironmentVO;
import com.keytech.modules.maotai.cockpit.vo.InventoryVO;
import com.keytech.modules.maotai.cockpit.vo.MaterialStatusVO;
import com.keytech.modules.maotai.cockpit.vo.TankRiskVO;
import com.keytech.modules.maotai.cockpit.vo.TransferExecutionVO;
import com.keytech.modules.maotai.cockpit.vo.TransferPlanVO;
import com.keytech.modules.maotai.cockpit.vo.TrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 * 驾驶舱卡片数据接口
 * <p>
 * 为前端 workshop-cockpit.html 的各类业务卡片提供聚合数据：
 * - 左侧：月度调拨柱状图
 * - 右侧：年度调拨柱状图、实时物料状态表
 * - 品牌库存环图
 * - 底部：储罐风险（含装载率）、能耗（水/电/气）
 * <p>
 * 所有接口均接收 workshopIndex 参数（0~3），按当前车间隔离返回数据。
 *
 * @author keytech
 * @since 2026-07-29
 */
@Tag(name = "驾驶舱卡片数据")
@RestController
@RequestMapping("/maotai/cockpitCard")
@Slf4j
@RequiredArgsConstructor
public class CockpitCardController {

    private final ICockpitCardService cockpitCardService;

    /**
     * 获取月度调拨数据
     *
     * @param workshopIndex 车间索引（0~3，默认0）
     * @return 各品牌月度计划/实际调拨量、完成率、偏差
     */
    @Operation(summary = "月度调拨数据", description = "按车间返回本月各品牌计划/实际调拨量")
    @GetMapping("/monthlyTransfer")
    public Result<TransferPlanVO> monthlyTransfer(
            @Parameter(description = "车间索引 0~3") @RequestParam(defaultValue = "0") Integer workshopIndex,
            @Parameter(description = "月份1~12,空=当月") @RequestParam(name = "month", required = false) Integer month) {
        return Result.OK(cockpitCardService.getMonthlyTransfer(workshopIndex, month));
    }

    /**
     * 获取年度调拨数据
     *
     * @param workshopIndex 车间索引（0~3，默认0）
     * @return 各品牌年度计划/实际调拨量、完成率、偏差
     */
    @Operation(summary = "年度调拨数据", description = "按车间返回本年各品牌计划/实际调拨量")
    @GetMapping("/annualTransfer")
    public Result<TransferPlanVO> annualTransfer(
            @Parameter(description = "车间索引 0~3") @RequestParam(defaultValue = "0") Integer workshopIndex) {
        return Result.OK(cockpitCardService.getAnnualTransfer(workshopIndex));
    }

    /**
     * 获取品牌库存数据
     *
     * @param workshopIndex 车间索引（0~3，默认0）
     * @return 各品牌当前库存量、占比、颜色
     */
    @Operation(summary = "品牌库存数据", description = "按车间返回各品牌当前库存量及占比")
    @GetMapping("/inventory")
    public Result<InventoryVO> inventory(
            @Parameter(description = "车间索引 0~3") @RequestParam(defaultValue = "0") Integer workshopIndex) {
        return Result.OK(cockpitCardService.getInventory(workshopIndex));
    }

    /**
     * 获取实时物料数据
     *
     * @param workshopIndex 车间索引（0~3，默认0）
     * @return 各罐区品牌-批次-库存-状态明细
     */
    @Operation(summary = "实时物料数据", description = "按车间返回各罐区品牌-批次-库存-状态")
    @GetMapping("/materialStatus")
    public Result<MaterialStatusVO> materialStatus(
            @Parameter(description = "车间索引 0~3") @RequestParam(defaultValue = "0") Integer workshopIndex) {
        return Result.OK(cockpitCardService.getMaterialStatus(workshopIndex));
    }

    /**
     * 获取能耗数据
     *
     * @param workshopIndex 车间索引（0~3，默认0）
     * @return 水/电/气今日消耗、计划、单耗
     */
    @Operation(summary = "品牌库存趋势(日粒度)", description = "mes3_maotai_cockpit_inventory_trend,支持日期范围过滤")
    @GetMapping("/inventoryTrend")
    public Result<List<InventoryTrendPointVO>> getInventoryTrend(@RequestParam(defaultValue = "0") Integer workshopIndex,
                                                                 @RequestParam(required = false) String startDate,
                                                                 @RequestParam(required = false) String endDate) {
        return Result.OK(cockpitCardService.getInventoryTrend(workshopIndex, startDate, endDate));
    }

    @Operation(summary = "能耗数据", description = "按车间返回水/电/气今日消耗、计划、单耗")
    @GetMapping("/energy")
    public Result<List<EnergyItemVO>> energy(
            @Parameter(description = "车间索引 0~3") @RequestParam(defaultValue = "0") Integer workshopIndex) {
        return Result.OK(cockpitCardService.getEnergy(workshopIndex));
    }

    /**
     * 获取储罐风险数据
     *
     * @param workshopIndex 车间索引（0~3，默认0）
     * @return 运行/空闲/清洗/预警罐数及平均装载率
     */
    @Operation(summary = "储罐风险数据", description = "按车间返回运行/空闲/清洗/预警罐数及装载率")
    @GetMapping("/tankRisk")
    public Result<TankRiskVO> tankRisk(
            @Parameter(description = "车间索引 0~3") @RequestParam(defaultValue = "0") Integer workshopIndex) {
        return Result.OK(cockpitCardService.getTankRisk(workshopIndex));
    }

    /**
     * 获取室内环境数据
     *
     * @param workshopIndex 车间索引（0~3，默认0）
     * @return 温度、湿度及其正常范围、预警/报警阈值
     */
    @Operation(summary = "室内环境数据", description = "按车间返回温湿度及阈值配置")
    @GetMapping("/environment")
    public Result<EnvironmentVO> environment(
            @Parameter(description = "车间索引 0~3") @RequestParam(defaultValue = "0") Integer workshopIndex) {
        return Result.OK(cockpitCardService.getEnvironment(workshopIndex));
    }

    /**
     * 获取今日调拨执行数据
     *
     * @param workshopIndex 车间索引（0~3，默认0）
     * @return 入罐/出罐量、计划量、净流入、完成率
     */
    @Operation(summary = "今日调拨执行数据", description = "按车间返回入罐/出罐量及完成率")
    @GetMapping("/transfer")
    public Result<TransferExecutionVO> transfer(
            @Parameter(description = "车间索引 0~3") @RequestParam(defaultValue = "0") Integer workshopIndex) {
        return Result.OK(cockpitCardService.getTransfer(workshopIndex));
    }

    /**
     * 获取趋势数据
     *
     * @param workshopIndex 车间索引（0~3，默认0）
     * @return 6h/12h/24h/48h 四个时段的液位和温度时序
     */
    @Operation(summary = "趋势数据", description = "按车间返回多时段液位与温度时序数据")
    @GetMapping("/trend")
    public Result<TrendVO> trend(
            @Parameter(description = "车间索引 0~3") @RequestParam(defaultValue = "0") Integer workshopIndex) {
        return Result.OK(cockpitCardService.getTrend(workshopIndex));
    }
}
