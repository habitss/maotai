package com.keytech.modules.ems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.ems.constants.*;
import com.keytech.modules.ems.entity.AggregationStatus;
import com.keytech.modules.ems.entity.CollectPointStatus;
import com.keytech.modules.ems.entity.ShiftDayPeriodSet;
import com.keytech.modules.ems.mapper.AggregationStatusMapper;
import com.keytech.modules.ems.service.IAggregationStatusService;
import com.keytech.modules.ems.vo.ShiftPeriod;
import com.keytech.modules.ems.vo.TagVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 聚合计算状态表
 * @author yepeng
 * @since 2025.05.13
 * */
@Service
@Slf4j
public class AggregationStatusServiceImpl extends BaseServiceImpl<AggregationStatusMapper, AggregationStatus> implements IAggregationStatusService {

    /**
     * 根据聚合类型查询待处理的聚合状态
     * @param aggregationEnum 聚合类型枚举(采集表班,采集表日,第三方班,第三方日,中间点日周月等)
     * @return 待处理的聚合状态列表
     */
    @Override
    public List<AggregationStatus> queryPendingAggregationByType(AggregationTypeEnum aggregationEnum) {
        // 根据聚合类型确定粒度和标签类型
        int grainType;
        int tagKind = switch (aggregationEnum) {
            case COLLECT_SHIFT -> {
                grainType = GrainTypeEnum.SHIFT.getVal();
                yield TagKindEnum.COLLECT.getVal();
            }
            case COLLECT_DAY -> {
                grainType = GrainTypeEnum.DAY.getVal();
                yield TagKindEnum.COLLECT.getVal();
            }
            case THIRD_SHIFT -> {
                grainType = GrainTypeEnum.SHIFT.getVal();
                yield TagKindEnum.THIRDPARTY.getVal();
            }
            case THIRD_DAY -> {
                grainType = GrainTypeEnum.DAY.getVal();
                yield TagKindEnum.THIRDPARTY.getVal();
            }
            case ORS_DATA_HOUR -> {
                grainType = GrainTypeEnum.HOUR.getVal();
                yield TagKindEnum.ORSDATA.getVal();
            }
            case ORS_DATA_SHIFT -> {
                grainType = GrainTypeEnum.SHIFT.getVal();
                yield TagKindEnum.ORSDATA.getVal();
            }
            case ORS_DATA_DAY -> {
                grainType = GrainTypeEnum.DAY.getVal();
                yield TagKindEnum.ORSDATA.getVal();
            }
            case ORS_DATA_WEEK -> {
                grainType = GrainTypeEnum.WEEK.getVal();
                yield TagKindEnum.ORSDATA.getVal();
            }
            case ORS_DATA_MTD -> {
                grainType = GrainTypeEnum.MONTH.getVal();
                yield TagKindEnum.ORSDATA.getVal();
            }
            case ORS_DATA_YTD -> {
                grainType = GrainTypeEnum.YEAR.getVal();
                yield TagKindEnum.ORSDATA.getVal();
            }
        };
        // 查询未完成的聚合状态记录
        return baseMapper.selectList(new LambdaQueryWrapper<AggregationStatus>()
                .eq(AggregationStatus::getGrainType, grainType)
                .eq(AggregationStatus::getTagKind, tagKind)
                .eq(AggregationStatus::getFinishFlag, StatusFinishFlagEnum.UNDONE.getVal()));
    }

    /**
     * 批量标记聚合状态为已完成
     * @param ids 需要更新的ID集合
     */
    @Override
    public void batchUpdateFinished(Collection<Integer> ids) {
        UpdateWrapper<AggregationStatus> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", ids).set("finish_flag", StatusFinishFlagEnum.FINISHED.getVal()).set("error_flag", CollectErrorFlagEnum.NORMAL.getVal()).set("error_msg", "");
        updateWrapper.setSql("retry_count = retry_count + 1");
        this.update(updateWrapper);
    }

