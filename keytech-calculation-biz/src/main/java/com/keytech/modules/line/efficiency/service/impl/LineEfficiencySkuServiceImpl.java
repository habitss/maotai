package com.keytech.modules.line.efficiency.service.impl;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.modules.line.efficiency.dto.PointDateDto;
import com.keytech.modules.line.efficiency.dto.ProductionDto;
import com.keytech.modules.line.efficiency.dto.SkuDataDto;
import com.keytech.modules.line.efficiency.entity.*;
import com.keytech.modules.line.efficiency.mapper.LineEfficiencySkuMapper;
import com.keytech.modules.line.efficiency.service.*;
import com.keytech.modules.pi.common.MeasurementData;
import com.keytech.modules.pi.service.IPiComputationModelService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: sku配置
 * @Author: aolei
 * @Date:   2025-08-13
 * @Version: V1.0
 */
@Service
public class LineEfficiencySkuServiceImpl extends BaseServiceImpl<LineEfficiencySkuMapper, LineEfficiencySku> implements ILineEfficiencySkuService {

    /**
     * 产线配置信息
     */
    @Resource
    private ILineEfficiencyHandleService lineEfficiencyHandleService;

    /**
     * 产线最新执行数据信息
     */
    @Resource
    private ILineEfficiencyStateLastService lineEfficiencyStateLastService;

    /**
     * 计算服务
     */
    @Resource
    private IPiComputationModelService piComputationModelService;

    /**
     * SKU明细记录服务
     */
    @Resource
    private ILineEfficiencySkuRecordService lineEfficiencySkuRecordService;

    /**
     * SKU日记录
     */
    @Resource
    private ILineEfficiencySkuDayService lineEfficiencySkuDayService;

    /**
     * 推送总部线效记录
     */
    @Resource
    private ILineEfficiencyStateRecordService lineEfficiencyStateRecordService;

    /**
     * 推送总部线效记录 - 清零记录
     */
    @Resource
    private ILineEfficiencyStateRecordPieceService lineEfficiencyStateRecordPieceService;

    /**
     * 推送总部线效记录 - 清零记录 - sku记录
     */
    @Resource
    private ILineEfficiencyStateRecordPieceSkuService lineEfficiencyStateRecordPieceSkuService;


    /**
     * 根据小时来获取对应的产量及SKU的信息（小时定时任务）
     */
    @Override
    public void getLineEfficiencySkuListByHour(){
        //获取每条线的产量配置
        List<LineEfficiencyHandle> lineEfficiencyHandleList = lineEfficiencyHandleService.lambdaQuery().eq(LineEfficiencyHandle::getHandleType, 1).list();
        if(null == lineEfficiencyHandleList || lineEfficiencyHandleList.size() == 0){
            return;
        }
        //查询所有的配置信息
        List<LineEfficiencySku> skuHandleList = list();

        //获取每条线的SKU配置
        List<LineEfficiencySku> lineEfficiencySkuList = skuHandleList.stream().filter(x->x.getType() == 1).collect(Collectors.toList());


        if(null == lineEfficiencySkuList || lineEfficiencySkuList.size() == 0){
            return;
        }
        //获取每条线最新的执行时间
        List<LineEfficiencyStateLast> stateLastList = lineEfficiencyStateLastService.lambdaQuery().eq(LineEfficiencyStateLast::getType,0).list();

        //获取当前时间的小时
        int currentHour = LocalDateTime.now().getHour();
        LocalDateTime endTime = LocalDateTime.now().withHour(currentHour).withMinute(0).withSecond(0).withNano(0);
        //往前推1个小时
        LocalDateTime startTime = endTime.minusMinutes(60);

        List<LineEfficiencySkuRecord> list = new ArrayList<>();
        //将两个配置进行归类
        for (LineEfficiencyHandle lineEfficiencyHandle : lineEfficiencyHandleList) {
            List<LineEfficiencySku> lineSkus = lineEfficiencySkuList.stream().filter(x->x.getLineCode().equals(lineEfficiencyHandle.getLineCode())).collect(Collectors.toList());
            lineEfficiencyHandle.setLineEfficiencySkuList(lineSkus);
            //查询sku最新的执行时间
            LineEfficiencyStateLast lineEfficiencyStateLast = stateLastList.stream().filter(x->x.getLineCode().equals(lineEfficiencyHandle.getLineCode())).findFirst().orElse(null);

            //根据实际时间来进行查询，按照1小时的间隔循环
            if(null != lineEfficiencyStateLast && null != lineEfficiencyStateLast.getSkuLastTime()){
                startTime = lineEfficiencyStateLast.getSkuLastTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            LocalDateTime startTemp = startTime;
            LocalDateTime endTemp = startTime.plusMinutes(60);
            while(endTemp.isBefore(endTime) || endTemp.isEqual(endTime)){
                //获取所有sku的时间段
                List<LineEfficiencySkuRecord> listSku = getSkuPairList(lineEfficiencyHandle,startTemp,endTemp,1);
                list.addAll(listSku);
                startTemp = endTemp;
                endTemp = endTemp.plusMinutes(60);
            }
        }
        //计算产量
        list = getSkuOutPut(list);

        //计算酒机状态（每小时执行）
        List<LineEfficiencySku> wineHandle = skuHandleList.stream().filter(x->x.getType() == 2).collect(Collectors.toList());
        List<LineEfficiencySkuRecord> windList = getWineMachineState(wineHandle,stateLastList);

        list.addAll(windList);

        //保存数据
        lineEfficiencySkuRecordService.saveOrUpdateBatch(list);

        //执行保存操作并提交后执行，将数据汇聚到日数据表
        saveSkuDay(stateLastList);

        //批量更新最新数据记录表
        if (stateLastList != null && !stateLastList.isEmpty()) {
            List<String> ids = stateLastList.stream()
                    .map(LineEfficiencyStateLast::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!ids.isEmpty()) {
                Date updateTime = Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant());
                // 生成一条 SQL: UPDATE table SET sku_last_time = ? WHERE id IN (?, ?, ?)
                lineEfficiencyStateLastService.update(
                        Wrappers.<LineEfficiencyStateLast>lambdaUpdate()
                                .set(LineEfficiencyStateLast::getSkuLastTime, updateTime)
                                .in(LineEfficiencyStateLast::getId, ids)
                );
            }
        }

    }


