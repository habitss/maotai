package com.keytech.modules.pi.job;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.keytech.modules.pi.common.*;
import com.keytech.modules.pi.entity.PiCalculateSignal;
import com.keytech.modules.pi.entity.PiComputationModel;
import com.keytech.modules.pi.entity.PiRetrievalSignal;
import com.keytech.modules.pi.entity.PiRetrievalStep;
import com.keytech.modules.pi.service.IPiCalculateSignalService;
import com.keytech.modules.pi.service.IPiComputationModelService;
import com.keytech.modules.pi.service.IPiRetrievalSignalService;
import com.keytech.modules.pi.service.IPiRetrievalStepService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PI计算逻辑(废弃，集成外部接口，用xxl-job集成)
 * @author: 敖磊
 * @date: 2024/11/14
 */
@Slf4j
public class PiCalculateJob implements Job {

    private String parameter;

    /**
     * pi计算信号服务
     */
    @Resource
    private IPiCalculateSignalService piCalculateSignalService;

    /**
     * PI取数信号服务
     */
    @Resource
    private IPiRetrievalSignalService piRetrievalSignalService;

    /**
     * PI计算步骤服务
     */
    @Resource
    private IPiRetrievalStepService piRetrievalStepService;

    /**
     * PI计算模型服务
     */
    @Resource
    private IPiComputationModelService piComputationModelService;