    /**
     * 批量标记聚合状态为已完成
     * @param ids 需要更新的ID集合
     * @param finishFlag 完成标记
     * @param errorFlag 失败标记
     * @param errorMsg 失败原因
     */
    @Override
    public void batchUpdateStatus(Collection<Integer> ids, Integer finishFlag, Integer errorFlag, String errorMsg) {
        UpdateWrapper<AggregationStatus> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", ids).set("finish_flag", finishFlag).set("error_flag", errorFlag).set("error_msg", errorMsg);
        updateWrapper.setSql("retry_count = retry_count + 1");
        this.update(updateWrapper);
    }

    /**
     * 批量标记聚合状态为失败
     * @param ids 需要更新的ID集合
     * @param errorMsg 失败原因
     */
    @Override
    public void batchUpdateError(Collection<Integer> ids, String errorMsg) {
        UpdateWrapper<AggregationStatus> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", ids).set("error_flag", CollectErrorFlagEnum.UNUSUAL.getVal()).set("error_msg", errorMsg);
        updateWrapper.setSql("retry_count = retry_count + 1");
        this.update(updateWrapper);
    }

    /**
     * 生成周期采集任务之后的ORS小时/采集班/采集天聚合任务
     * @param grainType 粒度类型(小时/班/天)
     * @param tagKind 点位类型(collect/ors)
     * @param stime 开始时间
     * @param tagVoList 点位列表
     */
    @Override
    public void setAfterCycleCollectAggregationStatusList(Integer grainType, Integer tagKind, LocalDateTime stime, List<TagVo> tagVoList) {
        if (tagVoList == null || tagVoList.isEmpty()) {
            log.warn("点位列表为空");
            return ;
        }
        // 构造批量插入记录
        List<AggregationStatus> toInsertList = new ArrayList<>();
        // 针对当前整点小时生成点位状态
        tagVoList.forEach(f -> {
            AggregationStatus status = new AggregationStatus();
            status.setTenantId(f.getTenantId());
            status.setFactoryCode(f.getFactoryCode());
            status.setDeptCode(f.getDeptCode());
            status.setLineCode(f.getLineCode());
            status.setTableId(0);
            status.setFieldCode("");
            status.setTagId(f.getTagId());
            status.setGrainType(grainType);
            status.setTagKind(tagKind);
            status.setTimePoint(stime);
            status.setFinishFlag(0);
            status.setErrorFlag(0);
            status.setRetryCount(0);
            status.setErrorMsg("");
            status.setCreateBy(EmsConstantsX.SYSTEM);
            toInsertList.add(status);
        });
        //去重
        List<AggregationStatus> distinctList = toInsertList.stream().collect(Collectors.toMap(
                s -> s.getFactoryCode() + "|" + s.getTagId() + "|" + s.getGrainType() + "|" + s.getTagKind() + "|" + s.getTimePoint(),
                Function.identity(), (existing, replacement) -> existing
        )).values().stream().toList();

        //批量插入或更新
        if (!distinctList.isEmpty()) {
            this.baseMapper.batchInsertOrUpdateAggregationStatus(distinctList);
            log.info("生成周期采集任务之后的ORS小时/采集班/采集天聚合任务: {} 条", distinctList.size());
        }else {
            log.info("生成周期采集任务之后的ORS小时/采集班/采集天聚合任务: 0 条");
        }
    }

