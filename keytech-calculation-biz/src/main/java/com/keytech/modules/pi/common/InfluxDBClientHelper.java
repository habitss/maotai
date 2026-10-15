package com.keytech.modules.pi.common;

import cn.hutool.core.date.DateUtil;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@Component
public class InfluxDBClientHelper {

    private static final Logger logger = LoggerFactory.getLogger(InfluxDBClientHelper.class);

    @Autowired
    private InfluxDBClient influxDBClient;

    @Value("${influx.bucket}")
    public String influxDataBase;

    @Value("${influxdb.table}")
    public String tagTable;

    @Value("${influxdb.field}")
    public String field;

    @Value("${influxdb.tag}")
    public String tag;

    // 定义一个基础查询模板，其中包含占位符
    private static final String BASE_QUERY_TEMPLATE =
            "from(bucket: \"%s\") \n" +
                    "|> range(start: %s, stop: %s) \n" +
                    "|> filter(fn: (r) => r[\"_measurement\"] == \"%s\") \n";

    // 构建查询的方法
    public static String buildCommonQuery(String bucket, LocalDateTime startTime, LocalDateTime endTime, String measurement) {

        // 将时间格式化为 ISO 8601 格式的字符串
        if(null == startTime)
        {
            startTime = LocalDateTime.now().minusYears(1);
        }
        if(null == endTime)
        {
            endTime = LocalDateTime.now().plusYears(1);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 使用formatter将LocalDateTime格式化为字符串
        String startTimeStr = startTime.format(formatter);
        String endTimeStr = endTime.format(formatter);

        SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            long startStr = formatterDate.parse(startTimeStr).getTime()/1000;
            long stopStr = formatterDate.parse(endTimeStr).getTime()/1000;
            // 使用 String.format() 填充占位符
            return String.format(BASE_QUERY_TEMPLATE, bucket, startStr, stopStr, measurement);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<FluxRecord> queryDataAsync(String fluxQuery) {
        List<FluxRecord> publisher=new ArrayList<>();
        try
        {
            List<FluxTable> list = influxDBClient.getQueryApi().query(fluxQuery);

            if(null !=list && list.size()>0)
            {
                for(FluxTable table:list)
                {
                    publisher.addAll(table.getRecords());
                }
            }

        }catch (Exception e)
        {
            logger.error("InfluxDB查询异常, fluxQuery={}", fluxQuery, e);
        }

        return publisher;
    }

    // 添加字段过滤
    public void appendFieldFilter(StringBuffer queryBuilder) {
        queryBuilder.append("|> filter(fn: (r) => r[\"_field\"] == \"").append(field).append("\") \n");
    }

    // 添加字段值过滤
    public void appendValuedFilter(StringBuffer queryBuilder,Float value,Operator operator) {
        String operatorStr = null == operator?"==":operator.getSymbol();
        queryBuilder.append("|> filter(fn: (r) => r[\"_value\"] "+operatorStr+" "+value+") \n");
    }


    // 添加字段过滤
    public void appendTagIdFilter(StringBuffer queryBuilder, List<String> tagIds) {
        // 添加多个 tagid 的过滤条件
        if (!tagIds.isEmpty()) {
            queryBuilder.append("|> filter(fn: (r) => ");
            for (int i = 0; i < tagIds.size(); i++) {
                if (i > 0) {
                    queryBuilder.append(" or ");
                }
                queryBuilder.append("r[\""+tag+"\"] == \"").append(tagIds.get(i)).append("\"");
            }
            queryBuilder.append(") \n");
        }
    }

    /**
     *
     * @param queryBuilder
     * @param second
     * @param queryType
     */
    public void appendGroup(StringBuffer queryBuilder,Integer second,QueryType queryType) {

        if(null != second)
        {
            queryBuilder.append("|> aggregateWindow(every: "+second+"s, fn: "+queryType.getFunctionName()+", createEmpty: false) \n");
        }
    }

    public void appendSort(StringBuffer queryBuilder, QueryCondition.SortOrder sortOrder) {

        if( null !=sortOrder&&QueryCondition.SortOrder.DESC==sortOrder)
        {
            queryBuilder.append("|> sort(columns: [\"_time\"], desc: "+true+")");
        }else {
            queryBuilder.append("|> sort(columns: [\"_time\"], desc: "+false+")");
        }
    }

    // 添加字段过滤
    public void appendYield(StringBuffer queryBuilder) {
        queryBuilder.append("|> yield(name:\"result\"").append("\n");
    }

    // 添加字段过滤并求最大值
    public void appendFunction(StringBuffer queryBuilder, QueryType queryType) {
        if(queryType.getN()!=-1)
        {
            queryBuilder.append("只能添加不带数量的函数");
        }else if(queryType == QueryType.DIFFERENCE){
            queryBuilder.append("|> difference(columns: [\"_value\"])\n");
            queryBuilder.append("|> sum(column: \"_value\")\n");
        }else {
            queryBuilder.append("|> "+queryType.getFunctionName()+"() \n");
        }

    }

    // 添加 TOP N
    public void appendFunctionTop(StringBuffer queryBuilder, QueryType queryType,int topN) {

        if(queryType.getN()!=99)
        {
            queryBuilder.append("只能添加带数量的函数");
        }else {
            queryBuilder.append("|> "+queryType.getFunctionName()+"() ");
        }
        queryBuilder.append("|> group(columns: [\"_field\"]) ")
                .append("|> sort(columns: [\"_value\"], desc: true) ")
                .append("|> "+queryType.getFunctionName()+"(n: ").append(topN).append(") \n");
    }

    public List<MeasurementData> convertToMeasurementDataList(List<FluxRecord> fluxRecords) {
        List<MeasurementData> measurementDataList = fluxRecords.stream()
                .map(record -> new MeasurementData(
                        roundInstantToSeconds(record.getTime()),
                        record.getValueByKey(tag).toString(),
                        Double.parseDouble(record.getValueByKey("_value").toString())
                ))
                .collect(Collectors.toList());

        return  measurementDataList;
    }

    public String querySql(QueryCondition queryCondition){
        String commonquery = InfluxDBClientHelper.buildCommonQuery(influxDataBase,queryCondition.getStart(),queryCondition.getEnd(),tagTable);
        StringBuffer queryBuilder = new StringBuffer(commonquery);
        appendFieldFilter(queryBuilder);
        List<String> tagIds = queryCondition.queryTagConditionList.stream().map(x->x.getTagId()).collect(Collectors.toList());
        appendTagIdFilter(queryBuilder, tagIds);
        QueryType queryType = queryCondition.queryType;

        if(null != queryCondition.queryTagConditionList)
        {
            QueryTagCondition queryTagCondition = queryCondition.queryTagConditionList.get(0);
            if(null != queryTagCondition.getValue())
            {
                appendValuedFilter(queryBuilder,queryTagCondition.getValue(),queryTagCondition.getOperator());
            }

        }

        if( null != queryCondition.getFrequency() && null != queryCondition.getQueryType())
        {
            appendGroup(queryBuilder,queryCondition.getFrequency(),queryCondition.getQueryType());
        }

        if( null !=queryCondition.sortOrder)
        {
            appendSort(queryBuilder,queryCondition.sortOrder);
        }

        if(null !=queryCondition.queryType && null == queryCondition.getFrequency())
        {
            if(99==queryType.getN())
            {
                appendFunctionTop(queryBuilder, queryType,queryType.getN());
            }else {
                appendFunction(queryBuilder, queryType);
            }
        }

        return queryBuilder.toString();
    }

    public  String queryBatchSql(List<String> querySqlList){
        StringBuffer sb = new StringBuffer("");
        for(String querySql:querySqlList)
        {
            sb.append(querySql);
            sb.append("|> yield(name: \""+UUID.randomUUID().toString()+"\")\n");
        }

        return sb.toString();
    }


    public MeasurementData getOne(List<MeasurementData> measurementDataList)
    {
        return  null==measurementDataList?null:measurementDataList.get(0);
    }


    public List<MeasurementData> query(QueryCondition queryCondition)
    {

        queryCondition.setBucket(influxDataBase);

        queryCondition.setMeasurement(tagTable);

        queryCondition.setField(field);
        List<String> tagIds = queryCondition.queryTagConditionList.stream().map(x->x.getTagId().toString()).collect(Collectors.toList());

        if(null ==tagIds||tagIds.size()==0)
        {
            return null;
        }

        if(null == queryCondition.getFrequency()){
            queryCondition.setFrequency(1);
        }

        if(queryCondition.queryType !=null&&queryCondition.queryType.isTopOrBottom())
        {
            if(null==queryCondition.getN()||queryCondition.getN()<1)
            {
                queryCondition.setN(10);
            }
        }
        String query=querySql(queryCondition);

        List<FluxRecord> fluxRecords = queryDataAsync(query);

        List<MeasurementData> measurementDataList = convertToMeasurementDataList(fluxRecords);

        return  measurementDataList;
    }

    public void closeClient() {
        influxDBClient.close();
    }

    // 其他公共方法...
    /**
     * 将Instant四舍五入到秒
     * @param instant 原始Instant
     * @return 四舍五入到秒的Instant
     */
    public Instant roundInstantToSeconds(Instant instant) {
        if (instant.getNano() >= 500_000_000) {
            // 如果纳秒部分>=0.5秒，则向上取整
            return instant.plusSeconds(1).truncatedTo(ChronoUnit.SECONDS);
        } else {
            // 否则向下取整（截断）
            return instant.truncatedTo(ChronoUnit.SECONDS);
        }
    }
}