package com.keytech.modules.ems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.ems.constants.DataPeriodEnum;
import com.keytech.modules.ems.constants.ShiftEnum;
import com.keytech.modules.ems.entity.*;
import com.keytech.modules.ems.mapper.*;
import com.keytech.modules.ems.service.*;
import com.keytech.modules.ems.util.UsageDataServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 能源模块 聚合业务类
 * @author yepeng
 * @since 2025.05.13
 * */
@Slf4j
@Service
@RequiredArgsConstructor
public class AggregationCalcService {
    private final IShiftDayPeriodSetService shiftDayPeriodSetService;
    private final CollectClassUsageMapper collectClassUsageMapper;
    private final CollectDayUsageMapper collectDayUsageMapper;
    private final OrsdataClassUsageMapper orsdataClassUsageMapper;
    private final ThirdpartyClassUsageMapper thirdpartyClassUsageMapper;
    private final ThirdpartyDayUsageMapper thirdpartyDayUsageMapper;
    private final OrsdataHourUsageMapper orsdataHourUsageMapper;
    private final OrsdataDayUsageMapper orsdataDayUsageMapper;
    private final OrsdataWeekUsageMapper orsdataWeekUsageMapper;
    private final OrsdataMtdUsageMapper orsdataMtdUsageMapper;
    private final OrsdataYtdUsageMapper orsdataYtdUsageMapper;
    private final UsageDataServiceHelper usageDataServiceHelper;

    /**
     * 批量写成功聚合的采集班数据(先删除再添加)
     * @param usageList 用量值
     */
    public void batchSaveOrUpdateCollectClassUsage(List<CollectClassUsage> usageList) {
        //班数据需要根据时间算出班次
        Map<Integer, Integer> endHour2ShiftMap = getEndHour2ShiftMap();
        for (CollectClassUsage usage: usageList) {
            usage.setShift(ShiftEnum.getNameByVal(endHour2ShiftMap.get(usage.getEtime().getHour())));
        }
        //再批量插入
        this.usageDataServiceHelper.batchInsertOrUpdateWithRetry(usageList, collectClassUsageMapper::batchInsertOrUpdate, "采集表班数据");
    }

    /**
     * 批量写成功聚合的中间点位班数据
     * @param usageList 用量值
     */
    public void batchSaveOrUpdateOrsDataClassUsage(List<OrsdataClassUsage> usageList) {
        //班数据需要根据时间算出班次
        Map<Integer, Integer> endHour2ShiftMap = getEndHour2ShiftMap();
        for (OrsdataClassUsage usage: usageList) {
            usage.setShift(ShiftEnum.getNameByVal(endHour2ShiftMap.get(usage.getEtime().getHour())));
        }
        //再批量插入
        this.usageDataServiceHelper.batchInsertOrUpdateWithRetry(usageList, orsdataClassUsageMapper::batchInsertOrUpdate, "中间表班数据");
    }

    /**
     * 批量写成功聚合的第三方班数据
     * @param usageList 用量值
     */
    public void batchSaveOrUpdateThirdClassUsage(List<ThirdpartyClassUsage> usageList) {
        //班数据需要根据时间算出班次
        Map<Integer, Integer> endHour2ShiftMap = getEndHour2ShiftMap();
        for (ThirdpartyClassUsage usage: usageList) {
            usage.setShift(ShiftEnum.getNameByVal(endHour2ShiftMap.get(usage.getEtime().getHour())));
        }
        //再批量插入
        this.usageDataServiceHelper.batchInsertOrUpdateWithRetry(usageList, thirdpartyClassUsageMapper::batchInsertOrUpdate, "第三方表班数据");
    }

    /**
     * 批量写成功聚合的采集天数据
     * @param usageList 用量值
     */
    public void batchSaveOrUpdateCollectDayUsage(List<CollectDayUsage> usageList) {
        this.usageDataServiceHelper.batchInsertOrUpdateWithRetry(usageList, collectDayUsageMapper::batchInsertOrUpdate, "采集表天数据");
    }

    /**
     * 批量写成功聚合的第三方天数据
     * @param usageList 用量值
     */
    public void batchSaveOrUpdateThirdDayUsage(List<ThirdpartyDayUsage> usageList) {
        this.usageDataServiceHelper.batchInsertOrUpdateWithRetry(usageList, thirdpartyDayUsageMapper::batchInsertOrUpdate, "第三方表天数据");
    }

    /**
     * 批量写成功聚合的中间表小时数据
     * @param usageList 用量值
     */
    public void batchSaveOrUpdateOrsdataHourUsage(List<OrsdataHourUsage> usageList) {
        this.usageDataServiceHelper.batchInsertOrUpdateWithRetry(usageList, orsdataHourUsageMapper::batchInsertOrUpdate, "中间表小时数据");
    }

    /**
     * 批量写成功聚合的中间表天数据
     * @param usageList 用量值
     */
    public void batchSaveOrUpdateOrsdataDayUsage(List<OrsdataDayUsage> usageList) {
        this.usageDataServiceHelper.batchInsertOrUpdateWithRetry(usageList, orsdataDayUsageMapper::batchInsertOrUpdate, "中间表天数据");
    }

    /**
     * 批量写成功聚合的中间表周数据
     * @param usageList 用量值
     */
    public void batchSaveOrUpdateOrsdataWeekUsage(List<OrsdataWeekUsage> usageList) {
        this.usageDataServiceHelper.batchInsertOrUpdateWithRetry(usageList, orsdataWeekUsageMapper::batchInsertOrUpdate, "中间表周数据");
    }

    /**
     * 批量写成功聚合的中间表MTD数据
     * @param usageList 用量值
     */
    public void batchSaveOrUpdateOrsdataMtdUsage(List<OrsdataMtdUsage> usageList) {
        this.usageDataServiceHelper.batchInsertOrUpdateWithRetry(usageList, orsdataMtdUsageMapper::batchInsertOrUpdate, "中间表MTD数据");
    }

    /**
     * 批量写成功聚合的中间表YTD数据
     * @param usageList 用量值
     */
    public void batchSaveOrUpdateOrsdataYtdUsage(List<OrsdataYtdUsage> usageList) {
        this.usageDataServiceHelper.batchInsertOrUpdateWithRetry(usageList, orsdataYtdUsageMapper::batchInsertOrUpdate, "中间表YTD数据");
    }

    /**
     * 根据班次结束时间计算所属班次
     */
    public Map<Integer, Integer> getEndHour2ShiftMap() {
        List<ShiftDayPeriodSet> shiftDayPeriodSetList = shiftDayPeriodSetService.list(new LambdaQueryWrapper<ShiftDayPeriodSet>()
                .eq(ShiftDayPeriodSet::getDataPeriod, DataPeriodEnum.SHIFT.getVal()));
        return shiftDayPeriodSetList.stream()
                .collect(Collectors.toMap(ShiftDayPeriodSet::getHour, ShiftDayPeriodSet::getShift, (oldValue, newValue) -> oldValue));
    }

}