    /**
     * 生成重试或补采成功后的ORS点位小时聚合任务
     * 更新存在且已完的的状态为未完成，插入新增的状态
     * @param successPointList 成功重试或补采点位
     * @param orsTagVoList 相关联的ORS点位列表
     */
    @Override
    public void setBackfillOrsHourAggregationStatusList(List<CollectPointStatus> successPointList, List<TagVo> orsTagVoList) {
        //建立基础点位与ORS点位的关系
        Map<String, List<TagVo>> baseTag2OrsTagMap = new HashMap<>();
        successPointList.forEach(s->{
            //将半小时任务向前推半小时再取小时时间点，因为中间点只计算小时数据，半小时点位当stime为00:00时只能表过00:00-00:30的数据
            if (s.getGrainType() == GrainTypeEnum.HALFHOUR.getVal()) {
                s.setCollectTime(s.getCollectTime().minusMinutes(30).truncatedTo(ChronoUnit.HOURS));
            }
            List<TagVo> tagVoList = orsTagVoList.stream().filter(o->o.getBaseTags().contains("C:"+ s.getTagId())).toList();
            if (!tagVoList.isEmpty()) {
                baseTag2OrsTagMap.put(s.getTagId(), tagVoList);
            }
        });
        //开始生成关联ORS点位小时聚合任务
        List<AggregationStatus> toInsertList = new ArrayList<>();
        for (CollectPointStatus cps : successPointList) {
            List<TagVo> orsTagList = baseTag2OrsTagMap.get(cps.getTagId());
            if (orsTagList != null) {
                orsTagList.forEach(f->{
                    AggregationStatus status = new AggregationStatus();
                    status.setTenantId(f.getTenantId());
                    status.setFactoryCode(f.getFactoryCode());
                    status.setDeptCode(f.getDeptCode());
                    status.setLineCode(f.getLineCode());
                    status.setTableId(0);
                    status.setFieldCode("");
                    status.setTagId(f.getTagId());
                    status.setGrainType(GrainTypeEnum.HOUR.getVal());
                    status.setTagKind(TagKindEnum.ORSDATA.getVal());
                    status.setTimePoint(cps.getCollectTime());
                    status.setFinishFlag(0);
                    status.setErrorFlag(0);
                    status.setRetryCount(0);
                    status.setErrorMsg("");
                    status.setCreateBy(EmsConstantsX.SYSTEM);
                    toInsertList.add(status);
                });
            }
        }
        //去重
        List<AggregationStatus> distinctList = toInsertList.stream().collect(Collectors.toMap(
                s -> s.getFactoryCode() + "|" + s.getTagId() + "|" + s.getGrainType() + "|" + s.getTagKind() + "|" + s.getTimePoint(),
                Function.identity(), (existing, replacement) -> existing
            )).values().stream().toList();

        //批量插入或更新
        if (!distinctList.isEmpty()) {
            this.baseMapper.batchInsertOrUpdateAggregationStatus(distinctList);
            log.info("生成重试或补采成功后的ORS点位小时聚合任务: {} 条", distinctList.size());
        }else {
            log.info("生成重试或补采成功后的ORS点位小时聚合任务: 0 条");
        }
    }

