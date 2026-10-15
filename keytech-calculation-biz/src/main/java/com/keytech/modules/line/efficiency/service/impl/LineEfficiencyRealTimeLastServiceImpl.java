package com.keytech.modules.line.efficiency.service.impl;

import com.keytech.modules.line.efficiency.dto.PointDateDto;
import com.keytech.modules.line.efficiency.entity.*;
import com.keytech.modules.line.efficiency.mapper.LineEfficiencyRealTimeLastMapper;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyFilerlossRecordService;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyHandleService;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyRealTimeLastService;
import com.keytech.modules.line.efficiency.service.ILineEfficiencySkuService;
import com.keytech.modules.pi.common.MeasurementData;
import com.keytech.modules.pi.service.IPiComputationModelService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 实时计算逻辑-最新记录表
 * @Author: jeecg-boot
 * @Date:   2025-09-05
 * @Version: V1.0
 */
@Service
public class LineEfficiencyRealTimeLastServiceImpl extends BaseServiceImpl<LineEfficiencyRealTimeLastMapper, LineEfficiencyRealTimeLast> implements ILineEfficiencyRealTimeLastService {

    /**
     * pi计算模型
     */
    @Resource
    private IPiComputationModelService piComputationModelService;

    /**
     * SKU配置
     */
    @Resource
    private ILineEfficiencySkuService lineEfficiencySkuService;

    /**
     * 产量配置
     */
    @Resource
    private ILineEfficiencyHandleService lineEfficiencyHandleService;

    /**
     * 酒损历史记录表
     */
    @Resource
    private ILineEfficiencyFilerlossRecordService lineEfficiencyFilerlossRecordService;


