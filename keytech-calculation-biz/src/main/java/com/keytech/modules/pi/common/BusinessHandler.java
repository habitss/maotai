package com.keytech.modules.pi.common;

import com.influxdb.query.FluxRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import cn.hutool.core.lang.Pair;

/**
 * @Author f'l
 * @date 2024/11/17 16:27
 */

@Component
public class BusinessHandler {

    @Autowired
    private InfluxDBClientHelper influxDBClientHelper;


    /**
     * 查找每个点位在一段时间内的增量数据,向上传 QueryType.FIRST ,向下传QueryType.LAST
     * @param queryCondition
     * @return
     */
    public  List<MeasurementData> upOrDownNearestValue(QueryCondition queryCondition)
    {
        List<QueryTagCondition> tagIds = queryCondition.queryTagConditionList;
        List<String> queryBatch = new ArrayList<>();
        for(QueryTagCondition s: tagIds)
        {
            List<QueryTagCondition> ids = new ArrayList<>();
            ids.add(s);
            queryCondition.setQueryTagConditionList(ids);
            String query = influxDBClientHelper.querySql(queryCondition);
            queryBatch.add(query);
        }

        String queryBatchSql = influxDBClientHelper.queryBatchSql(queryBatch);

        List<FluxRecord> listBatch = influxDBClientHelper.queryDataAsync(queryBatchSql);

        List<MeasurementData> measurementDataList = influxDBClientHelper.convertToMeasurementDataList(listBatch);
        return measurementDataList;
    }


    /**
     * 按时间封装行数据
     * @param queryCondition
     * @return
     */
    public Map<Instant,List<MeasurementData>> getMeasurementRowByTime(QueryCondition queryCondition)
    {
        queryCondition.setQueryType(null);
        String query = influxDBClientHelper.querySql(queryCondition);
        List<FluxRecord> list= influxDBClientHelper.queryDataAsync(query);
        List<MeasurementData> measurementDataList=influxDBClientHelper.convertToMeasurementDataList(list);
        // 按 time 字段分组，并且保证每个分组内部按 time 排序
        Map<Instant, List<MeasurementData>> groupedByTime = measurementDataList.stream()
                .collect(Collectors.groupingBy(MeasurementData::getTime));

        Map<Instant, List<MeasurementData>> sortedGroupedByTimeDesc;
        // 使用 TreeMap 按键（即 time）降序排序
        if(queryCondition.getSortOrder() == QueryCondition.SortOrder.DESC)
        {
            sortedGroupedByTimeDesc = new TreeMap<>(Comparator.reverseOrder());
            sortedGroupedByTimeDesc.putAll(groupedByTime);
        }else {
            sortedGroupedByTimeDesc = new TreeMap<>(groupedByTime);

        }
        return sortedGroupedByTimeDesc;
    }


    /**
     * 获取点位在指定时间的连续出现的最后䘣连续出现的开始时间和节数时间，传入时间点（end）、点位对象（tagid+值）
     * @param queryCondition
     * @return
     */
    public Pair<Instant, Instant> getStartAndEndTimeForContinuousTag(QueryCondition queryCondition)
    {
        if(null==queryCondition.getQueryTagConditionList()|| null == queryCondition.getEnd())
        {
            return null;
        }
        queryCondition.setQueryType(QueryType.LAST);
//        queryCondition.getQueryTagConditionList().stream().peek(tag -> tag.setOperator(Operator.EQUALS));
        List<QueryTagCondition> listQueryTagCondition =  queryCondition.getQueryTagConditionList();
        listQueryTagCondition.forEach(queryTagCondition->
                queryTagCondition.setOperator(Operator.EQUALS)
        );
        queryCondition.setQueryTagConditionList(listQueryTagCondition);


        String queryEndTime = influxDBClientHelper.querySql(queryCondition);
        List<FluxRecord> listEnd= influxDBClientHelper.queryDataAsync(queryEndTime);
        List<MeasurementData> measurementDataList1=influxDBClientHelper.convertToMeasurementDataList(listEnd);
        if(null == measurementDataList1 ||  measurementDataList1.isEmpty() || null == measurementDataList1.get(0) || null == measurementDataList1.get(0).getTime()){
            return null;
        }
        MeasurementData end = measurementDataList1.get(0);
        Instant endTime = end.getTime();

        queryCondition.setEnd(endTime.atZone(ZoneId.systemDefault()).toLocalDateTime());

        queryCondition.setQueryType(QueryType.LAST);
//        queryCondition.getQueryTagConditionList().stream().peek(tag -> tag.setOperator(Operator.NOT_EQUALS));
        listQueryTagCondition.forEach(queryTagCondition->
                queryTagCondition.setOperator(Operator.NOT_EQUALS)
        );
        queryCondition.setQueryTagConditionList(listQueryTagCondition);


        String queryStartTime = influxDBClientHelper.querySql(queryCondition);
        List<FluxRecord> listStart= influxDBClientHelper.queryDataAsync(queryStartTime);
        List<MeasurementData> measurementDataList2=influxDBClientHelper.convertToMeasurementDataList(listStart);
        if(null == measurementDataList2 ||  measurementDataList2.isEmpty() || null == measurementDataList2.get(0) || null == measurementDataList2.get(0).getTime()){
            return null;
        }
        MeasurementData start = measurementDataList2.get(0);
        Instant  startTime =  start.getTime();
        Pair<Instant, Instant> pair = new Pair<>(startTime, endTime);
        return pair;
    }

}