    /**
     * 生成采集班/天聚合成功后的ORS的聚合任务
     * 更新存在且已完的的状态为未完成，插入新增的状态
     * @param statusList 班聚合成功的点位列表
     * @param orsTagVoList 相关联的ORS点位列表
     * @param grainTypeEnum 聚合粒度 SHIFT-班 DAY-天 WEEK-周 MONTH-月
     */
    @Override
    public void setAfterCollectExecToOrsAggregationStatusList(List<AggregationStatus> statusList, List<TagVo> orsTagVoList, GrainTypeEnum grainTypeEnum) {
        //建立基础点位与ORS点位的关系
        Map<String, List<TagVo>> baseTag2OrsTagMap = new HashMap<>();
        statusList.forEach(s->{
            List<TagVo> tagVoList = orsTagVoList.stream().filter(o->o.getBaseTags().contains("C:"+ s.getTagId())).toList();
            if (!tagVoList.isEmpty()) {
                baseTag2OrsTagMap.put(s.getTagId(), tagVoList);
            }
        });
        //开始生成关联ORS点位小时聚合任务
        List<AggregationStatus> toInsertList = new ArrayList<>();
        for (AggregationStatus cps : statusList) {
            List<TagVo> orsTagList = baseTag2OrsTagMap.get(cps.getTagId());
            if (orsTagList != null) {
                orsTagList.forEach(f->{
                    AggregationStatus status = new AggregationStatus();
                    status.setTenantId(f.getTenantId());
                    status.setFactoryCode(f.getFactoryCode());
                    status.setDeptCode(f.getDeptCode());
                    status.setLineCode(f.getLineCode());
                    status.setTableId(0);
                    status.setFieldCode("");
                    status.setTagId(f.getTagId());
                    status.setGrainType(grainTypeEnum.getVal());
                    status.setTagKind(TagKindEnum.ORSDATA.getVal());
                    //这里是时间段起点
                    LocalDateTime timePoint = cps.getTimePoint();
                    //根据当前时间得到周一的时间
                    if (GrainTypeEnum.WEEK.getVal() == grainTypeEnum.getVal()) {
                        // 获取本周第一天（周一）再去掉时分秒
                        timePoint = timePoint.with(ChronoField.DAY_OF_WEEK, 1).truncatedTo(ChronoUnit.DAYS);
                    }else if (GrainTypeEnum.MONTH.getVal() == grainTypeEnum.getVal()) {
                        // 获取本月第一天（1号）再去掉时分秒
                        timePoint = timePoint.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
                    }else if (GrainTypeEnum.YEAR.getVal() == grainTypeEnum.getVal()) {
                        // 获取本年第一天（1月1号）再去掉时分秒
                        timePoint = timePoint.withDayOfMonth(1).withMonth(1).truncatedTo(ChronoUnit.DAYS);
                    }
                    status.setTimePoint(timePoint);
                    status.setFinishFlag(0);
                    status.setErrorFlag(0);
                    status.setRetryCount(0);
                    status.setErrorMsg("");
                    status.setCreateBy(EmsConstantsX.SYSTEM);
                    toInsertList.add(status);
                });
            }
        }
        //去重
        List<AggregationStatus> distinctList = toInsertList.stream().collect(Collectors.toMap(
                s -> s.getFactoryCode() + "|" + s.getTagId() + "|" + s.getGrainType() + "|" + s.getTagKind() + "|" + s.getTimePoint(),
                Function.identity(), (existing, replacement) -> existing
        )).values().stream().toList();

        //批量插入或更新
        if (!distinctList.isEmpty()) {
            this.baseMapper.batchInsertOrUpdateAggregationStatus(distinctList);
            log.info("生成采集{}聚合成功后的ORS的班聚合任务: {} 条", grainTypeEnum.getName(), distinctList.size());
        }else {
            log.info("生成采集{}聚合成功后的ORS的班聚合任务: 0 条", grainTypeEnum.getName());
        }
    }

    /**
     * 生成重试或补采成功后的采集点位班聚合任务
     * 更新存在且已完的的状态为未完成，插入新增的状态
     * @param successPointList 成功重试或补采点位
     * @param shiftPeriodSetList 班时间段列表
     */
    @Override
    public void setBackfillCollectShiftAggregationStatusList(List<CollectPointStatus> successPointList, List<ShiftDayPeriodSet> shiftPeriodSetList) {
        List<AggregationStatus> toInsertList = new ArrayList<>();
        //通过班时间段列表判断采集时间在哪个班次内
        List<ShiftPeriod> shiftPeriodList = shiftPeriodSetList.stream().map(s->new ShiftPeriod(s.getShift(), s.getHour(), s.getPeriodTime())).toList();
        for (CollectPointStatus cps : successPointList) {
            ShiftPeriod shiftPeriod = getShiftPeriodByTime(cps.getCollectTime().toLocalTime(), shiftPeriodList);
            if (shiftPeriod != null) {
                LocalDateTime shiftEndTime = shiftPeriod.getShiftEndDateTime(cps.getCollectTime());
                AggregationStatus status = new AggregationStatus();
                status.setTenantId(cps.getTenantId());
                status.setFactoryCode(cps.getFactoryCode());
                status.setDeptCode(cps.getDeptCode());
                status.setLineCode(cps.getLineCode());
                status.setTableId(0);
                status.setFieldCode("");
                status.setTagId(cps.getTagId());
                status.setGrainType(GrainTypeEnum.SHIFT.getVal());
                status.setTagKind(TagKindEnum.COLLECT.getVal());
                //这里用班次周期时间段的起始时间
                status.setTimePoint(shiftEndTime.minusHours(shiftPeriodSetList.get(0).getPeriodTime()));
                status.setFinishFlag(0);
                status.setErrorFlag(0);
                status.setRetryCount(0);
                status.setErrorMsg("");
                status.setCreateBy(EmsConstantsX.SYSTEM);
                toInsertList.add(status);
            }
        }
        //去重
        List<AggregationStatus> distinctList = toInsertList.stream().collect(Collectors.toMap(
                s -> s.getFactoryCode() + "|" + s.getTagId() + "|" + s.getGrainType() + "|" + s.getTagKind() + "|" + s.getTimePoint(),
                Function.identity(), (existing, replacement) -> existing
        )).values().stream().toList();

        //批量插入或更新
        if (!distinctList.isEmpty()) {
            this.baseMapper.batchInsertOrUpdateAggregationStatus(distinctList);
            log.info("查询重试或补采成功后的采集点位班聚合任务: {} 条", distinctList.size());
        }else {
            log.info("查询重试或补采成功后的采集点位班聚合任务: 0 条");
        }
    }

