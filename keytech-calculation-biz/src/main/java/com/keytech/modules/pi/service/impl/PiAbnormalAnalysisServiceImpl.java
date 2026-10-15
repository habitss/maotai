package com.keytech.modules.pi.service.impl;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.modules.pi.common.*;
import com.keytech.modules.pi.dto.TableConditionQueryDto;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.PiAbnormalAnalysisMapper;
import com.keytech.modules.pi.service.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @Description: PI异常分析
 * @Author: jeecg-boot
 * @Date:   2025-02-08
 * @Version: V1.0
 */
@Service
public class PiAbnormalAnalysisServiceImpl extends BaseServiceImpl<PiAbnormalAnalysisMapper, PiAbnormalAnalysis> implements IPiAbnormalAnalysisService {

    /**
     * 数据红黄区标准服务
     */
    @Resource
    private IPiStandardDataService piStandardDataService;

    /**
     * PI红黄区pi明细表服务
     */
    @Resource
    private IPiStandardMaintenanceService piStandardMaintenanceService;

    /**
     * 自定义数据表服务
     */
    @Resource
    private IPiCustomizeTableService piCustomizeTableService;

    /**
     * pi计算信号服务
     */
    @Resource
    private IPiCalculateSignalService piCalculateSignalService;

    /**
     * PI计算步骤服务
     */
    @Resource
    private IPiComputationModelService piComputationModelService;


    /**
     * influxDB服务
     */
    @Resource
    private InfluxDBClientHelper influxDBClientHelper;

    @Resource
    private BusinessHandler businessHandler;

    /**
     * PI基础点位服务
     */
    @Resource
    private IPiBasePointService piBasePointService;