    /**
     * 获取酒机状态（每小时执行）
     */
    public List<LineEfficiencySkuRecord> getWineMachineState(List<LineEfficiencySku> lineEfficiencySkuList,List<LineEfficiencyStateLast> stateLastList){
        List<LineEfficiencySkuRecord> listData = new ArrayList<>();

        //获取当前时间的小时
        int currentHour = LocalDateTime.now().getHour();
        LocalDateTime endTime = LocalDateTime.now().withHour(currentHour).withMinute(0).withSecond(0).withNano(0);
        //往前推1个小时
        LocalDateTime startTime = endTime.minusMinutes(60);

        if(null == lineEfficiencySkuList || lineEfficiencySkuList.size() == 0){
            return listData;
        }

        for (LineEfficiencySku lineEfficiencySku : lineEfficiencySkuList){
            if(StrUtil.isBlank(lineEfficiencySku.getPointCode())){
                continue;
            }
            //查询最新的执行时间
            LineEfficiencyStateLast lineEfficiencyStateLast = stateLastList.stream().filter(
                    x-> x.getLineCode().equals(lineEfficiencySku.getLineCode())
                    ).findFirst()
                    .orElse(null);

            //根据实际时间来进行查询，按照1小时的间隔循环
            if(null != lineEfficiencyStateLast && null != lineEfficiencyStateLast.getSkuLastTime()){
                startTime = lineEfficiencyStateLast.getSkuLastTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            LocalDateTime startTemp = startTime;
            LocalDateTime endTemp = startTime.plusMinutes(60);

            List<PointDateDto> list = new ArrayList<>();

            while(endTemp.isBefore(endTime) || endTemp.isEqual(endTime)){
                List<PointDateDto> listPointDateDto = getTimeRangeByPointCode(lineEfficiencySku.getPointCode(),startTemp,endTemp);
                list.addAll(listPointDateDto);
                startTemp = endTemp;
                endTemp = endTemp.plusMinutes(60);
            }

            for(PointDateDto pointDateDto : list){
                if(null != pointDateDto.getStartInstant()  && null != pointDateDto.getEndInstant()){
                    LineEfficiencySkuRecord lineEfficiencySkuRecord = new LineEfficiencySkuRecord();
                    lineEfficiencySkuRecord.setLineCode(lineEfficiencySku.getLineCode());
                    lineEfficiencySkuRecord.setLineName(lineEfficiencySku.getLineName());
                    lineEfficiencySkuRecord.setPointCode(lineEfficiencySku.getPointCode());

                    lineEfficiencySkuRecord.setSkuCode(pointDateDto.getSkuValue()+"");
                    lineEfficiencySkuRecord.setStartTime(null == pointDateDto.getStartInstant() ? null : Date.from(pointDateDto.getStartInstant()));
                    lineEfficiencySkuRecord.setEndTime(null == pointDateDto.getEndInstant() ? null : Date.from(pointDateDto.getEndInstant()));
                    lineEfficiencySkuRecord.setRunningTime(pointDateDto.getRunningTime());
                    lineEfficiencySkuRecord.setType(2);

                    listData.add(lineEfficiencySkuRecord);
                }
            }
        }
        return listData;
    }

    /**
     * 执行SKU每天班次的数据
     */
    public void saveSkuDay(List<LineEfficiencyStateLast> stateLastList) {

        List<LineEfficiencySkuDay> listSkuDay = new ArrayList<>();

        //获取当前时间的小时
        int currentHour = LocalDateTime.now().getHour();
        //当前整数时间
        LocalDateTime endTime = LocalDateTime.now().withHour(currentHour).withMinute(0).withSecond(0).withNano(0);
        //只有在20点或者8点的时候执行汇聚操作
        //查看初始时间结合当前时间，如果中间有8点和20点的时候就执行
        for(LineEfficiencyStateLast stateLast : stateLastList){
            //上次执行时间的下一个08或者20点的时间
            LocalDateTime nextTargetTime = getNextTargetTime(stateLast.getSkuLastTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

            while(nextTargetTime.isBefore(endTime) || nextTargetTime.isEqual(endTime)){
                List<LineEfficiencySkuDay> lineEfficiencySkuDayList = new ArrayList<>() ;

                lineEfficiencySkuDayList = listSkuDay(stateLast.getLineCode(),stateLast.getLineName(),nextTargetTime);

                listSkuDay.addAll(lineEfficiencySkuDayList);

                nextTargetTime = getNextTargetTime(nextTargetTime);
            }
        }
        lineEfficiencySkuDayService.saveOrUpdateBatch(listSkuDay);
    }

    /**
     * 获取SKU每天班次数据
     * @param dataTime
     * @return
     */
    public List<LineEfficiencySkuDay> listSkuDay(String lineCode,String lineName,LocalDateTime dataTime){

        List<LineEfficiencySkuDay> listDay = new ArrayList<>();

        //判断传入的时间是08还是20
        int hour = dataTime.getHour();
        if(hour != 8 && hour != 20){
            return listDay;
        }
        //开始时间
        Date startDate = null;
        //结束时间
        Date endDate = null;
        //当天日期
        Date dateDay = null;
        //班次
        String classDes = "";

        endDate = Date.from(dataTime.atZone(ZoneId.systemDefault()).toInstant());

        //早晨8点钟，则执行头一天的20-08的晚班班次
        if(hour == 8){
            startDate = Date.from(dataTime.minusDays(1).withHour(20).withMinute(0).withSecond(0).withNano(0).atZone(ZoneId.systemDefault()).toInstant());
            dateDay = Date.from(dataTime.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0).atZone(ZoneId.systemDefault()).toInstant());
            classDes = "晚班";
        }else{
            startDate = Date.from(dataTime.withHour(8).withMinute(0).withSecond(0).withNano(0).atZone(ZoneId.systemDefault()).toInstant());
            dateDay = Date.from(dataTime.withHour(0).withMinute(0).withSecond(0).withNano(0).atZone(ZoneId.systemDefault()).toInstant());
            classDes = "早班";
        }

        //获取SKU点位
        List<LineEfficiencySku> listSku = lambdaQuery()
                .eq(LineEfficiencySku::getLineCode,lineCode)
                .eq(LineEfficiencySku::getType,1)
                .list();

        if(null == listSku || listSku.size() == 0){
            return listDay;
        }
        //sku点位
        String skuPointCode = listSku.get(0).getPointCode();

        //获取产量配置
        List<LineEfficiencyHandle> handleList = lineEfficiencyHandleService.lambdaQuery()
                .eq(LineEfficiencyHandle::getLineCode,lineCode)
                .eq(LineEfficiencyHandle::getHandleType,1)
                .list();
        if(null == handleList || handleList.size() == 0){
            return listDay;
        }
        //产量点位
        String outputPointCode = handleList.get(0).getPointCode();

        //recordList时间段、listStateLast时间段是不会产生交集
        List<Pair<LocalDateTime, LocalDateTime>> listTime = new ArrayList<>();

        //班次内将生产时间分出来
        List<LineEfficiencyStateRecord> recordList = lineEfficiencyStateRecordService.lambdaQuery()
                .eq(LineEfficiencyStateRecord::getLineCode,lineCode)
                .eq(LineEfficiencyStateRecord::getType,0)
                .eq(LineEfficiencyStateRecord::getStateCode,"1")
                .le(LineEfficiencyStateRecord::getStartTime,endDate)
                .ge(LineEfficiencyStateRecord::getEndTime,startDate)
                .list();
        if (null != recordList && recordList.size() > 0){
            for(LineEfficiencyStateRecord record : recordList){
                listTime.add(new Pair<>(
                        record.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                        record.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        ));
            }
        }

        //查询当前批次的生产时间
        List<LineEfficiencyStateLast> listStateLast = lineEfficiencyStateLastService.lambdaQuery()
                .eq(LineEfficiencyStateLast::getLineCode,lineCode)
                .eq(LineEfficiencyStateLast::getType,0)
                .eq(LineEfficiencyStateLast::getStateCode,"1")
                .le(LineEfficiencyStateLast::getStartTime,endDate)
                .list();
        if (null != listStateLast && listStateLast.size() > 0){
            for(LineEfficiencyStateLast record : listStateLast){
                listTime.add(new Pair<>(
                        record.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                        dataTime
                ));
            }
        }

        if(null == listTime || listTime.size() == 0){
            return listDay;
        }

        List<Pair<LocalDateTime, LocalDateTime>> listTimeBase = new ArrayList<>();
        listTimeBase.add(new Pair<>(
                startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                dataTime
        ));
        //取时间的交集
        listTime = piComputationModelService.getIntersectionFromTimeList(listTimeBase,listTime);

        //存储所有sku对应的产量,如果skuCode为空，则map的key给""
        List<SkuDataDto> skuDataDtoList = new ArrayList<>();

        for(Pair<LocalDateTime, LocalDateTime> timePair : listTime){
            //取时间段内的sku以及对应的产量,如果sku点位是空，则直接查询产量
            if(StrUtil.isBlank(skuPointCode)){
                Double outPut = queryDataProduction(outputPointCode,timePair.getKey().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond(),timePair.getValue().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond());

                SkuDataDto skuDataDto = new SkuDataDto();
                skuDataDto.setSkuCode(skuPointCode);
                skuDataDto.setSkuValue("");
                skuDataDto.setSkuValueName(listSku.get(0).getSkuName());
                skuDataDto.setStartTime(timePair.getKey());
                skuDataDto.setEndTime(timePair.getValue());
                skuDataDto.setOutPut(outPut);
                skuDataDtoList.add(skuDataDto);

            }else{
                List<PointDateDto> listDto = getTimeRangeByPointCode(skuPointCode,timePair.getKey(),timePair.getValue());
                for(PointDateDto dto : listDto){
                    String skuValue = null == dto.getSkuValue() ? "" : dto.getSkuValue().toString();

                    Double outPut = queryDataProduction(outputPointCode,dto.getStartInstant().getEpochSecond(),dto.getEndInstant().getEpochSecond());

                    SkuDataDto skuDataDto = new SkuDataDto();
                    skuDataDto.setSkuCode(skuPointCode);
                    skuDataDto.setSkuValue(skuValue);

                    Optional<LineEfficiencySku> lineEfficiencySkuOptional = listSku.stream().filter(x-> StringUtils.isNotBlank(x.getSkuCode()) && StringUtils.isNotBlank(skuValue) && Double.parseDouble(x.getSkuCode()) == Double.parseDouble(skuValue)).findAny();
                    if(lineEfficiencySkuOptional.isPresent()){
                        skuDataDto.setSkuValueName(lineEfficiencySkuOptional.get().getSkuName());
                        skuDataDto.setSkuSpeed(lineEfficiencySkuOptional.get().getSkuSpeed());
                    }

                    skuDataDto.setStartTime(dto.getStartInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                    skuDataDto.setEndTime(dto.getEndInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                    skuDataDto.setOutPut(outPut);

                    skuDataDtoList.add(skuDataDto);
                }
            }
        }

        for(SkuDataDto entry : skuDataDtoList){
            LineEfficiencySkuDay lineEfficiencySkuDay = new LineEfficiencySkuDay();

            lineEfficiencySkuDay.setLineCode(lineCode);
            lineEfficiencySkuDay.setLineName(lineName);
            lineEfficiencySkuDay.setDateDay(dateDay);
            lineEfficiencySkuDay.setClassDes(classDes);
            lineEfficiencySkuDay.setType(1);

            //默认给第一个
            lineEfficiencySkuDay.setSkuCode(entry.getSkuValue());
            lineEfficiencySkuDay.setSkuName(entry.getSkuValueName());
            lineEfficiencySkuDay.setSkuSpeed(entry.getSkuSpeed());
            lineEfficiencySkuDay.setOutputCode(handleList.get(0).getPointCode());

            lineEfficiencySkuDay.setOutputValue(entry.getOutPut());

            lineEfficiencySkuDay.setStartTime(Date.from(entry.getStartTime().atZone(ZoneId.systemDefault()).toInstant()));

            lineEfficiencySkuDay.setEndTime(Date.from(entry.getEndTime().atZone(ZoneId.systemDefault()).toInstant()));

            Duration duration = Duration.between(entry.getStartTime(), entry.getEndTime());
            lineEfficiencySkuDay.setRunningTime(Double.valueOf(duration.getSeconds()));

            listDay.add(lineEfficiencySkuDay);
        }
        return listDay;
    }


    /**
     * 获取时间范围内所有sku的时间段
     * @param lineEfficiencyHandle 产线配置信息
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    public List<LineEfficiencySkuRecord> getSkuPairList(LineEfficiencyHandle lineEfficiencyHandle,LocalDateTime startTime,LocalDateTime endTime,int type){
        //按照sku进行时间段的区分
        List<LineEfficiencySkuRecord> skuRecordList = new ArrayList<>();

        //sku配置信息在groupedByPointCode中正常情况下只能有一条，多条的话，取第一条
        List<LineEfficiencySku> lineEfficiencySkus = lineEfficiencyHandle.getLineEfficiencySkuList() ;
        String keyCode = "";
        if(null != lineEfficiencySkus && lineEfficiencySkus.size() > 0){
            keyCode = lineEfficiencySkus.get(0).getPointCode();
        }
        //查询所有sku在时间段内的数据，如果sku配置的keyCode是空，则所有数据按照默认的计算
        if(null == lineEfficiencySkus || lineEfficiencySkus.size() == 0){
            return skuRecordList;
        }

        List<LineEfficiencySku> skuHandleList = lineEfficiencyHandle.getLineEfficiencySkuList();

        if(StrUtil.isNotBlank(keyCode)){

            List<PointDateDto> listPointDataCodes = getTimeRangeByPointCode(keyCode,startTime,endTime);

            for(PointDateDto pointDateDto : listPointDataCodes){

                LineEfficiencySku skuHandle = skuHandleList.stream().filter(x-> {
                    Double skuCodeValue = safeParseDouble(x.getSkuCode());
                    return skuCodeValue != null && skuCodeValue.equals(pointDateDto.getSkuValue());
                }).findFirst().orElse(new LineEfficiencySku());

                LineEfficiencySkuRecord skuRecord = new LineEfficiencySkuRecord();
                skuRecord.setLineCode(skuHandleList.get(0).getLineCode());
                skuRecord.setLineName(skuHandleList.get(0).getLineName());
                skuRecord.setPointCode(skuHandleList.get(0).getPointCode());

                skuRecord.setSkuName(skuHandle.getSkuName());
                skuRecord.setSkuSpeed(skuHandle.getSkuSpeed());
                skuRecord.setBottleShape(skuHandle.getBottleShape());
                skuRecord.setBottleShapeValue(skuHandle.getBottleShapeValue());

                skuRecord.setSkuCode(pointDateDto.getSkuValue()+"");
                skuRecord.setStartTime(Date.from(pointDateDto.getStartInstant()));
                skuRecord.setEndTime(Date.from(pointDateDto.getEndInstant()));
                skuRecord.setRunningTime(pointDateDto.getRunningTime());

                skuRecord.setOutputCode(lineEfficiencyHandle.getPointCode());
                skuRecord.setType(type);

                skuRecordList.add(skuRecord);
            }
        }else{
            LineEfficiencySkuRecord skuRecord = new LineEfficiencySkuRecord();
            skuRecord.setLineCode(skuHandleList.get(0).getLineCode());
            skuRecord.setLineName(skuHandleList.get(0).getLineName());
            skuRecord.setSkuCode(skuHandleList.get(0).getSkuCode());
            skuRecord.setSkuName(skuHandleList.get(0).getSkuName());
            skuRecord.setSkuSpeed(skuHandleList.get(0).getSkuSpeed());
            skuRecord.setBottleShape(skuHandleList.get(0).getBottleShape());
            skuRecord.setBottleShapeValue(skuHandleList.get(0).getBottleShapeValue());
            skuRecord.setPointCode(skuHandleList.get(0).getPointCode());
            skuRecord.setStartTime(Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()));
            skuRecord.setEndTime(Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant()));
            skuRecord.setOutputCode(lineEfficiencyHandle.getPointCode());

            long seconds =Duration.between(startTime,endTime).getSeconds();
            skuRecord.setRunningTime(Double.valueOf(seconds));
            skuRecord.setType(type);

            skuRecordList.add(skuRecord);
        }
        return skuRecordList;
    }

    /**
     * 送总部批次时间拆解，按照SKU拆分，推送总部后，进行拆分
     */
    @Override
    public void batchSplit(List<LineEfficiencyStateRecord> listStateRecord){

        //获取每条线的产量配置
        List<LineEfficiencyHandle> lineEfficiencyHandleList = lineEfficiencyHandleService.lambdaQuery().eq(LineEfficiencyHandle::getHandleType, 1).list();
        if(null == lineEfficiencyHandleList || lineEfficiencyHandleList.size() == 0){
            return;
        }
        //查询所有的配置信息
        List<LineEfficiencySku> skuHandleList = lambdaQuery().eq(LineEfficiencySku::getType, 1).list();

        List<LineEfficiencyStateRecordPiece> listPieces = new ArrayList<>();


        for(LineEfficiencyStateRecord stateRecord : listStateRecord){
            String lineCode = stateRecord.getLineCode();
            LocalDateTime startLocalDateTime = stateRecord.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime endLocalDateTime = stateRecord.getEndTimeActual().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            //投产才计算
            if(StrUtil.isBlank(stateRecord.getStateCode()) || !"1".equals(stateRecord.getStateCode()) || stateRecord.getType() != 0){
                continue;
            }

            LineEfficiencyHandle lineHandle = lineEfficiencyHandleList.stream().filter(
                    handle -> handle.getLineCode().equals(stateRecord.getLineCode())
            ).findFirst().orElse(null);

            if(null == lineHandle || StrUtil.isBlank(lineHandle.getPointCode())){
                continue;
            }

            //产量code
            String pointCode = lineHandle.getPointCode();

            List<LineEfficiencySku> lineSkus = skuHandleList.stream().filter(x->x.getLineCode().equals(lineCode)).collect(Collectors.toList());
            lineHandle.setLineEfficiencySkuList(lineSkus);

            //判断产量清零的时间段
            //考虑一次性查询过多数据导致内存溢出，分段查询
            LocalDateTime startTemp = startLocalDateTime;
            LocalDateTime endTemp = startLocalDateTime.plusDays(1);


            //清零时间段的开始时间和结束时间
            List<Pair<LocalDateTime,LocalDateTime>> timePeriods = new ArrayList<>();
            LocalDateTime onInstant = startLocalDateTime;
            LocalDateTime offTime = endLocalDateTime;

            while(endTemp.isBefore(endLocalDateTime)){

                List<MeasurementData> listPointData = piComputationModelService.getMeasurementDataList(startTemp,endTemp,Arrays.asList(pointCode),1);
                //筛选出为0的
                List<MeasurementData> pointCodeListData = listPointData.stream().filter(measurementData -> measurementData.getValue() == 0).collect(Collectors.toList());

                for(MeasurementData measurementData : pointCodeListData){
                    offTime = LocalDateTime.ofInstant(measurementData.getTime(), ZoneId.systemDefault());
                    Duration duration = Duration.between(onInstant, offTime);
                    //连续等于0的点排除掉
                    if(duration.getSeconds() >1){
                        timePeriods.add(Pair.of(onInstant, offTime));
                    }
                    onInstant = offTime;
                }
                startTemp = endTemp;
                endTemp = endTemp.plusDays(1);
            }

            if(startTemp.isBefore(endLocalDateTime)){
                List<MeasurementData> listPointData = piComputationModelService.getMeasurementDataList(startTemp,endLocalDateTime,Arrays.asList(pointCode),1);
                //筛选出为0的
                List<MeasurementData> pointCodeListData = listPointData.stream().filter(measurementData -> measurementData.getValue() == 0).collect(Collectors.toList());

                for(MeasurementData measurementData : pointCodeListData){
                    offTime = LocalDateTime.ofInstant(measurementData.getTime(), ZoneId.systemDefault());
                    Duration duration = Duration.between(onInstant, offTime);
                    //连续等于0的点排除掉
                    if(duration.getSeconds() >1){
                        timePeriods.add(Pair.of(onInstant, offTime));
                    }
                    onInstant = offTime;
                }
            }
            //最后一段
            timePeriods.add(Pair.of(onInstant, endLocalDateTime));

            //获取到所有清零的时间段，通过时间段来区分SKU的运行数据
            for(Pair<LocalDateTime,LocalDateTime> pair : timePeriods){
                List<LineEfficiencyStateRecordPieceSku> listPieceSkus = new ArrayList<>();
                LineEfficiencyStateRecordPiece pieceEntity = new LineEfficiencyStateRecordPiece();
                pieceEntity.setRecordId(stateRecord.getId());
                pieceEntity.setStartTime(Date.from(pair.getKey().atZone(ZoneId.systemDefault()).toInstant()));
                pieceEntity.setEndTime(Date.from(pair.getValue().atZone(ZoneId.systemDefault()).toInstant()));


                List<LineEfficiencySkuRecord> skuRecordList = getSkuPairList(lineHandle,pair.getKey(),pair.getValue(),3);
                //计算产量
                skuRecordList = getSkuOutPut(skuRecordList);

                //将LineEfficiencySkuRecord对象转换成LineEfficiencyStateRecordPieceSku对象
                for(LineEfficiencySkuRecord skuRecord : skuRecordList){
                    LineEfficiencyStateRecordPieceSku pieceSku = new LineEfficiencyStateRecordPieceSku();
                    pieceSku.setRecordId(stateRecord.getId());
                    pieceSku.setLineCode(skuRecord.getLineCode());
                    pieceSku.setLineName(skuRecord.getLineName());
                    pieceSku.setSkuCode(skuRecord.getSkuCode());
                    pieceSku.setSkuName(skuRecord.getSkuName());
                    pieceSku.setSkuSpeed(skuRecord.getSkuSpeed());
                    pieceSku.setBottleShape(skuRecord.getBottleShape());
                    pieceSku.setBottleShapeValue(skuRecord.getBottleShapeValue());
                    pieceSku.setPointCode(skuRecord.getPointCode());
                    pieceSku.setStartTime(skuRecord.getStartTime());
                    pieceSku.setEndTime(skuRecord.getEndTime());
                    pieceSku.setRunningTime(skuRecord.getRunningTime());
                    pieceSku.setOutputValue(skuRecord.getOutputValue());
                    pieceSku.setShape(skuRecord.getShape());
                    pieceSku.setOutputCode(skuRecord.getOutputCode());

                    listPieceSkus.add(pieceSku);
                }
                pieceEntity.setListPieceSkus(listPieceSkus);
                listPieces.add(pieceEntity);
            }
        }

        //保存清零记录
        lineEfficiencyStateRecordPieceService.saveOrUpdateBatch(listPieces);


        List<LineEfficiencyStateRecordPieceSku> listPieceSkus = new ArrayList<>();
        //保存后获取到清零表ID，设置到sku记录表中
        for(LineEfficiencyStateRecordPiece piece : listPieces){
            List<LineEfficiencyStateRecordPieceSku> listPieceSkusTemp = piece.getListPieceSkus();
            listPieceSkusTemp.stream().forEach(pieceSkuVo -> {
                pieceSkuVo.setPieceId(piece.getId());
            });
            listPieceSkus.addAll(listPieceSkusTemp);
        }
        lineEfficiencyStateRecordPieceSkuService.saveOrUpdateBatch(listPieceSkus);

    }

    /**
     * 根据sku的记录，计算出对应的产量和ml
     * @param listSkuRecord
     * @return
     */
    public List<LineEfficiencySkuRecord> getSkuOutPut(List<LineEfficiencySkuRecord> listSkuRecord){
        listSkuRecord.forEach(x->{
            long startLong = x.getStartTime().toInstant().getEpochSecond();
            long endLong = x.getEndTime().toInstant().getEpochSecond();
            //计算产量
            Double output = queryDataProduction(x.getOutputCode(),startLong,endLong);
            x.setOutputValue(output);
            //计算ml
            Double bottleShapeValue = null == x.getBottleShapeValue() ? 0.0 : x.getBottleShapeValue();

            Double shape = bottleShapeValue * output;
            String shapeStr = String.format("%.0f", shape);
            x.setShape(shapeStr);
        });
        return listSkuRecord;
    }


    /**
     * 根据点位，获取时间段内，每个数据的持续时间
     * @param pointCode
     * @param startTime
     * @param endTime
     */
    @Override
    public List<PointDateDto> getTimeRangeByPointCode(String pointCode, LocalDateTime startTime, LocalDateTime endTime){
        List<PointDateDto> listPointDateDto = new ArrayList<>();

        if (StrUtil.isBlank(pointCode)){
            return listPointDateDto;
        }

        //查询时间段内所有SKU的数据，为了保证数据一秒不差，特地加1秒
        List<MeasurementData> listPointDataCodes = piComputationModelService.getMeasurementDataList(startTime,endTime.plusSeconds(1), Arrays.asList(pointCode),1);

        Instant startInstant = null;
        Instant endInstant = null;
        Double skuValue = null;

        for (MeasurementData measurementData : listPointDataCodes) {
            //第一次进入，给一个初始值
            if(null == startInstant){
                startInstant = measurementData.getTime();
                endInstant = measurementData.getTime();
                skuValue = measurementData.getValue();
                continue;
            }
            if(skuValue.equals(measurementData.getValue())){
                endInstant = measurementData.getTime();
            }else{
                PointDateDto pointDateDto = new PointDateDto();
                pointDateDto.setSkuValue(skuValue);
                pointDateDto.setStartInstant(startInstant);
                pointDateDto.setEndInstant(endInstant);

                if(null != startInstant && null != endInstant){
                    long secondsBetween = Duration.between(startInstant, endInstant).getSeconds();
                    pointDateDto.setRunningTime(Double.valueOf(secondsBetween));
                }

                listPointDateDto.add(pointDateDto);
                startInstant = endInstant;
                endInstant = measurementData.getTime();
                skuValue = measurementData.getValue();
            }
        }
        if(null != startInstant && null != endInstant){
            PointDateDto pointDateDto = new PointDateDto();
            pointDateDto.setSkuValue(skuValue);
            pointDateDto.setStartInstant(startInstant);
            pointDateDto.setEndInstant(endInstant);
            long secondsBetween = Duration.between(startInstant, endInstant).getSeconds();
            pointDateDto.setRunningTime(Double.valueOf(secondsBetween));
            listPointDateDto.add(pointDateDto);
        }

        return listPointDateDto;
    }



    /**
     * 根据时间段内获取产量（单独定制功能）
     * @param outPutCode 产量点位
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    @Override
    public Double queryDataProduction(String outPutCode, Long startTime, Long endTime) {

        Double production = 0.0;

        LocalDateTime startLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(startTime), ZoneId.systemDefault());
        LocalDateTime endLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(endTime), ZoneId.systemDefault());

        ProductionDto productionDto = new ProductionDto();
        productionDto.setProductionCode(outPutCode);

        productionDto = lineEfficiencyHandleService.getProductionByTime(startLocalDateTime,endLocalDateTime,productionDto,null);
        production = Objects.isNull(productionDto.getProduction()) ? 0.0 : productionDto.getProduction();

//        String fluxQuery = "from(bucket: \""+influxDBClientHelper.influxDataBase+"\")\n" +
//                "  |> range(start: "+startTime+", stop: "+endTime+")\n" +
//                "  |> filter(fn: (r) => r[\"_measurement\"] == \""+influxDBClientHelper.tagTable+"\")\n" +
//                "  |> filter(fn: (r) => r[\"_field\"] == \""+influxDBClientHelper.field+"\")\n" +
//                "  |> filter(fn: (r) => r[\""+influxDBClientHelper.tag+"\"] == \""+outPutCode+"\")\n" +
//                "  |> aggregateWindow(every: 1s, fn: last, createEmpty: false)\n" +
//                "  |> difference(columns: [\"_value\"], keepFirst: true)\n" +
//                "  |> keep (columns: [\""+influxDBClientHelper.tag+"\", \"_time\", \"_value\"])  \n" +
//                "  |> filter(fn: (r) => r[\"_value\"] >= 0)\n" +
//                "  |> sum()";
//        List<FluxRecord>  listFluxRecord = influxDBClientHelper.queryDataAsync(fluxQuery);
//        if(null != listFluxRecord && listFluxRecord.size() > 0){
//            production = null == listFluxRecord.get(0).getValue() ? 0.0 : (Double) listFluxRecord.get(0).getValue();
//        }
        return production;
    }

    /**
     * 推算出下一个08点或者20点的目标时间点
     * @param lastTime
     * @return
     */
    public LocalDateTime getNextTargetTime(LocalDateTime lastTime) {

        // 获取当天的 08:00 和 20:00 时间点
        LocalDateTime today8AM = lastTime.withHour(8).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime today8PM = lastTime.withHour(20).withMinute(0).withSecond(0).withNano(0);

        // 判断下一个目标时间点
        if (lastTime.isBefore(today8AM)) {
            // 如果最后执行时间早于当天 08:00，则下一个目标是 08:00
            return today8AM;
        } else if (lastTime.isBefore(today8PM)) {
            // 如果最后执行时间早于当天 20:00，则下一个目标是 20:00
            return today8PM;
        } else {
            // 如果最后执行时间晚于当天 20:00，则下一个目标是明天 08:00
            return today8AM.plusDays(1);
        }
    }


    /**
     * 安全转换字符串为Double
     * @param str
     * @return
     */
    private Double safeParseDouble(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