    /**
     * 生成重试或补采成功后的采集点位天聚合任务
     * 更新存在且已完的的状态为未完成，插入新增的状态
     * @param successPointList 成功重试或补采点位
     * @param dayPeriodSet 天时间段设置
     */
    @Override
    public void setBackfillCollectDayAggregationStatusList(List<CollectPointStatus> successPointList, ShiftDayPeriodSet dayPeriodSet) {
        List<AggregationStatus> toInsertList = new ArrayList<>();
        //通过天时间设置天的结束时间
        for (CollectPointStatus cps : successPointList) {
            LocalDateTime dayEndTime;
            if (cps.getCollectTime().getHour() > dayPeriodSet.getHour()) {
                dayEndTime = cps.getCollectTime().truncatedTo(ChronoUnit.HOURS).withHour(dayPeriodSet.getHour()).plusHours(dayPeriodSet.getPeriodTime());
            }else {
                dayEndTime = cps.getCollectTime().truncatedTo(ChronoUnit.HOURS).withHour(dayPeriodSet.getHour());
            }
            AggregationStatus status = new AggregationStatus();
            status.setTenantId(cps.getTenantId());
            status.setFactoryCode(cps.getFactoryCode());
            status.setDeptCode(cps.getDeptCode());
            status.setLineCode(cps.getLineCode());
            status.setTableId(0);
            status.setFieldCode("");
            status.setTagId(cps.getTagId());
            status.setGrainType(GrainTypeEnum.DAY.getVal());
            status.setTagKind(TagKindEnum.COLLECT.getVal());
            //这里用天周期时间段的起始时间
            status.setTimePoint(dayEndTime.minusHours(dayPeriodSet.getPeriodTime()));
            status.setFinishFlag(0);
            status.setErrorFlag(0);
            status.setRetryCount(0);
            status.setErrorMsg("");
            status.setCreateBy(EmsConstantsX.SYSTEM);
            toInsertList.add(status);
        }
        //去重
        List<AggregationStatus> distinctList = toInsertList.stream().collect(Collectors.toMap(
                s -> s.getFactoryCode() + "|" + s.getTagId() + "|" + s.getGrainType() + "|" + s.getTagKind() + "|" + s.getTimePoint(),
                Function.identity(), (existing, replacement) -> existing
        )).values().stream().toList();

        //批量插入或更新
        if (!distinctList.isEmpty()) {
            this.baseMapper.batchInsertOrUpdateAggregationStatus(distinctList);
            log.info("查询重试或补采成功后的采集点位天聚合任务: {} 条", distinctList.size());
        }else {
            log.info("查询重试或补采成功后的采集点位天聚合任务: 0 条");
        }
    }

    /**
     * 根据指定时间获取班次
     * @param time 指定时间
     * @param shiftPeriods 班时间段列表
     */
    private ShiftPeriod getShiftPeriodByTime(LocalTime time, List<ShiftPeriod> shiftPeriods) {
        for (ShiftPeriod period : shiftPeriods) {
            if (period.isInShift(time)) {
                return period;
            }
        }
        return null;
    }

}