    /**
     * 查询单个PI的异常分析记录
     * @param piAbnormalAnalysis
     * @return
     */
    @Override
    public PiAbnormalAnalysis queryAbnormalAnalysis(PiAbnormalAnalysis piAbnormalAnalysis) {
        LambdaQueryWrapper<PiAbnormalAnalysis> queryWrapper = Wrappers.lambdaQuery(PiAbnormalAnalysis.class);
        //数据表code
        queryWrapper.in(PiAbnormalAnalysis::getTableCode,piAbnormalAnalysis.getTableCode());
        //字段code
        queryWrapper.in(PiAbnormalAnalysis::getFieldCode,piAbnormalAnalysis.getFieldCode());
        //数据id
        queryWrapper.in(PiAbnormalAnalysis::getDataId,piAbnormalAnalysis.getDataId());

        List<PiAbnormalAnalysis> piAbnormalAnalysisList = list(queryWrapper);
        if(null != piAbnormalAnalysisList && piAbnormalAnalysisList.size() == 1){
            //存在异常分析的pi实际值发生变动的可能，将最新的PI实际值反填到前端
            String dataValue = piAbnormalAnalysis.getPiData();
            piAbnormalAnalysis = piAbnormalAnalysisList.get(0);
            piAbnormalAnalysis.setPiData(dataValue);
        }
        //异常描述为空，则将红黄区的数据填入
        if(StrUtil.isBlank(piAbnormalAnalysis.getAbnormalDescribe())){
            Map retStandardMap = getPiStandardData(piAbnormalAnalysis.getTableCode(),piAbnormalAnalysis.getDataId(),piAbnormalAnalysis.getFieldCode(),piAbnormalAnalysis.getPiData());
            String retStandard = null != retStandardMap.get("maintenanceDescribe") ? retStandardMap.get("maintenanceDescribe").toString() : "";
            if(StrUtil.isNotBlank(retStandard)){
                piAbnormalAnalysis.setAbnormalDescribe(retStandard);
            }
        }
        //如果触发时间为空，需要查询触发时间
        if(null == piAbnormalAnalysis.getAbnormalTime()){
            Map triggerMap = getTriggerTimeForPi(piAbnormalAnalysis.getDataId(),piAbnormalAnalysis.getTableCode(),piAbnormalAnalysis.getFieldCode());
            //触发时间
            String triggerTime = null != triggerMap.get("time") ? triggerMap.get("time").toString() : "";
            if(StrUtil.isNotBlank(triggerTime)){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date date = sdf.parse(triggerTime);
                    piAbnormalAnalysis.setAbnormalTime(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return piAbnormalAnalysis;
    }

    /**
     * 获取过程记录中每个点位的值
     * @param tableCode 数据表code
     * @param dataId 数据id
     * @param fieldCode 字段code
     * @param dataPage 第几页
     * @param dataOffSet 每一页的数据条数
     */
    @Override
    public Map<String,List<MeasurementData>> getBasePointDataList(String tableCode,String dataId,String fieldCode,Integer dataPage,Integer dataOffSet){

        Map<String,List<MeasurementData>> listPointData = new HashMap<>();

        //所有点位的点位集合
        List<MeasurementData> listPointDataAll = new ArrayList<>();

        //获取相关触发时间
        Map triggerMap = getTriggerTimeForPi(dataId,tableCode,fieldCode);

        String calculateSignalId = null != triggerMap.get("calculateSignalId") ? triggerMap.get("calculateSignalId").toString() : "";
        String triggerTime = null != triggerMap.get("time") ? triggerMap.get("time").toString() : "";

        List<String> listPI = Arrays.asList(fieldCode);

        List<PiCalculateSignal> piCalculateSignalList = piCalculateSignalService.queryListByPiCodes(tableCode,listPI);

        Optional<PiCalculateSignal> piCalculateSignalOptional = piCalculateSignalList.stream().filter(e->e.getId().equals(calculateSignalId)).findAny();

        PiCalculateSignal piCalculateSignal = new PiCalculateSignal();
        if(piCalculateSignalOptional.isPresent()){
            piCalculateSignal = piCalculateSignalOptional.get();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            try {
                LocalDateTime dateTime = LocalDateTime.parse(triggerTime, formatter);

                //判断提前或者延后
                if(StrUtil.isNotBlank(piCalculateSignal.getPiComputationModelList().get(0).getAdvanceTime()) && !"0".equals(piCalculateSignal.getPiComputationModelList().get(0).getAdvanceTime())){
                    if("0".equals(piCalculateSignal.getPiComputationModelList().get(0).getAdvanceDelay())){
                        dateTime = dateTime.minusSeconds(Long.parseLong(piCalculateSignal.getPiComputationModelList().get(0).getAdvanceTime()));
                    }else{
                        dateTime = dateTime.plusSeconds(Long.parseLong(piCalculateSignal.getPiComputationModelList().get(0).getAdvanceTime()));
                    }
                }

                piCalculateSignal.setCalculateTime(dateTime);
            } catch (DateTimeParseException e) {
                e.getMessage();
            }
            //获取符合条件的时间段集合
            List<Pair<LocalDateTime,LocalDateTime>> listTimePair = piComputationModelService.getTimeListByRetrievalStep(piCalculateSignal);

            //只查询范围型和监控型取数,将涉及的点位归集到一起
            List<String> tagList = new ArrayList<>();
            //范围型
            if(null != piCalculateSignal.getPiComputationModelList() && piCalculateSignal.getPiComputationModelList().size() == 1){
                if(StrUtil.isNotBlank(piCalculateSignal.getPiComputationModelList().get(0).getFormulaCode()) && piCalculateSignal.getPiComputationModelList().get(0).getFormulaCode().contains("⊙")){
                    tagList = piComputationModelService.getListPiCodeByFormulaCode(piCalculateSignal.getPiComputationModelList().get(0).getFormulaCode());
                }
            }
            //监控型
            if(null != piCalculateSignal.getPiComputationMonitorModelList() && piCalculateSignal.getPiComputationMonitorModelList().size() >= 1){
                tagList.add(piCalculateSignal.getPiComputationMonitorModelList().get(0).getActualValue());
                if(StrUtil.isNotBlank(piCalculateSignal.getPiComputationMonitorModelList().get(0).getPresumePoint())){
                    tagList.add(piCalculateSignal.getPiComputationMonitorModelList().get(0).getPresumePoint());
                }
            }

            //过滤掉field其他的的数据
            tagList = tagList.stream().filter(e-> !e.contains("field")).collect(Collectors.toList());

            if(tagList.size() > 0){
                //查询点位中文描述
                List<PiBasePoint> listPoint = piBasePointService.lambdaQuery().in(PiBasePoint::getPointCode,tagList).list();

                //频率
                Integer frequency = 1;
                if(StrUtil.isNotBlank(piCalculateSignal.getPiRetrievalStepList().get(0).getFrequency())){
                    frequency = Integer.parseInt(piCalculateSignal.getPiRetrievalStepList().get(0).getFrequency());
                }

                for(Pair<LocalDateTime,LocalDateTime> dateTimePair : listTimePair){
                    if(null == dateTimePair.getKey()  || null == dateTimePair.getValue()){
                        continue;
                    }
                    //将集合和时间点传入influxDB，返回对应的自动化值
                    QueryCondition queryCondition = new QueryCondition();
                    List<QueryTagCondition> queryTagConditionList = new ArrayList<>();

                    for(String tagCode : tagList){
                        //将实际点位加入计算列表
                        QueryTagCondition queryTagCondition = new QueryTagCondition();
                        queryTagCondition.setTagId(tagCode);
                        queryTagConditionList.add(queryTagCondition);
                    }

                    queryCondition.setStart(dateTimePair.getKey());
                    queryCondition.setQueryTagConditionList(queryTagConditionList);

                    //获取所有的触发步骤点位集合
                    List<MeasurementData> listPointDataPI = new ArrayList<>();
                    if(dateTimePair.getKey().isEqual(dateTimePair.getValue())){
                        queryCondition.setQueryType(QueryType.FIRST);
                        listPointDataPI = businessHandler.upOrDownNearestValue(queryCondition);
                    }else{
                        queryCondition.setFrequency(frequency);
                        queryCondition.setEnd(dateTimePair.getValue());
                        queryCondition.setQueryType(QueryType.LAST);
                        listPointDataPI = influxDBClientHelper.query(queryCondition);
                    }
                    if(null != listPointDataPI && listPointDataPI.size() > 0 && null != listPointDataPI.get(0)){
                        //将listPointDataPI集合按照MeasurementData中的cnlNum进行分组
                        Map<String, List<MeasurementData>> groupedByCnlNum = listPointDataPI.stream()
                                .collect(Collectors.groupingBy(MeasurementData::getCnlNum));
                        //直接判断当前集合的最后一个点位跟倒数第二个点位之间的差是否跟频率匹配，如果不匹配，直接删除最后一个点
                        Integer finalFrequency = frequency;
                        groupedByCnlNum.keySet().forEach(cnlNum -> {
                            List<MeasurementData> listNum = groupedByCnlNum.get(cnlNum);
                            if(listNum.size() >=2){
                                // 计算两个 Instant 对象之间的时间差
                                Duration duration = Duration.between(listNum.get(listNum.size()-2).getTime(), listNum.get(listNum.size()-1).getTime());
                                long seconds = duration.getSeconds();
                                if(seconds != finalFrequency){
                                    listNum.remove(listNum.size()-1);
                                }
                            }
                            listPointDataAll.addAll(listNum);
                        });
                    }
                }
                for(PiBasePoint point : listPoint){
                    List<MeasurementData> pointDatas = listPointDataAll.stream().filter(e->e.getCnlNum().equals(point.getPointCode())).collect(Collectors.toList());
                    //将pointDatas按照分页进行拆分
                    int startSize = (dataPage-1) * dataOffSet;
                    int endSize = dataPage * dataOffSet;
                    if(null != pointDatas && pointDatas.size() > 0 ){
                        if(pointDatas.size() > startSize && pointDatas.size() <= endSize){
                            endSize = pointDatas.size();
                        }
                        pointDatas = pointDatas.subList(startSize,endSize);
                        for(MeasurementData measurementData : pointDatas){
                            BigDecimal bd = new BigDecimal(measurementData.getValue());
                            //直接截取，不做4舍5入处理
                            BigDecimal roundedUp = bd.setScale(4, RoundingMode.DOWN);
                            measurementData.setValue(roundedUp.doubleValue());
                        }
                        listPointData.put(point.getPointName(),pointDatas);
                    }
                }
            }
        }
        return listPointData;
    }

    /**
     * 获取对应PI的标准以及描述
     * @param tableCode 基础数据表
     * @param dataId 数据id
     * @param fieldCode 字段代码
     * @param dataValue 字段值
     * @return standardMaintenance标准，maintenanceDescribe标准描述
     */
    @Override
    public Map<String,Object> getPiStandardData(String tableCode,String dataId,String fieldCode,String dataValue){
        //定义返回MAP
        Map<String,Object> retMap = new HashMap<>();

        LambdaQueryWrapper<PiStandardData> queryWrapperPiStandardData = Wrappers.lambdaQuery(PiStandardData.class);
        queryWrapperPiStandardData.eq(PiStandardData::getTableName,tableCode);
        queryWrapperPiStandardData.eq(PiStandardData::getDataId,dataId);
        List<PiStandardData> standardDataList = piStandardDataService.list(queryWrapperPiStandardData);
        if(null != standardDataList && standardDataList.size() == 1){
            PiStandardData standardDataEntity = standardDataList.get(0);
            String standardJson = standardDataEntity.getStandardJson();
            if(StrUtil.isNotBlank(standardJson)){
                List<Map> listMap = JSONUtil.toList(standardJson, Map.class);
                //查找对应字段的标准信息
                List<Map> mapList = listMap.stream().filter(e->(null != e.get("field") && e.get("field").equals(fieldCode))).collect(Collectors.toList());
//                Optional<Map> havaMapOptional = listMap.stream().filter(e->e.get("field").equals(fieldCode)).findAny();
                if(mapList.size() > 0){
                    Map fieldMap = mapList.get(0);
                    //获取标准明细ID
                    String maintenanceId =  null != fieldMap.get("maintenance_id") ? fieldMap.get("maintenance_id").toString() : "";
                    //颜色
                    String color =  null != fieldMap.get("color") ? fieldMap.get("color").toString() : "";
                    List<PiStandardMaintenance> maintenanceList = piStandardMaintenanceService.lambdaQuery().eq(PiStandardMaintenance::getId,maintenanceId).list();
                    if(null != maintenanceList && maintenanceList.size() == 1){
                        PiStandardMaintenance maintenanceEntity = maintenanceList.get(0);
                        //将标准对象注入到MAP中
                        retMap.put("standardMaintenance",maintenanceEntity);

                        //定义是标准值还是偏差值
                        String indicatorTypeName = "";
                        switch (maintenanceEntity.getIndicatorType()){
                            case "1" :
                                indicatorTypeName = "标准值";
                                break;
                            case "2" :
                                indicatorTypeName = "偏差值";
                                break;
                        }
                        //定义颜色
                        String colorName = "";
                        switch (color){
                            case "red" :
                                colorName = "红区";
                                break;
                            case "yellow" :
                                colorName = "黄区";
                                break;
                            case "green" :
                                colorName = "绿区";
                                break;
                            case "grey" :
                                colorName = "剔除";
                                break;
                        }
                        String retStandard = "【报表名称】" + maintenanceEntity.getTableText() +
                                ",【PI名称】" + maintenanceEntity.getFieldText() +
                                ",【实际值】" + dataValue +
                                ",【标准名称】" + maintenanceEntity.getManageName() +
                                ",【指标类型】" + indicatorTypeName +
                                ",【目标值】" + maintenanceEntity.getManageName() +
                                ",【结果是】" + colorName +
                                ",【标准是】" + maintenanceEntity.getRedDown() + " - " + maintenanceEntity.getYellowDown() + " - " + maintenanceEntity.getStandardValue() + " - " + maintenanceEntity.getYellowUp() + " - "  + maintenanceEntity.getRedUp() +
                                "【标准是：红下 - 黄下 - 绿 - 黄上 - 红上】";

                        //将标准描述注入到MAP中
                        retMap.put("maintenanceDescribe",retStandard);
                    }
                }
            }
        }
        return retMap;
    }

    /**
     * 获取PI的数据计算触发时间
     * @param dataId
     * @param tableCode
     * @param fieldCode
     * @return field\time\calculateSignalId
     */
    @Override
    public Map getTriggerTimeForPi(String dataId,String tableCode,String fieldCode){
        Map triggerMap = new HashMap<>();
        // 设置查询条件
        String query = "id = " + dataId;
        //设置查询字段，默认查询全部
        List<String> columns = Arrays.asList("trigger_json");

        TableConditionQueryDto tableConditionQueryDto = new TableConditionQueryDto();
        tableConditionQueryDto.setFieldList(columns);
        tableConditionQueryDto.setTable(tableCode);
        tableConditionQueryDto.setCondition(query);
        //当前表中所有的符合要求的数据
        List<Map<String,Object>> tableData = piCustomizeTableService.getTableByCondition(tableConditionQueryDto);
        if(null != tableData && tableData.size() != 0 && null != tableData.get(0)){
            String triggerJson = null != tableData.get(0).get("trigger_json") ? tableData.get(0).get("trigger_json").toString() : "";
            if(StrUtil.isNotBlank(triggerJson)){
                List<Map> listMap = JSONUtil.toList(triggerJson, Map.class);
                //临时变量，用于stream()过滤
                Optional<Map> piMapOptional = listMap.stream().filter(e->e.get("field").equals(fieldCode)).findFirst();
                if(piMapOptional.isPresent()){
                    triggerMap = piMapOptional.get();
                }
            }
        }
        return triggerMap;
    }
}