    /**
     * 开始执行
     */
    @Override
    public void executeCip(){
        LocalDateTime nowLocalDateTime = LocalDateTime.now().withSecond(0).withNano(0);
        Date nowDate =  Date.from(nowLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
        //查询所有产线有关CIP的配置
        List<LineEfficiencySku> LineEfficiencySkuList = lineEfficiencySkuService.lambdaQuery().list();
        if(null == LineEfficiencySkuList || LineEfficiencySkuList.size() == 0){
            return;
        }
        //查询所有产线的产量配置信息
        List<LineEfficiencyHandle> productionHandleList = lineEfficiencyHandleService.lambdaQuery().eq(LineEfficiencyHandle::getHandleType,1).list();

        //按 sbId 分组
        Map<String, List<LineEfficiencySku>> groupedByLineCode = LineEfficiencySkuList.stream().collect(Collectors.groupingBy(LineEfficiencySku::getLineCode));
        //查询cip的最新执行记录
        List<LineEfficiencyRealTimeLast> lineEfficiencyRealTimeLastList = lambdaQuery().eq(LineEfficiencyRealTimeLast::getType,0).list();

        //最新的数据
        List<LineEfficiencyRealTimeLast> lineEfficiencyRealTimeLastNew = new ArrayList<>();

        for(String lineCode : groupedByLineCode.keySet()){
            List<LineEfficiencySku> handleList = groupedByLineCode.get(lineCode);
            Optional<LineEfficiencyRealTimeLast> lineEfficiencyRealTimeLastOptional = lineEfficiencyRealTimeLastList.stream().filter(x-> x.getLineCode().equals(lineCode)).findAny();
            LineEfficiencyRealTimeLast realTimeLast = new LineEfficiencyRealTimeLast();
            if(!lineEfficiencyRealTimeLastOptional.isPresent()){
                realTimeLast.setLineCode(lineCode);
                realTimeLast.setLineName(handleList.get(0).getLineName());
                realTimeLast.setType(0);
                //按照当前时间往前推10分钟
                long tenMillisAgo = nowDate.getTime() - 10 * 60 * 1000;
                Date tenMillisAgoDate = new Date(tenMillisAgo);
                realTimeLast.setLastTime(tenMillisAgoDate);

            }else{
                realTimeLast = lineEfficiencyRealTimeLastOptional.get();
            }
            //匹配产量信息
            Optional<LineEfficiencyHandle> productionHandleOptional = productionHandleList.stream().filter(x-> x.getLineCode().equals(lineCode)).findAny();
            if (productionHandleOptional.isPresent()){
                realTimeLast.setProductionHandle(productionHandleOptional.get());
            }
            realTimeLast.setHandleList(handleList);
            lineEfficiencyRealTimeLastNew.add(realTimeLast);
        }


        for (LineEfficiencyRealTimeLast lineEfficiencyRealTimeLast : lineEfficiencyRealTimeLastNew){
            //循环配置信息，进行cip判断
            List<LineEfficiencyFilerlossRecord> listRecord = new ArrayList<>();

            //防止意外情况次产生，定时任务没有执行，导致开始时间和当前时间中间存在跨天的情况，需要分段进行查询
            //获取开始时间
            LocalDateTime startLocalDateTime = lineEfficiencyRealTimeLast.getLastTime().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            //寻找开始时间最近的一个10分钟的整数点
            startLocalDateTime = getNext10MinutePoint(startLocalDateTime);

            //通过开始时间往后推10分钟
            while(startLocalDateTime.isBefore(nowLocalDateTime) || startLocalDateTime.isEqual(nowLocalDateTime)){
                Date startLocalDateTimeDate =  Date.from(startLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
                lineEfficiencyRealTimeLast.setTriggerTime(startLocalDateTimeDate);
                lineEfficiencyRealTimeLast = cipStateCalculate(lineEfficiencyRealTimeLast);

                //开始计算对应的数据
                lineEfficiencyRealTimeLast = calculateResult(lineEfficiencyRealTimeLast);
                //将计算结果给全局集合
                if(null != lineEfficiencyRealTimeLast.getListRecord() && lineEfficiencyRealTimeLast.getListRecord().size() > 0){
                    listRecord.addAll(lineEfficiencyRealTimeLast.getListRecord());
                }

                //清空当前集合
                lineEfficiencyRealTimeLast.setListRecord(new ArrayList<>());

                startLocalDateTime = getNext10MinutePoint(startLocalDateTime);

                if(null != listRecord && listRecord.size() > 100){
                    saveOrUpdate(lineEfficiencyRealTimeLast);
                    lineEfficiencyFilerlossRecordService.saveOrUpdateBatch(listRecord);
                }
            }
            if(null != listRecord && listRecord.size() > 0){
                saveOrUpdate(lineEfficiencyRealTimeLast);
                lineEfficiencyFilerlossRecordService.saveOrUpdateBatch(listRecord);
            }
        }
    }

    /**
     * 计算所有的结果信息
     * @param lineEfficiencyRealTimeLast
     * @return
     */
    public LineEfficiencyRealTimeLast calculateResult(LineEfficiencyRealTimeLast lineEfficiencyRealTimeLast) {
        if(null == lineEfficiencyRealTimeLast.getListRecord() || lineEfficiencyRealTimeLast.getListRecord().size() == 0){
            return lineEfficiencyRealTimeLast;
        }

        //计算所有lineEfficiencyRealTimeLast中recordList结果status为0的数据
        List<LineEfficiencyFilerlossRecord>  list = lineEfficiencyRealTimeLast.getListRecord().stream().filter(x->
                        StringUtils.isNotBlank(x.getStateCode()) && x.getStateCode().equals("0")
                )
                .collect(Collectors.toList());

        List<LineEfficiencySku> handleList = lineEfficiencyRealTimeLast.getHandleList();
        //灌酒机sku信息点位
        String fillSkuPointCode =  handleList.stream().filter(handle -> handle.getType() == 1)
                .findFirst()
                .map(LineEfficiencySku::getPointCode)
                .orElse("");
        //灌酒机SKU的默认值，当灌酒机没有配置SKU信息时，使用默认值
        Double fillSkuDefaultValue = handleList.stream().filter(handle -> handle.getType() == 1)
                .findFirst()
                .map(LineEfficiencySku::getBottleShapeValue)
                .orElse(0.0);

        //灌酒机进酒量点位
        String fillInWindCode =  handleList.stream().filter(handle -> handle.getType() == 4)
                .findFirst()
                .map(LineEfficiencySku::getPointCode)
                .orElse("");
        //灌酒机产量点位
        String fillProductionCode = StringUtils.isBlank(lineEfficiencyRealTimeLast.getProductionHandle().getPointCode()) ? "" : lineEfficiencyRealTimeLast.getProductionHandle().getPointCode();

        //码垛机产量点位
        String palProductionCode = handleList.stream().filter(handle -> handle.getType() == 5)
                .findFirst()
                .map(LineEfficiencySku::getPointCode)
                .orElse("");

        //码垛机SKU
        String palSkuPointCode = handleList.stream().filter(handle -> handle.getType() == 6)
                .findFirst()
                .map(LineEfficiencySku::getPointCode)
                .orElse("");

        //sku可以为空，直接按照默认的来计算
//        if(null == list || list.size() == 0 || StringUtils.isBlank(fillSkuPointCode) || StringUtils.isBlank(fillInWindCode) || StringUtils.isBlank(fillProductionCode)){
//            return lineEfficiencyRealTimeLast;
//        }
        if(null == list || list.size() == 0 || StringUtils.isBlank(fillInWindCode) || StringUtils.isBlank(fillProductionCode)){
            return lineEfficiencyRealTimeLast;
        }

        for(LineEfficiencyFilerlossRecord record : list){
            LocalDateTime startDate = record.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime endDate = record.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            //查询时间段内所有酒机的SKU分段信息
            List<PointDateDto> listPointDataCodes = new ArrayList<>();
            if (StringUtils.isBlank(fillSkuPointCode)){
                if(Objects.isNull(fillSkuDefaultValue)){
                    continue;
                }
                PointDateDto pointDateDto = new PointDateDto();
                pointDateDto.setStartInstant(startDate.atZone(ZoneId.systemDefault()).toInstant());
                pointDateDto.setEndInstant(endDate.atZone(ZoneId.systemDefault()).toInstant());
                pointDateDto.setSkuValue(fillSkuDefaultValue);
                long secondsBetween = Duration.between(startDate.atZone(ZoneId.systemDefault()).toInstant(), endDate.atZone(ZoneId.systemDefault()).toInstant()).getSeconds();
                pointDateDto.setRunningTime(Double.valueOf(secondsBetween));
                listPointDataCodes.add(pointDateDto);
            }else{
                listPointDataCodes = lineEfficiencySkuService.getTimeRangeByPointCode(fillSkuPointCode, startDate, endDate);
            }
            //批次产量
            double batchProduction = 0.0;
            for (PointDateDto pointDateDto : listPointDataCodes){
                double skuProduction = lineEfficiencySkuService.queryDataProduction(fillProductionCode,pointDateDto.getStartInstant().getEpochSecond(),pointDateDto.getEndInstant().getEpochSecond());
                skuProduction = Objects.isNull(skuProduction) ? 0.0 : skuProduction;

                Double skuValue = pointDateDto.getSkuValue();


                LineEfficiencySku lineEfficiencySku = new LineEfficiencySku();

                List<LineEfficiencySku> listSkuHandle = handleList.stream().filter(handle -> handle.getType() == 1).collect(Collectors.toList());
                if(null != listSkuHandle && listSkuHandle.size() > 1){
                    Optional<LineEfficiencySku> skuHandleOptional = handleList.stream().filter(handle -> StringUtils.isNotBlank(handle.getSkuCode()) && Double.parseDouble(handle.getSkuCode()) == skuValue).findFirst();
                    if(skuHandleOptional.isPresent()){
                        lineEfficiencySku = skuHandleOptional.get();
                    }
                }else{
                    if(StringUtils.isBlank(listSkuHandle.get(0).getPointCode()) && StringUtils.isBlank(listSkuHandle.get(0).getSkuCode())){
                        lineEfficiencySku = listSkuHandle.get(0);
                    }else {
                        Optional<LineEfficiencySku> skuHandleOptional = handleList.stream().filter(handle -> StringUtils.isNotBlank(handle.getSkuCode()) && Double.parseDouble(handle.getSkuCode()) == skuValue).findFirst();
                        if(skuHandleOptional.isPresent()){
                            lineEfficiencySku = skuHandleOptional.get();
                        }
                    }
                }

                double bottleShapeValue = Objects.isNull(lineEfficiencySku.getBottleShapeValue()) ? 0.0 : lineEfficiencySku.getBottleShapeValue();

                batchProduction += skuProduction * bottleShapeValue / 1000000.0;
            }
            //批次接酒量
            double fillInWindProduction = lineEfficiencySkuService.queryDataProduction(fillInWindCode,startDate.atZone(ZoneId.systemDefault()).toEpochSecond(),endDate.atZone(ZoneId.systemDefault()).toEpochSecond());

            record.setFillProduction(batchProduction);
            record.setFillReceivingCapacity(fillInWindProduction);

            if (Objects.isNull(fillInWindProduction) || fillInWindProduction == 0){
                continue;
            }
            //酒损批次计算 (批次接酒量 - 批次产量)/批次接酒量,批次接酒量是hl需要换算成kl，除以10
            double lossProduction = (fillInWindProduction - batchProduction) / fillInWindProduction / 10.0;
            // 保留4位小数
            lossProduction = BigDecimal.valueOf(lossProduction)
                    .setScale(4, RoundingMode.HALF_UP)
                    .doubleValue();


            record.setFillFilerloss(lossProduction);

            //当record的type等于1的时候，说明是一整个批次的状态
            if(record.getType() == 1){
                double palBatchProduction = 0.0;
                List<PointDateDto> palListPointDataCodes = lineEfficiencySkuService.getTimeRangeByPointCode(palSkuPointCode, startDate, endDate);
                for (PointDateDto palPointDateDto : palListPointDataCodes){
                    double palSkuProduction = lineEfficiencySkuService.queryDataProduction(palProductionCode,palPointDateDto.getStartInstant().getEpochSecond(),palPointDateDto.getEndInstant().getEpochSecond());
                    palSkuProduction = Objects.isNull(palSkuProduction) ? 0.0 : palSkuProduction;

                    Double palSkuValue = palPointDateDto.getSkuValue();
                    Optional<LineEfficiencySku> palSkuHandleOptional = handleList.stream().filter(handle -> handle.getType() == 6 && StringUtils.isNotBlank(handle.getSkuCode()) && Double.parseDouble(handle.getSkuCode()) == palSkuValue).findFirst();
                    if(palSkuHandleOptional.isPresent()){
                        palBatchProduction += palSkuProduction * palSkuHandleOptional.get().getBottleShapeValue() / 1000000.0;
                    }
                }
                record.setPalProduction(palBatchProduction);
                try {
                    double palFilerloss = 0;
                    //码垛机酒损
                    if(batchProduction != 0){
                        palFilerloss = (batchProduction - palBatchProduction) / batchProduction;
                        palFilerloss = BigDecimal.valueOf(palFilerloss)
                                .setScale(4, RoundingMode.HALF_UP)
                                .doubleValue();
                    }
                    record.setPalFilerloss(palFilerloss);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        lineEfficiencyRealTimeLast.setListRecord(list);
        return lineEfficiencyRealTimeLast;
    }


    /**
     * CIP状态判断，获取所有的CIP信息
     * @param lineEfficiencyRealTimeLast
     * @return
     */
    public LineEfficiencyRealTimeLast cipStateCalculate(LineEfficiencyRealTimeLast lineEfficiencyRealTimeLast) {
        //获取当前时间
        LocalDateTime nowLocalDateTime = lineEfficiencyRealTimeLast.getTriggerTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        //获取开始时间
        LocalDateTime startTime = lineEfficiencyRealTimeLast.getLastTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        //获取设备运行状态点位
        List<String> cipRunningStateList = lineEfficiencyRealTimeLast.getHandleList().stream().filter(handle -> handle.getType() == 3).map(LineEfficiencySku::getPointCode).collect(Collectors.toList());

        //定义取数频率 默认1s
        Integer frequency = 1;
        List<MeasurementData> listPointDataCodes = new ArrayList<>();
        if(startTime.isBefore(nowLocalDateTime)){
            listPointDataCodes = piComputationModelService.getMeasurementDataList(startTime,nowLocalDateTime.plusSeconds(1),cipRunningStateList,frequency);
        }

        if(null == listPointDataCodes || listPointDataCodes.size() == 0){
            //更新触发时间
            lineEfficiencyRealTimeLast.setLastTime(lineEfficiencyRealTimeLast.getTriggerTime());
            return lineEfficiencyRealTimeLast;
        }
        //最新的集合
        List<MeasurementData> listPointDataNew = new ArrayList<>();

        if(cipRunningStateList.size() == 1){
            listPointDataNew = listPointDataCodes;
        }else{
            return lineEfficiencyRealTimeLast;
        }

        //持续时长
        int duration = 60;

        //数据记录表
        List<LineEfficiencyFilerlossRecord> listRecord = new ArrayList<>();

        //持续时间
        Instant newTime = null;

        for (MeasurementData measurementData : listPointDataNew){
            //每次更新最新时间到最新记录表中
            lineEfficiencyRealTimeLast.setLastTime(Date.from(measurementData.getTime()));

            String data = Objects.isNull(measurementData.getValue()) ? "" :  measurementData.getValue().intValue()+"";

            if(StringUtils.isBlank(lineEfficiencyRealTimeLast.getStateCode())){
                lineEfficiencyRealTimeLast.setStateCode(data);
            }
            if(Objects.isNull(lineEfficiencyRealTimeLast.getStartTime())){
                lineEfficiencyRealTimeLast.setStartTime(Date.from(measurementData.getTime()));
            }

            if(!data.equals(lineEfficiencyRealTimeLast.getStateCode())){
                //初始秒
                long initialSeconds = Objects.isNull(lineEfficiencyRealTimeLast.getRunningTime()) ? 0 : lineEfficiencyRealTimeLast.getRunningTime().longValue();

                if(Objects.isNull(newTime)){
                    //初始秒数，防止上一个任务执行完了后，没有运行到指定范围的秒数
                    newTime = measurementData.getTime().minusSeconds(initialSeconds);
                }

                long seconds = Duration.between(newTime, measurementData.getTime()).getSeconds();

                lineEfficiencyRealTimeLast.setRunningTime((double) seconds);

                if(seconds >= duration){
                    //写入到历史记录表
                    LineEfficiencyFilerlossRecord stateRecord = new LineEfficiencyFilerlossRecord();
                    stateRecord.setLineCode(lineEfficiencyRealTimeLast.getLineCode());
                    stateRecord.setLineName(lineEfficiencyRealTimeLast.getLineName());

                    stateRecord.setStateCode(lineEfficiencyRealTimeLast.getStateCode());
                    stateRecord.setType(1);

                    stateRecord.setStartTime(lineEfficiencyRealTimeLast.getStartTime());

                    stateRecord.setEndTime(Date.from(newTime));

                    //计算后面的运行时长
                    long secondsRecord = Duration.between(
                            stateRecord.getStartTime().toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime(),
                            stateRecord.getEndTime().toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime()
                    ).getSeconds();
                    stateRecord.setRunningTime(secondsRecord);

                    listRecord.add(stateRecord);

                    //添加后，更改最新数据表对象的数据
                    lineEfficiencyRealTimeLast.setStateCode(data);
                    lineEfficiencyRealTimeLast.setStartTime(Date.from(newTime));

                    lineEfficiencyRealTimeLast.setRunningTime(0.0);

                    newTime = null;
                }
            }else{
                lineEfficiencyRealTimeLast.setRunningTime(0.0);
                newTime = null;
            }
        }
        if(!lineEfficiencyRealTimeLast.getStartTime().equals(lineEfficiencyRealTimeLast.getLastTime())){
            //写入到历史记录表
            LineEfficiencyFilerlossRecord stateRecordEnd = new LineEfficiencyFilerlossRecord();
            stateRecordEnd.setLineCode(lineEfficiencyRealTimeLast.getLineCode());
            stateRecordEnd.setLineName(lineEfficiencyRealTimeLast.getLineName());

            stateRecordEnd.setStateCode(lineEfficiencyRealTimeLast.getStateCode());
            stateRecordEnd.setType(0);

            stateRecordEnd.setStartTime(lineEfficiencyRealTimeLast.getStartTime());

            stateRecordEnd.setEndTime(lineEfficiencyRealTimeLast.getLastTime());

            //计算后面的运行时长
            long secondsRecord = Duration.between(
                    stateRecordEnd.getStartTime().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime(),
                    stateRecordEnd.getEndTime().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
            ).getSeconds();
            stateRecordEnd.setRunningTime(secondsRecord);

            listRecord.add(stateRecordEnd);
        }

        //获取原本已经存在的listRecord
        List<LineEfficiencyFilerlossRecord> listRecordOld = lineEfficiencyRealTimeLast.getListRecord();
        if(null == listRecordOld || listRecordOld.size() == 0){
            listRecordOld = listRecord;
        }else{
            listRecordOld.addAll(listRecord);
        }
        lineEfficiencyRealTimeLast.setListRecord(listRecordOld);

        return lineEfficiencyRealTimeLast;
    }


    /**
     * 找下一个10分钟的整数点
     * @param startLocalDateTime
     * @return
     */
    public LocalDateTime getNext10MinutePoint(LocalDateTime startLocalDateTime) {
        // 获取分钟数
        int minute = startLocalDateTime.getMinute();
        // 计算下一个10分钟的整数点
        LocalDateTime next10MinutePoint;

        // 总是找到下一个10分钟整数点（而不是当前点）
        int next10Minute = ((minute / 10) + 1) * 10;
        if (next10Minute >= 60) {
            // 如果超过了59分钟，则进入下一小时
            next10MinutePoint = startLocalDateTime.plusHours(1)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
        } else {
            next10MinutePoint = startLocalDateTime
                    .withMinute(next10Minute)
                    .withSecond(0)
                    .withNano(0);
        }

        return next10MinutePoint;
    }
}
