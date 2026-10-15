package com.keytech.modules.line.efficiency.service;

import com.keytech.modules.line.efficiency.dto.ProductionDto;
import com.keytech.modules.line.efficiency.entity.LineEfficiencyHandle;
import com.keytech.base.service.BaseService;

import java.time.LocalDateTime;

/**
 * @Description: 线效率-产量/线效状态点位配置
 * @Author: jeecg-boot
 * @Date:   2025-05-26
 * @Version: V1.0
 */
public interface ILineEfficiencyHandleService extends BaseService<LineEfficiencyHandle> {

    /**
     * 开始计算线效状态
     */
    void lineEfficiencyState();

    /**
     * 设备运行时长（按照设备开机信号点来进行计算，计算出每次开机关机的时间）
     */
    void machineMinute();

    /**
     * 报警（按照报警信号点来进行计算，计算出每次报警开始的时间）
     */
    void alarmMinute();

    /**
     * 点位运行状态（按照点位信号点来进行计算，计算出每次运行开始的时间）
     */
    void operatingDurationForPoint();

    /**
     * 查询当前时间的外排水数据，然后发送给第三方
     */
    void queryEhsData();

    /**
     * 开始计算线效状态-历史计算
     * @param startLocalDateTime 开始时间
     * @param endLocalDateTime 结束时间
     */
    void lineEfficiencyStateHistory(LocalDateTime startLocalDateTime, LocalDateTime endLocalDateTime);

    /**
     * 开始运行时长
     * @param startLocalDateTime 开始时间
     * @param endLocalDateTime 结束时间
     */
    void machineMinuteHistory(LocalDateTime startLocalDateTime,LocalDateTime endLocalDateTime);

    /**
     * 报警（按照报警信号点来进行计算，计算出每次报警开始的时间）
     * @param startLocalDateTime 开始时间
     * @param endLocalDateTime 结束时间
     */
    void alarmMinuteHistory(LocalDateTime startLocalDateTime,LocalDateTime endLocalDateTime);

    /**
     * 防清零计算逻辑。传入时间和产量点位列表，获取区间内的增量产量，当limits限制产量不为空，返回达到限制产量的一瞬间的数据，如果为空，则返回时间内的产量总和
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pointDto 点位
     * @param limits 限制产量，大于限制产量即返回
     * @return
     */
    ProductionDto getProductionByTime(LocalDateTime startTime, LocalDateTime endTime, ProductionDto pointDto, Double limits);
}
