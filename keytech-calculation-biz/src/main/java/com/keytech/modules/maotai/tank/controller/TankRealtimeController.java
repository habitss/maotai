package com.keytech.modules.maotai.tank.controller;

import com.keytech.modules.maotai.tank.service.ITankRealtimeService;
import com.keytech.modules.maotai.tank.vo.TankRealtimeListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 3D罐体实时数据接口
 * <p>
 * 为前端 workshop.html 的 Three.js 罐体模型提供实时数据。
 * 前端通过 updateTankRealtimeData(payload) 消费本接口返回的数据。
 * 当前阶段返回 Mock 数据，后续接入真实数据源后无需改接口契约。
 *
 *
 * @author keytech
 * @since 2026-07-28
 */
@Tag(name = "3D罐体实时数据")
@RestController
@RequestMapping("/maotai/tankRealtime")
@Slf4j
@RequiredArgsConstructor
public class TankRealtimeController {

    private final ITankRealtimeService tankRealtimeService;

    /**
     * 按车间索引获取罐体实时数据列表
     * @param workshopIndex 车间索引（0=中转酒罐车间,1=系列酒勾调厂房,2=成品酒罐房,3=包装厂房）
     * @return 罐体实时数据
     */
    @Operation(summary = "3D罐体实时数据-按车间查询", description = "返回指定车间的全部罐体实时数据（当前为Mock数据）")
    @GetMapping("/list")
    public Result<TankRealtimeListVO> list(
            @RequestParam(name = "workshopIndex", defaultValue = "0") Integer workshopIndex) {
        return Result.OK(tankRealtimeService.listByWorkshop(workshopIndex));
    }
}
