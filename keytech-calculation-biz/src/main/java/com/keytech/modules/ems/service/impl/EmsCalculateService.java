package com.keytech.modules.ems.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.modules.ems.constants.*;
import com.keytech.modules.ems.dto.HistoryCurveDto;
import com.keytech.modules.ems.entity.*;
import com.keytech.modules.ems.mapper.CollectHalfhourUsageMapper;
import com.keytech.modules.ems.mapper.CollectHourUsageMapper;
import com.keytech.modules.ems.service.ICollectErrorLogService;
import com.keytech.modules.ems.service.ICollectPointStatusService;
import com.keytech.modules.ems.service.ICollectTagService;
import com.keytech.modules.ems.util.UsageDataServiceHelper;
import com.keytech.modules.ems.vo.CalcUsageResult;
import com.keytech.modules.ems.vo.CollectResultVo;
import com.keytech.modules.ems.vo.FluxZone8Data;
import com.keytech.modules.ems.vo.HistoryCurveVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 能源模块 计算 业务类
 * @author yepeng
 * @since 2025.05.04
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmsCalculateService  {
    private final EmsInfluxDbService influxDbService;
    private final ICollectPointStatusService collectPointStatusService;
    private final CollectHourUsageMapper collectHourUsageMapper;
    private final CollectHalfhourUsageMapper collectHalfhourUsageMapper;
    private final ICollectErrorLogService collectErrorLogService;
    private final RefValueUsageCalculator refValueUsageCalculator;
    private final UsageDataServiceHelper usageDataServiceHelper;
    private final ICollectTagService collectTagService;

    /**
     * 批量采集一组指定时间段的点位底数并计算出最终用量值
     * @param batchPoints   点位状态列表
     * @param startTime     开始时间
     * @param endTime       结束时间
     */
    public List<CollectResultVo> batchCollectPoints(List<CollectPointStatus> batchPoints, LocalDateTime startTime, LocalDateTime endTime) {
        List<String> fluxCodes = batchPoints.stream().map(CollectPointStatus::getFluxCode).toList();
        List<CollectResultVo> results = new ArrayList<>();
        try {
            long now = System.currentTimeMillis();
            log.info("查询FLUX点位并计算完用量开始：{}", fluxCodes);
            Map<String, List<FluxZone8Data>> influxData = influxDbService.queryMultiPointsWith5MinutesLastMap(fluxCodes, startTime, endTime);
            //解析批量数据
            for (CollectPointStatus pointStatus : batchPoints) {
                if (influxData.containsKey(pointStatus.getFluxCode())) {
                    List<FluxZone8Data> pointMeasurements = influxData.get(pointStatus.getFluxCode());
                    List<Double> values = pointMeasurements.stream().map(FluxZone8Data::getValue).toList();
                    //计算用量需要参考值
                    CalcUsageResult usageRes = refValueUsageCalculator.calculateUsage(values, pointStatus.getLastBaseVal());
                    if (usageRes.getErrorType() == CollectErrorTypeEnum.NULL_DATA_ERROR.getVal()) {
                        results.add(CollectResultVo.fail(pointStatus, "获取到的值不足两个", CollectErrorTypeEnum.NULL_DATA_ERROR.getVal()));
                    }else{
                        results.add(CollectResultVo.success(pointStatus, usageRes.getBaseVal(), usageRes.getUsage(), usageRes.getErrorType()));
                    }
                }else {
                    results.add(CollectResultVo.fail(pointStatus, "未获取到值", CollectErrorTypeEnum.NULL_DATA_ERROR.getVal()));
                }
            }
            log.info("查询FLUX点位{}个并计算完用量完成，耗时：{}ms", fluxCodes.size(), System.currentTimeMillis() - now);
        } catch (Exception e) {
            log.error("批量采集点位并计算用量时：", e);
            for (CollectPointStatus pointStatus : batchPoints) {
                results.add(CollectResultVo.fail(pointStatus, e.getMessage(), CollectErrorTypeEnum.NETWORK_ERROR.getVal()));
            }
        }
        return results;
    }

    /**
     * 批量写成功的数据(先删除再添加)
     * @param successList 成功底数和用量
     * @param grainType   采集粒度
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchInsertUsageDb(List<CollectResultVo> successList, int grainType) {
        if (grainType == GrainTypeEnum.HOUR.getVal()) {
            List<CollectHourUsage> usageList = successList.stream().map(s->{
                CollectHourUsage usage = new CollectHourUsage();
                usage.setTenantId(s.getPointStatus().getTenantId());
                usage.setFactoryCode(s.getPointStatus().getFactoryCode());
                usage.setDeptCode(s.getPointStatus().getDeptCode());
                usage.setLineCode(s.getPointStatus().getLineCode());
                usage.setTagId(s.getPointStatus().getTagId());
                //小时表Utime为对应小时起点即X时0分0秒
                usage.setUtime(s.getPointStatus().getCollectTime());
                usage.setBaseval(s.getValue());
                usage.setFusage(s.getUsage());
                usage.setCreateBy(EmsConstantsX.SYSTEM);
                return usage;
            }).toList();
            this.usageDataServiceHelper.batchInsertOrUpdateWithRetry(usageList, collectHourUsageMapper::batchInsertOrUpdate, "采集表小时数据");
        }else if (grainType == GrainTypeEnum.HALFHOUR.getVal()) {
            List<CollectHalfhourUsage> usageList = successList.stream().map(s->{
                CollectHalfhourUsage usage = new CollectHalfhourUsage();
                usage.setTenantId(s.getPointStatus().getTenantId());
                usage.setFactoryCode(s.getPointStatus().getFactoryCode());
                usage.setDeptCode(s.getPointStatus().getDeptCode());
                usage.setLineCode(s.getPointStatus().getLineCode());
                usage.setTagId(s.getPointStatus().getTagId());
                //半小时表Utime为对应半小时起点即X时0分0秒或X时30分0秒
                usage.setUtime(s.getPointStatus().getCollectTime());
                usage.setBaseval(s.getValue());
                usage.setFusage(s.getUsage());
                usage.setCreateBy(EmsConstantsX.SYSTEM);
                return usage;
            }).toList();
            this.usageDataServiceHelper.batchInsertOrUpdateWithRetry(usageList, collectHalfhourUsageMapper::batchInsertOrUpdate, "采集表半小时数据");
        }

    }

    /**
     * 批量写采集失败日志
     * @param failList 失败日志
     */
    public void batchInsertCollectLog(List<CollectResultVo> failList) {
        List<CollectErrorLog> errorLogList = failList.stream().map(s->{
            CollectErrorLog collectErrorLog = new CollectErrorLog();
            collectErrorLog.setTenantId(s.getPointStatus().getTenantId());
            collectErrorLog.setFactoryCode(s.getPointStatus().getFactoryCode());
            collectErrorLog.setDeptCode(s.getPointStatus().getDeptCode());
            collectErrorLog.setLineCode(s.getPointStatus().getLineCode());
            collectErrorLog.setTagId(s.getPointStatus().getTagId());
            collectErrorLog.setCollectTime(s.getPointStatus().getCollectTime());
            collectErrorLog.setErrorType(s.getErrorType());
            collectErrorLog.setErrorMsg(StringUtils.isEmpty(s.getErrorMsg())?CollectErrorTypeEnum.getNameByVal(s.getErrorType()):s.getErrorMsg());
            collectErrorLog.setCreateBy("system");
            return collectErrorLog;
        }).toList();
        collectErrorLogService.saveBatch(errorLogList);
    }

    /**
     * 更新采集点位状态
     * @param resultList 采集结果
     * @param jobType 1-周期任务 2-重试任务 3-人工补采
     */
    public void updateCollectPointStatus(List<CollectResultVo> resultList, Integer jobType) {
        String factoryCode = resultList.get(0).getPointStatus().getFactoryCode();
        //在重试和人工补采任务的返回结果中会存在多个时间点
        Map<LocalDateTime, List<CollectResultVo>> groupedByTime = resultList.stream().collect(Collectors.groupingBy(CollectResultVo::getCollectTime));
        for (Map.Entry<LocalDateTime, List<CollectResultVo>> entry : groupedByTime.entrySet()) {
            LocalDateTime collectTime = entry.getKey();
            List<CollectResultVo> resultVoList = entry.getValue();
            List<CollectResultVo> successResultList = resultVoList.stream().filter(CollectResultVo::isSuccess).toList();
            //更新成功点位状态及最新的底数和用量（会作为后面采集的参考值）
            for (CollectResultVo resultVo : successResultList) {
                UpdateWrapper<CollectPointStatus> succUpdateWrap = new UpdateWrapper<>();
                succUpdateWrap.set("finish_flag", StatusFinishFlagEnum.FINISHED.getVal()).set("error_flag", CollectErrorFlagEnum.NORMAL.getVal())
                        .set("last_base_val", resultVo.getValue()).set("last_usage", resultVo.getUsage()).set("update_time", LocalDateTime.now()).eq("collect_time", collectTime)
                        .eq(EmsConstantsX.TAG_ID, resultVo.getTagId()).eq(EmsConstantsX.FACTORY_CODE, factoryCode);
                if (jobType == JobTypeEnum.ERROR_RETRY_JOB.getVal()) {
                    succUpdateWrap.setSql("retry_count = retry_count + 1");
                }
                collectPointStatusService.update(succUpdateWrap);
            }
            //更新失败点位状态
            List<String> failTagIds = resultVoList.stream().filter(s->!s.isSuccess()).map(s->s.getPointStatus().getTagId()).toList();
            if (!failTagIds.isEmpty()) {
                UpdateWrapper<CollectPointStatus> failUpdateWrap = new UpdateWrapper<>();
                failUpdateWrap.set("error_flag", CollectErrorFlagEnum.UNUSUAL.getVal()).set("update_time", LocalDateTime.now()).eq("collect_time", collectTime).in("tag_id", failTagIds).eq("factory_code", factoryCode);
                if (jobType == JobTypeEnum.ERROR_RETRY_JOB.getVal()) {
                    failUpdateWrap.setSql("retry_count = retry_count + 1");
                }
                collectPointStatusService.update(failUpdateWrap);
                log.warn("批量更新失败点位状态，点位：{}", failTagIds);
            }
        }
    }

    /**
     * 获取历史曲线数据
     * @param dto 参数
     */
    public List<HistoryCurveVo> getHistoryCurveData(HistoryCurveDto dto) {
        List<HistoryCurveVo> historyCurveVoList = new ArrayList<>();
        LocalDateTime nowtime = LocalDateTime.now();
        if (ChronoUnit.SECONDS.between(dto.getStime(), nowtime) < 0) {
            dto.setStime(nowtime);
        }
        if (ChronoUnit.SECONDS.between(dto.getEtime(), nowtime) < 0) {
            dto.setEtime(nowtime);
        }
        List<String> tagidList = Arrays.asList(StringUtils.split(dto.getTagIds(), ","));
        List<CollectTag> collectTagList = this.collectTagService.list(Wrappers.<CollectTag>lambdaQuery().select(Arrays.asList(CollectTag::getTagName, CollectTag::getTagId, CollectTag::getPointCode, CollectTag::getTagType))
                .ne(CollectTag::getTagType, "").eq(CollectTag::getFactoryCode, dto.getFactoryCode()).in(CollectTag::getTagId, tagidList));
        List<String> fluxCodeList = collectTagList.stream().map(CollectTag::getPointCode).toList();
        Map<String, CollectTag> fluxCodeTagMap = collectTagList.stream().collect(Collectors.toMap(CollectTag::getPointCode, Function.identity()));

        //计算时间间隔
        long seconds = ChronoUnit.SECONDS.between(dto.getStime(), dto.getEtime());
        long frequency = seconds % dto.getFrequency() == 0 ? seconds / dto.getFrequency() : seconds / dto.getFrequency() + 1;

        Map<String, List<FluxZone8Data>> fluxResultMap = this.influxDbService.queryMultiPointsWithIntervalLastMap(fluxCodeList, dto.getStime(), dto.getEtime(), (int) frequency);
        for(Map.Entry<String, List<FluxZone8Data>> entry : fluxResultMap.entrySet()){
            String fluxCode = entry.getKey();
            List<FluxZone8Data> fluxDataList = entry.getValue();
            HistoryCurveVo historyCurveVo = new HistoryCurveVo();
            historyCurveVo.setTagId(fluxCodeTagMap.get(fluxCode).getTagId());
            historyCurveVo.setDataType("Other");
            historyCurveVo.setLineDatas(fluxDataList);
            historyCurveVoList.add(historyCurveVo);
        }
        return historyCurveVoList;
    }

}
