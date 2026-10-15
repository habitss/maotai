package com.keytech.modules.ems.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.ems.constants.AggregationTypeEnum;
import com.keytech.modules.ems.constants.GrainTypeEnum;
import com.keytech.modules.ems.entity.AggregationStatus;
import com.keytech.modules.ems.entity.CollectPointStatus;
import com.keytech.modules.ems.entity.ShiftDayPeriodSet;
import com.keytech.modules.ems.vo.TagVo;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 聚合计算状态表
 * @author yepeng
 * @since 2025.05.12
 * */
public interface IAggregationStatusService extends BaseService<AggregationStatus> {

    /**
     * 批量查询待聚合状态
     * @param aggregationEnum 聚合类型(采集表班,采集表日,第三方班,第三方日,中间点日周月等)
     */
    List<AggregationStatus> queryPendingAggregationByType(AggregationTypeEnum aggregationEnum);

    /**
     * 批量标记聚合完成
     * @param ids
     */
    void batchUpdateFinished(Collection<Integer> ids);

    /**
     * 批量标记聚合状态为已完成
     * @param ids 需要更新的ID集合
     * @param finishFlag 完成标记
     * @param errorFlag 失败标记
     * @param errorMsg 失败原因
     */
    void batchUpdateStatus(Collection<Integer> ids, Integer finishFlag, Integer errorFlag, String errorMsg);

    /**
     * 批量标记聚合失败
     * @param ids
     * @param errorMsg 失败原因
     */
    void batchUpdateError(Collection<Integer> ids, String errorMsg);

    /**
     * 生成周期采集任务之后的ORS小时/采集班/采集天聚合任务
     * @param grainType 粒度类型(小时/班/天)
     * @param tagKind 点位类型(collect/ors)
     * @param stime 开始时间
     * @param tagVoList 点位列表
     */
    void setAfterCycleCollectAggregationStatusList(Integer grainType, Integer tagKind, LocalDateTime stime, List<TagVo> tagVoList);

    /**
     * 查询重试或补采成功后的ORS点位小时聚合任务
     * 更新存在且已完的的状态为未完成，插入新增的状态
     * @param successPointList 成功重试或补采点位
     * @param orsTagVoList 相关联的ORS点位列表
     */
    void setBackfillOrsHourAggregationStatusList(List<CollectPointStatus> successPointList, List<TagVo> orsTagVoList);

    /**
     * 生成采集班聚合成功后的ORS的班聚合任务
     * 更新存在且已完的的状态为未完成，插入新增的状态
     * @param statusList 班聚合成功的点位列表
     * @param orsTagVoList 相关联的ORS点位列表
     * @param grainTypeEnum 聚合粒度 SHIFT-班 DAY-天
     */
    void setAfterCollectExecToOrsAggregationStatusList(List<AggregationStatus> statusList, List<TagVo> orsTagVoList, GrainTypeEnum grainTypeEnum);

    /**
     * 查询重试或补采成功后的采集点位班聚合任务
     * 更新存在且已完的的状态为未完成，插入新增的状态
     * @param successPointList 成功重试或补采点位
     * @param shiftPeriodSetList 班时间段列表
     */
    void setBackfillCollectShiftAggregationStatusList(List<CollectPointStatus> successPointList, List<ShiftDayPeriodSet> shiftPeriodSetList);

    /**
     * 查询重试或补采成功后的采集点位天聚合任务
     * 更新存在且已完的的状态为未完成，插入新增的状态
     * @param successPointList 成功重试或补采点位
     * @param dayPeriodSet 天时间段设置
     */
    void setBackfillCollectDayAggregationStatusList(List<CollectPointStatus> successPointList, ShiftDayPeriodSet dayPeriodSet);

}