    @Resource
    private InfluxDBClientHelper influxDBClientHelper;


    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

//        String parameterObject = this.parameter;
//        try {
//            //根据parameter参数，确定查询多久以前的数据，确定开始时间，传入参数单位秒
//            //获取当前时间
//            LocalDateTime endTime = LocalDateTime.now();
//            // 创建一个表示秒的时间间隔,默认6分钟
//            long seconds = 360;
//            if(StrUtil.isNotBlank(parameterObject)){
//                seconds = Long.parseLong(parameterObject);
//            }
//            //从当前日期和时间中减去对应的秒数
//            LocalDateTime startTime = endTime.minusSeconds(seconds);
//
//            //获取需要计算的PI
//            List<String> listPI = new ArrayList<>();
//
//            //根据PI清单，查询对应PI所有的计算信号
////            List<PiCalculateSignal> piCalculateSignalList = piCalculateSignalService.queryListByPiCodes(listPI);
//
//            //信号点位id集合，用于后面查找对应的步骤、取值信号等
//            List<String> calculateSignalIds = new ArrayList<>();
//
//            //将计算信号中的唯一约束json转换成listMap
//            //json字符串格式：[{colName:a,tagCode:b,tagValue:c},{colName:a,tagCode:b,tagValue:c}]
//            //唯一约束字符串解析:map格式：colName唯一约束字段名、tagCode字段对应的点位code、tagValue字段对应的点位value
//            for(PiCalculateSignal entity : piCalculateSignalList){
//                String extJson = entity.getUniqueJson();
//                JSONArray jsonArray = new JSONArray(extJson);
//
//                List<Map<String, Object>> listMap = new ArrayList<>();
//
//                List<String> listUniqueTagCode = new ArrayList<>();
//
//                for (int i = 0; i < jsonArray.size(); i++) {
//                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                    Map<String, Object> map = new HashMap<>();
//                    Set<String> keys = jsonObject.keySet();
//                    for(String key : keys){
//                        map.put(key, jsonObject.get(key));
//                        //将唯一约束的tag集合进行整合
//                        if(key.equals("tagCode") && null != jsonObject.get(key) && !"".equals(jsonObject.get(key))){
//                            listUniqueTagCode.add(jsonObject.get(key).toString());
//                        }
//                    }
//                    listMap.add(map);
//                }
//                entity.setListUniqueMap(listMap);
//                //触发时间
//                entity.setCalculateTime(endTime);
//                calculateSignalIds.add(entity.getId());
//            }
//
//            //查询符合条件的取值信号、步骤范围以及计算模型
//            //取值信号集合
//            List<PiRetrievalSignal> piRetrievalSignalList = new ArrayList<>();
//            //取值步骤集合
//            List<PiRetrievalStep> piRetrievalStepList = new ArrayList<>();
//            //计算模型集合
//            List<PiComputationModel> piComputationModelList = new ArrayList<>();
//
//            if(calculateSignalIds.size() > 0){
//                piRetrievalSignalList = piRetrievalSignalService.queryListBycalculateIds(calculateSignalIds);
//                piRetrievalStepList = piRetrievalStepService.queryListBycalculateIds(calculateSignalIds);
//                piComputationModelList = piComputationModelService.queryListBycalculateIds(calculateSignalIds);
//            }
//
//            //一次性获取所有的取数信号的值
//            List<String> calculateSignalTagCodeList = piCalculateSignalList.stream().map(PiCalculateSignal::getPointCode).collect(Collectors.toList());
//
//            //将集合和时间点传入influxDB，返回对应的自动化值
//            QueryCondition queryCondition = new QueryCondition();
//            List<QueryTagCondition> queryTagConditionList = new ArrayList<>();
//            for(String tagCode : calculateSignalTagCodeList){
//                QueryTagCondition queryTagCondition = new QueryTagCondition();
//                queryTagCondition.setTagId(tagCode);
//                queryTagConditionList.add(queryTagCondition);
//            }
//            queryCondition.setStart(startTime);
//            queryCondition.setEnd(endTime);
//
//            //获取所有的触发步骤点位集合
//            List<MeasurementData> listPointData = influxDBClientHelper.query(queryCondition);
//
//            //开始判断触发逻辑
//            for(PiCalculateSignal beforEntity : piCalculateSignalList){
//                /*****开始进入触发逻辑******/
//                //计算方式 进入7、结束8
//                String type = beforEntity.getCalculateType();
//                //初始化一个自动化不可能存在的值，防止第一次异常匹配上
//                double valueTemp = 987654321;
//                for(MeasurementData measurementData : listPointData){
//                    if(measurementData.getCnlNum().equals(beforEntity.getPointCode())){
//                        //进入触发
//                        boolean isComming = false;
//                        if(measurementData.getCnlNum().equals(beforEntity.getPointCode())){
//                            double value = Double.parseDouble(beforEntity.getCalculateValue());
//                            //进入
//                            if(type.equals("7")){
//                                if(value == measurementData.getValue() && valueTemp != value){
//                                    isComming = true;
//                                }
//                            }
//                            //结束
//                            if(type.equals("8")){
//                                if(value != measurementData.getValue() && valueTemp == value){
//                                    isComming = true;
//                                }
//                            }
//                            //将本次的值给全局，为下次循环比较做准备
//                            valueTemp = value;
//
//                            /*****已经触发，进入计算逻辑******/
//                            if(isComming){
//                                //查找对应的取值信号集合、步骤范围集合、计算模型
//                                //取值信号集合
//                                List<PiRetrievalSignal> piRetrievalSignalListByCalculateId = piRetrievalSignalList.stream().filter(entity ->
//                                        entity.getCalculateId().equals(beforEntity.getId())
//                                ).collect(Collectors.toList());
//
//                                //取值步骤集合
//                                List<PiRetrievalStep> piRetrievalStepListByCalculateId = piRetrievalStepList.stream().filter(entity ->
//                                        entity.getCalculateId().equals(beforEntity.getId())
//                                ).collect(Collectors.toList());
//
//                                //计算模型集合
//                                List<PiComputationModel> piComputationModelListByCalculateId = piComputationModelList.stream().filter(entity ->
//                                        entity.getCalculateId().equals(beforEntity.getId())
//                                ).collect(Collectors.toList());
//
//                                if(piComputationModelListByCalculateId.size() == 1){
//                                    //进去PI的计算模型
////                                    piComputationModelService.calculate(piRetrievalSignalListByCalculateId,piRetrievalStepListByCalculateId,piComputationModelListByCalculateId.get(0),beforEntity);
//                                }
//                            }
//                        }
//                    }
//                }
//
//            }
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
