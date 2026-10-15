package com.keytech.modules.ems.service.impl;

import com.keytech.modules.ems.vo.FluxZone8Data;
import com.keytech.modules.pi.common.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 能源模块 InfluxDB 业务类
 * @author yepeng
 * @since 2025.05.04
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EmsInfluxDbService {
    private final InfluxDBClientHelper influxDBClientHelper;

    /**
     * 批量查询多个点在某个时间段内，取每X分钟的 LAST 值
     * @param fluxCodes 点位Flux编码
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param interval  间隔时间(秒)
     */
    public List<FluxZone8Data> queryMultiPointsWithIntervalLast(List<String> fluxCodes, LocalDateTime startTime, LocalDateTime endTime, int interval) {
        QueryCondition condition = new QueryCondition();
        //需要能正确取到首点和尾点，这里采用LAST方式且startTime前移一个间隔的方式
        condition.setStart(startTime.minusSeconds(interval));
        condition.setEnd(endTime);
        condition.setFrequency(interval);
        condition.setQueryType(QueryType.LAST);
        condition.setSortOrder(QueryCondition.SortOrder.ASC);
        List<QueryTagCondition> tagConditions = fluxCodes.stream().map(s->{
            QueryTagCondition tagCondition = new QueryTagCondition();
            tagCondition.setTagId(s);
            return tagCondition;
        }).toList();
        condition.setQueryTagConditionList(tagConditions);
        // 调用查询方法
        List<MeasurementData> measurementDataList = influxDBClientHelper.query(condition);
        return measurementDataList.stream().map(s-> new FluxZone8Data(s.getTime(), s.getCnlNum(), s.getValue())).toList();
    }

    /**
     * 批量查询多个点在某个时间段内，取每X分钟的 Last 值
     * @param fluxCodes 点位Flux编码
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param interval  间隔时间(秒)
     */
    public Map<String, List<FluxZone8Data>> queryMultiPointsWithIntervalLastMap(List<String> fluxCodes, LocalDateTime startTime, LocalDateTime endTime, int interval) {
        List<FluxZone8Data> dataList = queryMultiPointsWithIntervalLast(fluxCodes, startTime, endTime, interval);
        return dataList.stream().collect(Collectors.groupingBy(FluxZone8Data::getFluxCode));
    }

    /**
     * 批量查询多个点在某个时间段内，取每5分钟的 Last 值
     * @param fluxCodes 点位Flux编码
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    public List<FluxZone8Data> queryMultiPointsWith5MinutesLast(List<String> fluxCodes, LocalDateTime startTime, LocalDateTime endTime) {
        return queryMultiPointsWithIntervalLast(fluxCodes, startTime, endTime, 300);
    }

    /**
     * 批量查询多个点在某个时间段内，取每5分钟的 Last 值
     * @param fluxCodes 点位Flux编码
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    public Map<String, List<FluxZone8Data>> queryMultiPointsWith5MinutesLastMap(List<String> fluxCodes, LocalDateTime startTime, LocalDateTime endTime) {
        List<FluxZone8Data> dataList = queryMultiPointsWith5MinutesLast(fluxCodes, startTime, endTime);
        return dataList.stream().collect(Collectors.groupingBy(FluxZone8Data::getFluxCode));
    }


}