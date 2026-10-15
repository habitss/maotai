package com.keytech.modules.line.efficiency.service.impl;

import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.keytech.modules.line.efficiency.dto.*;
import com.keytech.modules.line.efficiency.entity.*;
import com.keytech.modules.line.efficiency.mapper.LineEfficiencyHandleMapper;
import com.keytech.modules.line.efficiency.service.*;
import com.keytech.modules.line.efficiency.utils.VariableUtil;
import com.keytech.modules.pi.common.InfluxDBClientHelper;
import com.keytech.modules.pi.common.MeasurementData;
import com.keytech.modules.pi.entity.LineEfficiencyStateRecordDay;
import com.keytech.modules.pi.service.IPiComputationModelService;
import jakarta.annotation.Resource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.keytech.base.service.impl.BaseServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @Description: 线效率-产量/线效状态点位配置
 * @Author: jeecg-boot
 * @Date:   2025-05-26
 * @Version: V1.0
 */
@Service
public class LineEfficiencyHandleServiceImpl extends BaseServiceImpl<LineEfficiencyHandleMapper, LineEfficiencyHandle> implements ILineEfficiencyHandleService {

    @Resource
    private LineEfficiencyHandleMapper handleMapper;
    /**
     * pi计算模型
     */
    @Resource
    private IPiComputationModelService piComputationModelService;

    /**
     * 线效率最新数据表服务
     */
    @Resource
    private ILineEfficiencyStateLastService lineEfficiencyStateLastService;

    /**
     * 线效状态历史记录服务
     */
    @Resource
    private ILineEfficiencyStateRecordService lineEfficiencyStateRecordService;

    /**
     * 推送点位配置服务
     */
    @Resource
    private ILineEfficiencyEhsPointsService lineEfficiencyEhsPointsService;

    /**
     * 外排水-推送记录服务
     */
    @Resource
    private ILineEfficiencyEhsRecordsService lineEfficiencyEhsRecordsService;

    /**
     * 工厂对照表服务
     */
    @Resource
    private ILineEfficiencyEhsFactoryService lineEfficiencyEhsFactoryServiceImpl;

    /**
     * 线效状态历史记录-天
     */
    @Resource
    private ILineEfficiencyStateRecordDayService lineEfficiencyStateRecordDayService;

    /**
     * 线效状态历史记录-清零拆分
     */
    @Resource
    private ILineEfficiencySkuService lineEfficiencySkuService;

    /**
     * 当前工厂对象
     */
    private LineEfficiencyEhsFactory factoryEntity = null;


    /**
     * influxDB客户端
     */
    @Resource
    private InfluxDBClient influxDBClient;

    @Resource
    private InfluxDBClientHelper influxDBClientHelper;

    /**
     * 开始计算线效状态-历史计算
     * @param startLocalDateTime 开始时间
     * @param endLocalDateTime 结束时间
     */
    @Override
    public void lineEfficiencyStateHistory(LocalDateTime startLocalDateTime,LocalDateTime endLocalDateTime){
        //通过开始时间往后推30分支
        while(startLocalDateTime.isBefore(endLocalDateTime)){
            lineEfficiencyStateByTimeFrame(startLocalDateTime);
            startLocalDateTime = getNext30MinutePoint(startLocalDateTime);
        }
        lineEfficiencyStateByTimeFrame(endLocalDateTime);
    }


    /**
     * 开始运行时长-历史计算
     * @param startLocalDateTime 开始时间
     * @param endLocalDateTime 结束时间
     */
    @Override
    public void machineMinuteHistory(LocalDateTime startLocalDateTime,LocalDateTime endLocalDateTime){
        //通过开始时间往后推30分支
        while(startLocalDateTime.isBefore(endLocalDateTime)){
            startLocalDateTime = getNext30MinutePoint(startLocalDateTime);
            machineMinuteByTimeFrame(startLocalDateTime);
        }
        machineMinuteByTimeFrame(endLocalDateTime);
    }


    /**
     * 报警（按照报警信号点来进行计算，计算出每次报警开始的时间）-历史计算
     * @param startLocalDateTime 开始时间
     * @param endLocalDateTime 结束时间
     */
    @Override
    public void alarmMinuteHistory(LocalDateTime startLocalDateTime,LocalDateTime endLocalDateTime){
        //通过开始时间往后推30分支
        while(startLocalDateTime.isBefore(endLocalDateTime)){
            startLocalDateTime = getNext30MinutePoint(startLocalDateTime);
            alarmMinuteByTimeFrame(startLocalDateTime);
        }
        alarmMinuteByTimeFrame(endLocalDateTime);
    }


    /**
     * 开始计算线效状态
     */
    @Override
    public void lineEfficiencyState() {
        // 获取当前时间
        LocalDateTime startLocalDateTime = LocalDateTime.now();
        lineEfficiencyStateByTimeFrame(startLocalDateTime);
    }


    /**
     * 开始运行时长
     */
    @Override
    public void machineMinute(){
        // 获取当前时间
        LocalDateTime startLocalDateTime = LocalDateTime.now();
        machineMinuteByTimeFrame(startLocalDateTime);
    }

    /**
     * 报警（按照报警信号点来进行计算，计算出每次报警开始的时间）
     */
    @Override
    public void alarmMinute(){
        // 获取当前时间
        LocalDateTime startLocalDateTime = LocalDateTime.now();
        alarmMinuteByTimeFrame(startLocalDateTime);
    }

    /**
     * 点位运行状态（按照点位信号点来进行计算，计算出每次运行开始的时间）
     */
    @Override
    public void operatingDurationForPoint(){
        // 获取当前时间
        LocalDateTime startLocalDateTime = LocalDateTime.now();
        operatingDurationForPointByTimeFrame(startLocalDateTime);
    }

    /**
     * 查询当前时间的外排水数据，然后发送给第三方
     */
    @Override
    public void queryEhsData(){
        //查询当前时间外排水数据
        List<LineEfficiencyEhsRecords> listRecords = getListRecords();
        //发送数据给第三方
        ehsSend(listRecords);
    }


    /**
     * 开始计算线效状态
     */
    public void lineEfficiencyStateByTimeFrame(LocalDateTime localDateTime){
        // 转换为 Date
        Date nowDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

        //查询所有线效开机和关机的配置
        List<Integer> handleTypeList = Arrays.asList(VariableUtil.HANDLE_TYPE_LINE_EFFICIENCY_STATE_OPEN,VariableUtil.HANDLE_TYPE_LINE_EFFICIENCY_STATE_CLOSE);

        //查询所有设备的线效状态
        List<LineEfficiencyHandle> list = lambdaQuery().in(LineEfficiencyHandle::getHandleType, handleTypeList).list();

        //查询线效状态最新的数据，从最新的数据表中进行查询
        List<LineEfficiencyStateLast> lineEfficiencyStateLastList = lineEfficiencyStateLastService.lambdaQuery().eq(LineEfficiencyStateLast::getType, 0).list();

        //必须同时配置了开机、关机两个配置信息，才能进行计算
        // 按 sbId 分组
        Map<String, List<LineEfficiencyHandle>> groupedBySbId = list.stream().collect(Collectors.groupingBy(LineEfficiencyHandle::getSbId));

        // 筛选出同时包含开机和关机配置的 sbId
        Set<String> validSbIds = groupedBySbId.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(item -> item.getHandleType().equals(VariableUtil.HANDLE_TYPE_LINE_EFFICIENCY_STATE_OPEN)) &&
                        entry.getValue().stream()
                                .anyMatch(item -> item.getHandleType().equals(VariableUtil.HANDLE_TYPE_LINE_EFFICIENCY_STATE_CLOSE)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        //最新记录
        List<LineEfficiencyStateLast> lineEfficiencyStateLastListNew = new ArrayList<>();

        //跟配置信息进行比对，将新的配置信息写入到表中
        for(String sbid : validSbIds){
            List<LineEfficiencyHandle> listHandle = groupedBySbId.get(sbid);

            Optional<LineEfficiencyStateLast> lineEfficiencyStateLastOptional = lineEfficiencyStateLastList.stream().filter(item -> item.getSbId().equals(sbid)).findFirst();
            LineEfficiencyStateLast  lineEfficiencyStateLast = new LineEfficiencyStateLast();
            lineEfficiencyStateLast.setRatedProduction(listHandle.get(0).getRatedProduction());

            if(!lineEfficiencyStateLastOptional.isPresent()){
                lineEfficiencyStateLast.setLineCode(listHandle.get(0).getLineCode());
                lineEfficiencyStateLast.setLineName(listHandle.get(0).getLineName());

                lineEfficiencyStateLast.setSbId(sbid);
                lineEfficiencyStateLast.setSbName(listHandle.get(0).getSbName());
                lineEfficiencyStateLast.setType(0);
                //按照当前时间往前推30分钟
                // 计算半小时前的时间
                long halfHourAgoMillis = nowDate.getTime() - 30 * 60 * 1000;
                Date halfHourAgo = new Date(halfHourAgoMillis);
                lineEfficiencyStateLast.setLastTime(halfHourAgo);

            }else{
                lineEfficiencyStateLast = lineEfficiencyStateLastOptional.get();
            }
            lineEfficiencyStateLast.setLineEfficiencyHandleList(listHandle);
            lineEfficiencyStateLastListNew.add(lineEfficiencyStateLast);
        }

        for(LineEfficiencyStateLast lineEfficiencyStateLast : lineEfficiencyStateLastListNew){
            //将当前时间和配置信息注入的对象中
            lineEfficiencyStateLast.setTriggerTime(nowDate);
            if(StringUtils.isBlank(lineEfficiencyStateLast.getStateCode()) || lineEfficiencyStateLast.getStateCode().equals(VariableUtil.LINE_EFFICIENCY_STATE_ZERO)){
                lineEfficiencyStateLast = lineEfficiencyStateStart(lineEfficiencyStateLast);
            }
            if(VariableUtil.LINE_EFFICIENCY_STATE_ONE.equals(lineEfficiencyStateLast.getStateCode())){
                lineEfficiencyStateLast = lineEfficiencyStateEnd(lineEfficiencyStateLast);
            }
        }
        saveLineEfficiencyStateLast(lineEfficiencyStateLastListNew);
    }

    /**
     * 设备运行时长（按照设备开机信号点来进行计算，计算出每次开机关机的时间）
     */
    public void machineMinuteByTimeFrame(LocalDateTime localDateTime){
        Date nowDate =  Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        //查询所有设备的运行时长状态
        List<LineEfficiencyHandle> list = lambdaQuery().eq(LineEfficiencyHandle::getHandleType, VariableUtil.HANDLE_TYPE_LINE_POWER_SIGNAL)
                .list();

        //按 sbId 分组
        Map<String, List<LineEfficiencyHandle>> groupedBySbId = list.stream().collect(Collectors.groupingBy(LineEfficiencyHandle::getSbId));

        //查询设备运行时长的最新数据
        List<LineEfficiencyStateLast> lineEfficiencyStateLastList = lineEfficiencyStateLastService.lambdaQuery().eq(LineEfficiencyStateLast::getType, 1).list();

        //最新的数据
        List<LineEfficiencyStateLast> lineEfficiencyStateLastListNew = new ArrayList<>();

        for(String sbId : groupedBySbId.keySet()){
            //获取设备的运行时长配置信息
            List<LineEfficiencyHandle> handleList = groupedBySbId.get(sbId);
            Optional<LineEfficiencyStateLast> lineEfficiencyStateLastOptional = lineEfficiencyStateLastList.stream().filter(item -> item.getSbId().equals(sbId)).findFirst();
            LineEfficiencyStateLast  lineEfficiencyStateLast = new LineEfficiencyStateLast();
            if(!lineEfficiencyStateLastOptional.isPresent()){
                lineEfficiencyStateLast.setLineCode(handleList.get(0).getLineCode());
                lineEfficiencyStateLast.setLineName(handleList.get(0).getLineName());

                lineEfficiencyStateLast.setSbId(sbId);
                lineEfficiencyStateLast.setSbName(handleList.get(0).getSbName());
                lineEfficiencyStateLast.setType(1);
                //按照当前时间往前推30分钟
                long halfHourAgoMillis = nowDate.getTime() - 30 * 60 * 1000;
                Date halfHourAgo = new Date(halfHourAgoMillis);
                lineEfficiencyStateLast.setLastTime(halfHourAgo);
            }else{
                lineEfficiencyStateLast = lineEfficiencyStateLastOptional.get();
            }
            lineEfficiencyStateLast.setLineEfficiencyHandleList(handleList);
            lineEfficiencyStateLastListNew.add(lineEfficiencyStateLast);
        }
        for (LineEfficiencyStateLast LineEfficiencyStateLast :  lineEfficiencyStateLastListNew){
            //防止意外情况次产生，定时任务没有执行，导致开始时间和当前时间中间存在跨天的情况，需要分段进行查询
            //获取开始时间
            LocalDateTime startLocalDateTime = LineEfficiencyStateLast.getLastTime().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            //寻找开始时间最近的一个30分钟的整数点
            startLocalDateTime = getNext30MinutePoint(startLocalDateTime);

            //通过开始时间往后推30分支
            while(startLocalDateTime.isBefore(localDateTime)){
                Date startLocalDateTimeDate =  Date.from(startLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
                LineEfficiencyStateLast.setTriggerTime(startLocalDateTimeDate);
                LineEfficiencyStateLast = machineMinuteStartAndEnd(LineEfficiencyStateLast);

                startLocalDateTime = getNext30MinutePoint(startLocalDateTime);
            }

            LineEfficiencyStateLast.setTriggerTime(nowDate);
            LineEfficiencyStateLast = machineMinuteStartAndEnd(LineEfficiencyStateLast);

            //考虑到设备太多，跨天数太多导致无法批量保存，改成按照单设备进行保存
            saveLineEfficiencyStateLast(Arrays.asList(LineEfficiencyStateLast));
        }
//        saveLineEfficiencyStateLast(lineEfficiencyStateLastListNew);
    }


    /**
     * 报警（按照报警信号点来进行计算，计算出每次报警开始的时间）
     * @param localDateTime
     */
    public void alarmMinuteByTimeFrame(LocalDateTime localDateTime){

        Date nowDate =  Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        //查询所有设备的线效状态
        List<LineEfficiencyHandle> list = lambdaQuery().eq(LineEfficiencyHandle::getHandleType, VariableUtil.HANDLE_TYPE_ALARM).list();

        //查询设备报警的最新数据
        List<LineEfficiencyStateLast> lineEfficiencyStateLastList = lineEfficiencyStateLastService.lambdaQuery().eq(LineEfficiencyStateLast::getType, 2).list();

        //最新的数据
        List<LineEfficiencyStateLast> lineEfficiencyStateLastListNew = new ArrayList<>();

        for(LineEfficiencyHandle lineEfficiencyHandle : list){
            Optional<LineEfficiencyStateLast> lineEfficiencyStateLastOptional = lineEfficiencyStateLastList.stream().filter(item -> item.getSbId().equals(lineEfficiencyHandle.getSbId())).findFirst();
            LineEfficiencyStateLast  lineEfficiencyStateLast = new LineEfficiencyStateLast();
            if(!lineEfficiencyStateLastOptional.isPresent()){
                lineEfficiencyStateLast.setLineCode(lineEfficiencyHandle.getLineCode());
                lineEfficiencyStateLast.setLineName(lineEfficiencyHandle.getLineName());

                lineEfficiencyStateLast.setSbId(lineEfficiencyHandle.getSbId());
                lineEfficiencyStateLast.setSbName(lineEfficiencyHandle.getSbName());
                lineEfficiencyStateLast.setType(2);
                lineEfficiencyStateLast.setAlarmName(lineEfficiencyHandle.getPointName());

                //按照当前时间往前推30分钟
                //获取当前时间
//                Date now = new Date();
                // 计算半小时前的时间
                long halfHourAgoMillis = nowDate.getTime() - 30 * 60 * 1000;
                Date halfHourAgo = new Date(halfHourAgoMillis);
                lineEfficiencyStateLast.setLastTime(halfHourAgo);
            }else{
                lineEfficiencyStateLast = lineEfficiencyStateLastOptional.get();
            }
            lineEfficiencyStateLast.setLineEfficiencyHandleList(Arrays.asList(lineEfficiencyHandle));
            lineEfficiencyStateLastListNew.add(lineEfficiencyStateLast);
        }
        for (LineEfficiencyStateLast LineEfficiencyStateLast :  lineEfficiencyStateLastListNew){
            LineEfficiencyStateLast.setTriggerTime(nowDate);
            LineEfficiencyStateLast = machineMinuteStartAndEnd(LineEfficiencyStateLast);
        }
        saveLineEfficiencyStateLast(lineEfficiencyStateLastListNew);
    }


    /**
     * 点位的运行状态（查询时间范围内点位所有状态的运行时长）
     * @param localDateTime
     */
    public void operatingDurationForPointByTimeFrame(LocalDateTime localDateTime){
        Date nowDate =  Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        //查询所有设备的线效状态
        List<LineEfficiencyHandle> list = lambdaQuery().eq(LineEfficiencyHandle::getHandleType, VariableUtil.OPERATING_DURATION_POINT).list();

        //查询设备报警的最新数据
        List<LineEfficiencyStateLast> lineEfficiencyStateLastList = lineEfficiencyStateLastService.lambdaQuery().eq(LineEfficiencyStateLast::getType, 6).list();

        //最新的数据
        List<LineEfficiencyStateLast> lineEfficiencyStateLastListNew = new ArrayList<>();

        for(LineEfficiencyHandle lineEfficiencyHandle : list){
            Optional<LineEfficiencyStateLast> lineEfficiencyStateLastOptional = lineEfficiencyStateLastList.stream().filter(item -> item.getSbId().equals(lineEfficiencyHandle.getSbId())).findFirst();
            LineEfficiencyStateLast  lineEfficiencyStateLast = new LineEfficiencyStateLast();
            if(!lineEfficiencyStateLastOptional.isPresent()){
                lineEfficiencyStateLast.setLineCode(lineEfficiencyHandle.getLineCode());
                lineEfficiencyStateLast.setLineName(lineEfficiencyHandle.getLineName());

                lineEfficiencyStateLast.setSbId(lineEfficiencyHandle.getSbId());
                lineEfficiencyStateLast.setSbName(lineEfficiencyHandle.getSbName());
                //点位故障状态
                lineEfficiencyStateLast.setType(6);
                lineEfficiencyStateLast.setAlarmName(lineEfficiencyHandle.getPointName());

                //按照当前时间往前推30分钟
                //获取当前时间
//                Date now = new Date();
                // 计算半小时前的时间
                long halfHourAgoMillis = nowDate.getTime() - 30 * 60 * 1000;
                Date halfHourAgo = new Date(halfHourAgoMillis);
                lineEfficiencyStateLast.setLastTime(halfHourAgo);
            }else{
                lineEfficiencyStateLast = lineEfficiencyStateLastOptional.get();
            }
            lineEfficiencyStateLast.setLineEfficiencyHandleList(Arrays.asList(lineEfficiencyHandle));
            lineEfficiencyStateLastListNew.add(lineEfficiencyStateLast);
        }
        for (LineEfficiencyStateLast LineEfficiencyStateLast :  lineEfficiencyStateLastListNew){
            LineEfficiencyStateLast.setTriggerTime(nowDate);
            LineEfficiencyStateLast = machineMinuteStartAndEnd(LineEfficiencyStateLast);
        }
        saveLineEfficiencyStateLast(lineEfficiencyStateLastListNew);
    }

    /**
     * 判断设备开机时间/关机时间，报警的开始时间/结束时间，设备运行状态开始时间/结束时间
     * @param lineEfficiencyStateLast
     * @return
     */
    public LineEfficiencyStateLast machineMinuteStartAndEnd(LineEfficiencyStateLast lineEfficiencyStateLast) {
        //获取当前时间
        LocalDateTime nowLocalDateTime = lineEfficiencyStateLast.getTriggerTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        //获取开始时间
        LocalDateTime startTime = lineEfficiencyStateLast.getLastTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        //获取设备运行状态点位
        List<String> machineRunningStateList = lineEfficiencyStateLast.getLineEfficiencyHandleList().stream().map(LineEfficiencyHandle::getPointCode).collect(Collectors.toList());

        //定义取数频率 默认1s
        Integer frequency = 1;
        List<MeasurementData> listPointDataCodes = new ArrayList<>();
        if(startTime.isBefore(nowLocalDateTime)){
            listPointDataCodes = piComputationModelService.getMeasurementDataList(startTime,nowLocalDateTime,machineRunningStateList,frequency);
        }

        if(null == listPointDataCodes || listPointDataCodes.size() == 0){
            //更新触发时间
            lineEfficiencyStateLast.setLastTime(lineEfficiencyStateLast.getTriggerTime());
            return lineEfficiencyStateLast;
        }
        //最新的集合
        List<MeasurementData> listPointDataNew = new ArrayList<>();

        //当是设备运行状态判断的时候，跳过以下判断步骤
        if (lineEfficiencyStateLast.getType() == 6){
            listPointDataNew = listPointDataCodes;
        }else{
            // 如果只需要去重后的 Instant 列表，而不是字符串表示
            List<Instant> uniqueTimes = listPointDataCodes.stream()
                    .map(MeasurementData::getTime)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            //按照多个点位的数据进行处理，多个点位，任意一个点位满足条件，则认为该设备已经运行或者已经停止运行
            for(Instant time : uniqueTimes){
                MeasurementData measurementData = new MeasurementData(time,null,null);
                List<MeasurementData> points = listPointDataCodes.stream()
                        .filter(e -> e.getTime().equals(time))
                        .collect(Collectors.toList());
                //符合条件给1，不符合条件给0，默认不符合条件
                Double value = 0.0;
                for(MeasurementData data : points){
                    //获取配置的条件信息
                    LineEfficiencyHandle lineEfficiencyHandle = lineEfficiencyStateLast.getLineEfficiencyHandleList().stream().filter(e -> e.getPointCode().equals(data.getCnlNum())).findFirst().get();

                    //当所有条件全部为空的时候，默认0为不运行，大于0为运行
                    if(
                            StringUtils.isBlank(lineEfficiencyHandle.getLeftConditionType()) &&
                                    null == lineEfficiencyHandle.getLeftConditionValue() &&
                                    StringUtils.isBlank(lineEfficiencyHandle.getRightConditionType()) &&
                                    null == lineEfficiencyHandle.getRightConditionValue()
                    ){
                        if(data.getValue() > 0){
                            value = 1.0;
                            break;
                        }
                    }

                    //第一个条件成立的状态
                    boolean leftCondition = false;
                    //第二个条件成立的状态
                    boolean rightCondition = false;
                    //当所有条件都不为空的时候，需要判断且和或的关系
                    if(
                            StringUtils.isNotBlank(lineEfficiencyHandle.getLeftConditionType()) &&
                                    null != lineEfficiencyHandle.getLeftConditionValue() &&
                                    StringUtils.isNotBlank(lineEfficiencyHandle.getRightConditionType()) &&
                                    null != lineEfficiencyHandle.getRightConditionValue()
                    ){
                        //逻辑关系不能为空，否则直接默认不运行
                        if(StringUtils.isNotBlank(lineEfficiencyHandle.getConditionSymbol())){
                            leftCondition = isInclude(lineEfficiencyHandle.getLeftConditionType(),data.getValue(),lineEfficiencyHandle.getLeftConditionValue());
                            rightCondition = isInclude(lineEfficiencyHandle.getRightConditionType(),data.getValue(),lineEfficiencyHandle.getRightConditionValue());
                            //或的关系
                            if(lineEfficiencyHandle.getConditionSymbol().equals("0")){
                                if(leftCondition || rightCondition){
                                    value = 1.0;
                                    break;
                                }
                            }
                            //且的关系
                            if(lineEfficiencyHandle.getConditionSymbol().equals("1")){
                                if(leftCondition && rightCondition){
                                    value = 1.0;
                                    break;
                                }
                            }
                        }
                    }else{
                        //第一个条件
                        if(StringUtils.isNotBlank(lineEfficiencyHandle.getLeftConditionType()) && null != lineEfficiencyHandle.getLeftConditionValue()){
                            leftCondition = isInclude(lineEfficiencyHandle.getLeftConditionType(),data.getValue(),lineEfficiencyHandle.getLeftConditionValue());
                            if(leftCondition){
                                value = 1.0;
                                break;
                            }
                        }
                        if(StringUtils.isNotBlank(lineEfficiencyHandle.getRightConditionType()) && null != lineEfficiencyHandle.getRightConditionValue()){
                            rightCondition = isInclude(lineEfficiencyHandle.getRightConditionType(),data.getValue(),lineEfficiencyHandle.getRightConditionValue());
                            if(rightCondition){
                                value = 1.0;
                                break;
                            }
                        }
                    }
                }
                measurementData.setValue(value);
                listPointDataNew.add(measurementData);
            }
        }

        //持续时长
        int duration = 5;
        //报警需要需要持续5s，开关机需要持续60秒
        if(lineEfficiencyStateLast.getType().equals(1)){
            duration = 60;
        }
        //故障报警
        if(lineEfficiencyStateLast.getType().equals(6)){
            duration = 1;
        }

        //数据记录表
        List<LineEfficiencyStateRecord> listRecord = new ArrayList<>();

        //持续时间
        Instant newTime = null;

        for (MeasurementData measurementData : listPointDataNew){
            //每次更新最新时间到最新记录表中
            lineEfficiencyStateLast.setLastTime(Date.from(measurementData.getTime()));

            String data = Objects.isNull(measurementData.getValue()) ? "" :  measurementData.getValue().intValue()+"";

            if(StringUtils.isBlank(lineEfficiencyStateLast.getStateCode())){
                lineEfficiencyStateLast.setStateCode(data);
            }
            if(Objects.isNull(lineEfficiencyStateLast.getStartTime())){
                lineEfficiencyStateLast.setStartTime(Date.from(measurementData.getTime()));
            }

            if(!data.equals(lineEfficiencyStateLast.getStateCode())){
                //初始秒
                long initialSeconds = Objects.isNull(lineEfficiencyStateLast.getRunningTime()) ? 0 : lineEfficiencyStateLast.getRunningTime().longValue();

                if(Objects.isNull(newTime)){
                    //初始秒数，防止上一个任务执行完了后，没有运行到指定范围的秒数
                    newTime = measurementData.getTime().minusSeconds(initialSeconds);
                }

                long seconds = Duration.between(newTime, measurementData.getTime()).getSeconds();

                lineEfficiencyStateLast.setRunningTime((double) seconds);

                if(seconds >= duration){
                    //写入到历史记录表
                    LineEfficiencyStateRecord stateRecord = new LineEfficiencyStateRecord();
                    stateRecord.setLineCode(lineEfficiencyStateLast.getLineCode());
                    stateRecord.setLineName(lineEfficiencyStateLast.getLineName());

                    stateRecord.setSbId(lineEfficiencyStateLast.getSbId());
                    stateRecord.setSbName(lineEfficiencyStateLast.getSbName());
                    stateRecord.setStateCode(lineEfficiencyStateLast.getStateCode());
                    stateRecord.setType(lineEfficiencyStateLast.getType());

                    stateRecord.setStartTime(lineEfficiencyStateLast.getStartTime());

                    stateRecord.setEndTime(Date.from(newTime));

                    stateRecord.setLineEfficiencyHandleList(lineEfficiencyStateLast.getLineEfficiencyHandleList());
                    stateRecord.setDelFlag(0);

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
                    stateRecord.setAlarmName(lineEfficiencyStateLast.getAlarmName());

                    listRecord.add(stateRecord);

                    //添加后，更改最新数据表对象的数据
                    lineEfficiencyStateLast.setStateCode(data);
                    lineEfficiencyStateLast.setStartTime(Date.from(newTime));

                    lineEfficiencyStateLast.setRunningTime(0.0);

                    newTime = null;
                }
            }else{
                lineEfficiencyStateLast.setRunningTime(0.0);
                newTime = null;
            }
        }
        //获取原本已经存在的listRecord
        List<LineEfficiencyStateRecord> listRecordOld = lineEfficiencyStateLast.getListRecord();
        if(null == listRecordOld || listRecordOld.size() == 0){
            listRecordOld = listRecord;
        }else{
            listRecordOld.addAll(listRecord);
        }
        lineEfficiencyStateLast.setListRecord(listRecordOld);

        //运行信号才做12点的跨天处理，报警时长不做处理
        if(lineEfficiencyStateLast.getType().equals(2) || lineEfficiencyStateLast.getType().equals(3)){
            return lineEfficiencyStateLast;
        }

        //单独考虑12点钟的情况，12点钟需要进行分段处理，如果运行状态跨天，到12点钟要进行终止
        // 将 Date 转换为 LocalDateTime（使用系统默认时区）
        LocalDateTime triggerDateTime = lineEfficiencyStateLast.getTriggerTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // 获取当天的 00:00:00 时间
        LocalDateTime todayMidnight = triggerDateTime.with(LocalTime.MIDNIGHT);

        // 计算两个时间之间相差的分钟数
        long minutesDifference = java.time.Duration.between(todayMidnight, triggerDateTime).toMinutes();
        //相差10分钟，则证明是0点触发
        if(minutesDifference < 10){

            String stateCodeRecord = lineEfficiencyStateLast.getStateCode();
            Date startTimeRecord = lineEfficiencyStateLast.getStartTime();

            //开始时间为空，则直接返回
            if(Objects.isNull(startTimeRecord)){
                return  lineEfficiencyStateLast;
            }

            //获取当天最后1秒
            LocalDateTime endOfDay = startTimeRecord.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(23, 59, 59);
            Date endTimeRecord = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());

            long runningTime = 0;

            LineEfficiencyStateRecord lastStateRecordNew = new LineEfficiencyStateRecord();

            //获取当前最后一条运行时长数据
            if(null != lineEfficiencyStateLast.getListRecord() && lineEfficiencyStateLast.getListRecord().size() > 0){
                LineEfficiencyStateRecord lastStateRecordTemp = lineEfficiencyStateLast.getListRecord().get(lineEfficiencyStateLast.getListRecord().size() - 1);
                if(lastStateRecordTemp.getStateCode().equals(VariableUtil.RUNNING_STATE_NOT_RUNNING)){
                    stateCodeRecord = VariableUtil.RUNNING_STATE_RUNNING;
                }else{
                    stateCodeRecord = VariableUtil.RUNNING_STATE_NOT_RUNNING;
                }
                startTimeRecord = lastStateRecordTemp.getEndTime();
            }
            runningTime = Duration.between(
                    startTimeRecord.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime(),
                    endOfDay
            ).getSeconds();
            //排除极端情况，刚好12点钟跳变
            if(runningTime > 1){
                lastStateRecordNew.setLineCode(lineEfficiencyStateLast.getLineCode());
                lastStateRecordNew.setLineName(lineEfficiencyStateLast.getLineName());

                lastStateRecordNew.setSbId(lineEfficiencyStateLast.getSbId());
                lastStateRecordNew.setSbName(lineEfficiencyStateLast.getSbName());
                lastStateRecordNew.setType(lineEfficiencyStateLast.getType());
                lastStateRecordNew.setLineEfficiencyHandleList(lineEfficiencyStateLast.getLineEfficiencyHandleList());
                lastStateRecordNew.setDelFlag(0);

                lastStateRecordNew.setStateCode(stateCodeRecord);
                lastStateRecordNew.setStartTime(startTimeRecord);
                lastStateRecordNew.setEndTime(endTimeRecord);

                lastStateRecordNew.setRunningTime(runningTime);

                lastStateRecordNew.setAlarmName(lineEfficiencyStateLast.getAlarmName());

                lineEfficiencyStateLast.getListRecord().add(lastStateRecordNew);

                //获取工厂
                LineEfficiencyEhsFactory factoryVo = getFactoryCode();
                //是否需要拆分秒
                int ifSplitSecond = null == factoryVo.getIfSplitSecond() ? 0 : factoryVo.getIfSplitSecond();
                //特殊需求，南京工厂如果当天设备没有运行，生成一个1秒的运行数据，南京工厂的代码CR0121270016
                //当天设备没有运行，生成一个1秒的运行数据
                if(lastStateRecordNew.getStateCode().equals(VariableUtil.RUNNING_STATE_NOT_RUNNING) && runningTime == 86399 && ifSplitSecond == 1){
                    LineEfficiencyStateRecord lastStateRecordRunning = new LineEfficiencyStateRecord();
                    lastStateRecordRunning.setLineCode(lineEfficiencyStateLast.getLineCode());
                    lastStateRecordRunning.setLineName(lineEfficiencyStateLast.getLineName());

                    lastStateRecordRunning.setSbId(lineEfficiencyStateLast.getSbId());
                    lastStateRecordRunning.setSbName(lineEfficiencyStateLast.getSbName());
                    lastStateRecordRunning.setType(lineEfficiencyStateLast.getType());
                    lastStateRecordRunning.setLineEfficiencyHandleList(lineEfficiencyStateLast.getLineEfficiencyHandleList());
                    lastStateRecordRunning.setDelFlag(0);

                    lastStateRecordRunning.setStateCode(VariableUtil.RUNNING_STATE_RUNNING);
                    lastStateRecordRunning.setStartTime(endTimeRecord);
                    lastStateRecordRunning.setEndTime(endTimeRecord);
                    lastStateRecordRunning.setRunningTime(1L);
                    lastStateRecordRunning.setAlarmName(lineEfficiencyStateLast.getAlarmName());

                    lineEfficiencyStateLast.getListRecord().add(lastStateRecordRunning);
                }

                //修改最新数据表的数据状态
                Date newEndTime = Date.from(endOfDay.plusSeconds(1).atZone(ZoneId.systemDefault()).toInstant());
                lineEfficiencyStateLast.setStateCode(stateCodeRecord);
                lineEfficiencyStateLast.setStartTime(newEndTime);
                lineEfficiencyStateLast.setRunningTime(null);
                lineEfficiencyStateLast.setLastTime(newEndTime);
            }
        }
        return lineEfficiencyStateLast;
    }

    /**
     * 线效状态（计算开机时间）产量点位必须，酒机运行信号点位可以缺少
     */
    public LineEfficiencyStateLast lineEfficiencyStateStart(LineEfficiencyStateLast lineEfficiencyStateLast){
        //开始执行线效状态,获取当前时间
        Date nowDate = lineEfficiencyStateLast.getTriggerTime();
        LocalDateTime nowLocalDateTime = nowDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        //开机配置
        LineEfficiencyHandle lineEfficiencyHandle = new LineEfficiencyHandle();
        Optional<LineEfficiencyHandle> lineEfficiencyHandleOptional = lineEfficiencyStateLast.getLineEfficiencyHandleList().stream().filter(e->e.getHandleType().equals(1)).findAny();
        if(lineEfficiencyHandleOptional.isPresent()){
            lineEfficiencyHandle = lineEfficiencyHandleOptional.get();
        }
        //当酒机运行信号进入1开始计算到产量点位增量值>=1000

        //开始时间
        Instant startInstantTime = null;

        //如果状态为空\状态为0，则开始计算开机时间
        if(StringUtils.isBlank(lineEfficiencyStateLast.getStateCode()) || lineEfficiencyStateLast.getStateCode().equals(VariableUtil.LINE_EFFICIENCY_STATE_ZERO)){
            if(Objects.isNull(lineEfficiencyStateLast.getLastProduction())){
                //酒机运行信号
                String wineSignalCode = lineEfficiencyHandle.getPointCodeNew();
                //酒机运行信号为空，默认1
                if(StringUtils.isBlank(wineSignalCode)){
                    //定义初始产量
                    lineEfficiencyStateLast.setLastProduction(0.0);
                }else{
                    Integer frequency = 1;
                    List<MeasurementData> listPointData = new ArrayList<>();
                    LocalDateTime startTime = lineEfficiencyStateLast.getLastTime().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    if(startTime.isBefore(nowLocalDateTime)){
                        listPointData = piComputationModelService.getMeasurementDataList(startTime,nowLocalDateTime,Arrays.asList(wineSignalCode),frequency);
                    }

                    if(null == listPointData || listPointData.size() == 0 || null == listPointData.get(0)){
                        return lineEfficiencyStateLast;
                    }
                    //判断酒机运行信号等于1，获取等于1的时间，持续10秒
                    //持续时长
                    long duration = 10;

                    for(MeasurementData measurementData : listPointData){
                        lineEfficiencyStateLast.setLastTime(Date.from(measurementData.getTime()));

                        if(measurementData.getValue() == 1 && null == startInstantTime){
                            startInstantTime = measurementData.getTime();
                            continue;
                        }
                        if(measurementData.getValue() != 1){
                            startInstantTime = null;
                            continue;
                        }
                        long seconds = Duration.between(startInstantTime, measurementData.getTime()).getSeconds();
                        if(measurementData.getValue() == 1 && seconds >= duration){
                            //定义初始产量
                            lineEfficiencyStateLast.setLastProduction(0.0);
                            break;
                        }
                    }
                }
            }
        }
        //产量
        String productionCode = lineEfficiencyHandle.getPointCode();
        //产量为空，直接返回
        if(StringUtils.isBlank(productionCode)){
            return lineEfficiencyStateLast;
        }

        if(null == startInstantTime){
            startInstantTime = lineEfficiencyStateLast.getLastTime().toInstant();
        }

        if(Objects.nonNull(lineEfficiencyStateLast.getLastProduction())){
            ProductionDto productionDto = new ProductionDto();
            productionDto.setProductionCode(productionCode);
            productionDto.setProductionTime(Date.from(startInstantTime));
            productionDto.setProductionBase(lineEfficiencyStateLast.getLastProduction());

            LocalDateTime lastDate = startInstantTime.atZone(ZoneId.systemDefault()).toLocalDateTime();

            ProductionDto productionDtoRet = getProductionByTime(lastDate,nowLocalDateTime,productionDto,VariableUtil.PRODUCTION_INITIAL);
            //如果产量达标，设置开始时间
            lineEfficiencyStateLast.setLastProduction(productionDtoRet.getProduction());
            Double production = Objects.isNull(productionDtoRet.getProduction()) ? 0.0 : productionDtoRet.getProduction();
            if( production >= VariableUtil.PRODUCTION_INITIAL){
                //状态为空的情况
                if(StringUtils.isBlank(lineEfficiencyStateLast.getStateCode())){
                    lineEfficiencyStateLast.setStartTime(productionDtoRet.getProductionTime());
                    lineEfficiencyStateLast.setStateCode(VariableUtil.LINE_EFFICIENCY_STATE_ONE);
                }
                //状态为0的情况
                if(lineEfficiencyStateLast.getStateCode().equals(VariableUtil.LINE_EFFICIENCY_STATE_ZERO)){
                    lineEfficiencyStateLast.setEndTime(productionDtoRet.getProductionTime());
                }
                lineEfficiencyStateLast.setLastTime(productionDtoRet.getProductionTime());
            }else{
                lineEfficiencyStateLast.setLastTime(nowDate);
            }
        }
        return lineEfficiencyStateLast;
    }

    /**
     * 线效状态（计算关机时间）当CIP信号进入1且进酒温度>=55度且持续2分钟及以上线效状态置0，CIP信号和温度信号至少有一个
     * @param lineEfficiencyStateLast
     * @return
     */
    public LineEfficiencyStateLast lineEfficiencyStateEnd(LineEfficiencyStateLast lineEfficiencyStateLast){

        //关机配置
        LineEfficiencyHandle lineEfficiencyHandle = new LineEfficiencyHandle();
        Optional<LineEfficiencyHandle> lineEfficiencyHandleOptional = lineEfficiencyStateLast.getLineEfficiencyHandleList().stream().filter(e->e.getHandleType().equals(2)).findAny();
        if(lineEfficiencyHandleOptional.isPresent()){
            lineEfficiencyHandle = lineEfficiencyHandleOptional.get();
        }

        if(lineEfficiencyStateLast.getStateCode().equals(VariableUtil.LINE_EFFICIENCY_STATE_ONE)){
            //获取当前时间
            Date nowDateTime = lineEfficiencyStateLast.getTriggerTime();

            //获取当前时间
            LocalDateTime nowLocalDateTime = nowDateTime.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();


            //获取开始时间
            LocalDateTime startTime = lineEfficiencyStateLast.getLastTime().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            //CIP运行信号
            String cipSignalCode = lineEfficiencyHandle.getPointCode();
            //进酒温度
            String temperatureCode = lineEfficiencyHandle.getPointCodeNew();

            //都为空，直接返回
            if(StringUtils.isBlank(cipSignalCode) && StringUtils.isBlank(temperatureCode)){
                return lineEfficiencyStateLast;
            }

            //定义取数频率 默认1s
            Integer frequency = 1;
            List<MeasurementData> listPointDataCodes = new ArrayList<>();
            if(startTime.isBefore(nowLocalDateTime)){
                listPointDataCodes = piComputationModelService.getMeasurementDataList(startTime,nowLocalDateTime,Arrays.asList(cipSignalCode,temperatureCode),frequency);
            }
            List<MeasurementData>  cipSignalCodeListData = listPointDataCodes.stream().filter(measurementData -> measurementData.getCnlNum().equals(cipSignalCode)).collect(Collectors.toList());
            List<MeasurementData>  temperatureCodeListData = listPointDataCodes.stream().filter(measurementData -> measurementData.getCnlNum().equals(temperatureCode)).collect(Collectors.toList());

            //初始时间
            Instant firstTime = null;
            //结束时间
            Instant lastTime = null;
            //持续时长
            long duration = 30;
            //判断时间段内，cip运行信号符合条件的集合，连续30秒以上
            List<Pair<Instant,Instant>> listTime = new ArrayList<>();

            //判断CIP信号是否为空
            if(StringUtils.isBlank(cipSignalCode)){
                firstTime = startTime.atZone(ZoneId.systemDefault()).toInstant();
                lastTime = nowLocalDateTime.atZone(ZoneId.systemDefault()).toInstant();
                listTime.add(Pair.of(firstTime,lastTime));
            }else{
                for(MeasurementData cipSignalCodeData : cipSignalCodeListData){
                    //获取当前时间
                    Instant dataTime = cipSignalCodeData.getTime();
                    lastTime = dataTime;

                    if(cipSignalCodeData.getValue() == 1){
                        if(Objects.isNull(firstTime)){
                            firstTime = dataTime;
                        }
                    }else{
                        if(Objects.nonNull(firstTime)){
                            long seconds = Duration.between(firstTime, dataTime).getSeconds();
                            if(seconds >= duration){
                                listTime.add(Pair.of(firstTime,dataTime));
                            }
                            firstTime = null;
                        }
                    }
                }
                if(Objects.nonNull(firstTime) && Objects.nonNull(lastTime)){
                    long seconds = Duration.between(firstTime, lastTime).getSeconds();
                    if(seconds >= duration){
                        listTime.add(Pair.of(firstTime,lastTime));
                    }
                }
            }
            //cip符合要求的时间段，然后判断温度是否符合要求
            //设定温度
            Double temperatureValue = 55.0;
            outerLoop:
            for(Pair<Instant,Instant> time : listTime){
                //如果温度为空，则直接返回
                if(StringUtils.isBlank(temperatureCode)){
                    lineEfficiencyStateLast.setEndTime(Date.from(time.getKey()));
                }else{
                    //初始时间
                    firstTime = null;
                    //过滤出符合要求的温度数据
                    List<MeasurementData> measurementDataList = temperatureCodeListData.stream().filter(data -> data.getTime().isAfter(time.getKey()) && data.getTime().isBefore(time.getValue())).collect(Collectors.toList());
                    //判断是否连续
                    for (MeasurementData measurementData : measurementDataList){
                        lastTime = measurementData.getTime();
                        if(measurementData.getValue() >= temperatureValue){
                            if(Objects.isNull(firstTime)){
                                firstTime = measurementData.getTime();
                                continue;
                            }
                            long seconds = Duration.between(firstTime, lastTime).getSeconds();
                            if(seconds >= duration){
                                lineEfficiencyStateLast.setEndTime(Date.from(firstTime));
                                break outerLoop;
                            }
                        }else{
                            firstTime = null;
                        }
                    }
                }
            }
            if(Objects.isNull(lineEfficiencyStateLast.getEndTime())){
                lineEfficiencyStateLast.setLastTime(nowDateTime);
            }
        }
        return lineEfficiencyStateLast;
    }


    /**
     * 防清零计算逻辑。传入时间和产量点位列表，获取区间内的增量产量，当limits限制产量不为空，返回达到限制产量的一瞬间的数据，如果为空，则返回时间内的产量总和
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pointDto 点位
     * @param limits 限制产量，大于限制产量即返回
     * @return
     */
    @Override
    public ProductionDto getProductionByTime(LocalDateTime startTime, LocalDateTime endTime,ProductionDto pointDto,Double limits){
        //频率 1秒
        Integer frequency = 1;
        //获取点位集合的最新数据
        String points = pointDto.getProductionCode();
        List<MeasurementData> listPointDataCodes = new ArrayList<>();
        if(startTime.isBefore(endTime)){
            listPointDataCodes = piComputationModelService.getMeasurementDataList(startTime,endTime.plusSeconds(1),Arrays.asList(points),frequency);
        }
        // 将listPointDataCodes集合中产量为0的排除掉，但保留第一个0值
        AtomicBoolean firstZeroFound = new AtomicBoolean(false);
        listPointDataCodes = listPointDataCodes.stream().filter(measurementData -> {
            if (measurementData.getValue() != 0) {
                // 非0值直接保留
                return true;
            } else {
                // 值为0的情况
                if (firstZeroFound.compareAndSet(false, true)) {
                    // 第一个0值保留
                    return true;
                } else {
                    // 后续的0值排除
                    return false;
                }
            }
        }).collect(Collectors.toList());
        //累计产量
        Double production = Objects.isNull(pointDto.getProductionBase()) ? 0.0 : pointDto.getProductionBase();
        //每秒产量最大值
        Double productionLimitsMax = 10000.0;

        Double productionLimitsMin = 1000.0;

        //记录第一个点位产量
        Double productionTemp = listPointDataCodes.stream().findFirst().map(e->e.getValue()).map(e->new BigDecimal(e)).orElse(BigDecimal.ZERO).doubleValue();
        for(MeasurementData measurementData : listPointDataCodes){
            Double difference = BigDecimal.valueOf(measurementData.getValue()).subtract(BigDecimal.valueOf(productionTemp)).doubleValue();
            if(difference >= 0){
                if(difference < productionLimitsMax){
                    production = production + difference;
                }
            } else{
                if(measurementData.getValue() < productionLimitsMin){
                    production = production + measurementData.getValue();
                }
            }
            pointDto.setProductionTime(Date.from(measurementData.getTime()));
            pointDto.setProduction(production);

            productionTemp = measurementData.getValue();
            if(Objects.nonNull(limits) && limits != 0 && production >= limits){
                break;
            }
        }
        return pointDto;
    }

    /**
     * 保存实时数据表
     * @param lineEfficiencyStateLastList
     */
    public void saveLineEfficiencyStateLast(List<LineEfficiencyStateLast> lineEfficiencyStateLastList){

        //只有lineEfficiencyStateLast对象中开始时间和结束时间都不为空的时候才进行保存，保存到线效状态记录表，然后更新线效状态实时表

        List<LineEfficiencyStateRecord> lineEfficiencyStateRecordList = new ArrayList<>();
        for (LineEfficiencyStateLast lineEfficiencyStateLast : lineEfficiencyStateLastList){
            lineEfficiencyStateLast.setDelFlag(0);

            //不更新skuLastTime字段和zeroClearingTime字段
            lineEfficiencyStateLast.setSkuLastTime(null);

            //线效率状态记录
            if(Objects.nonNull(lineEfficiencyStateLast.getStartTime()) && Objects.nonNull(lineEfficiencyStateLast.getEndTime())){

                LineEfficiencyStateRecord lineEfficiencyStateRecord = new LineEfficiencyStateRecord();
                lineEfficiencyStateRecord.setLineCode(lineEfficiencyStateLast.getLineCode());
                lineEfficiencyStateRecord.setLineName(lineEfficiencyStateLast.getLineName());
                lineEfficiencyStateRecord.setSbId(lineEfficiencyStateLast.getSbId());
                lineEfficiencyStateRecord.setSbName(lineEfficiencyStateLast.getSbName());
                lineEfficiencyStateRecord.setStateCode(lineEfficiencyStateLast.getStateCode());
                lineEfficiencyStateRecord.setType(lineEfficiencyStateLast.getType());
                lineEfficiencyStateRecord.setStartTime(lineEfficiencyStateLast.getStartTime());
                lineEfficiencyStateRecord.setEndTime(lineEfficiencyStateLast.getEndTime());
                lineEfficiencyStateRecord.setRatedProduction(lineEfficiencyStateLast.getRatedProduction());

                lineEfficiencyStateRecord.setLineEfficiencyHandleList(lineEfficiencyStateLast.getLineEfficiencyHandleList());

                lineEfficiencyStateRecord.setDelFlag(0);

                lineEfficiencyStateRecordList.add(lineEfficiencyStateRecord);

                //更改线效状态实时表
                if(lineEfficiencyStateLast.getStateCode().equals(VariableUtil.LINE_EFFICIENCY_STATE_ZERO)){
                    lineEfficiencyStateLast.setStateCode(VariableUtil.LINE_EFFICIENCY_STATE_ONE);
                }else if(lineEfficiencyStateLast.getStateCode().equals(VariableUtil.LINE_EFFICIENCY_STATE_ONE)){
                    lineEfficiencyStateLast.setStateCode(VariableUtil.LINE_EFFICIENCY_STATE_ZERO);
                }

                //开始时间，需要在结束时间上+1秒
                Date newTimeAsDate = Date.from(
                        lineEfficiencyStateLast.getEndTime().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                                .plusSeconds(1)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                );
                lineEfficiencyStateLast.setStartTime(newTimeAsDate);
                //清空结束时间
                lineEfficiencyStateLast.setEndTime(null);
                lineEfficiencyStateLast.setLastTime(newTimeAsDate);
                //清空产量
                lineEfficiencyStateLast.setLastProduction(null);
            }

            //设备运行时长/报警
            if(null != lineEfficiencyStateLast.getListRecord() && lineEfficiencyStateLast.getListRecord().size() > 0){
                lineEfficiencyStateRecordList.addAll(lineEfficiencyStateLast.getListRecord());
            }
        }

        //计算新加的几个字段
        lineEfficiencyStateRecordList = calculatePiByHeadquarters(lineEfficiencyStateRecordList);

        //推送
        if(lineEfficiencyStateRecordList.size() > 0){
            lineEfficiencyStateRecordService.saveOrUpdateBatch(lineEfficiencyStateRecordList);

            //推送线效率
            //线效状态为运行状态才进行计算
            List<LineEfficiencyStateRecord> lineEfficiencyStateRecordListXxl  = lineEfficiencyStateRecordList.stream().filter(
                    lineEfficiencyStateRecord ->
                            lineEfficiencyStateRecord.getType() == 0 &&
                                    "1".equals(lineEfficiencyStateRecord.getStateCode())
            ).collect(Collectors.toList());
            if(null != lineEfficiencyStateRecordListXxl && lineEfficiencyStateRecordListXxl.size() > 0){
                countLineEfficiency(lineEfficiencyStateRecordList);
            }

            //设备运行时长推送
            List<LineEfficiencyStateRecord> lineEfficiencyStateRecordListSbyxsc  = lineEfficiencyStateRecordList.stream().filter(
                    lineEfficiencyStateRecord ->
                            lineEfficiencyStateRecord.getType() == 1
            ).collect(Collectors.toList());
            if(null != lineEfficiencyStateRecordListSbyxsc && lineEfficiencyStateRecordListSbyxsc.size() > 0){
                sendMachineMinute(lineEfficiencyStateRecordListSbyxsc);
            }

            //设备报警时长推送
            List<LineEfficiencyStateRecord> lineEfficiencyStateRecordListSbbjsc  = lineEfficiencyStateRecordList.stream().filter(
                    lineEfficiencyStateRecord ->
                            lineEfficiencyStateRecord.getType() == 2 &&
                                    "1".equals(lineEfficiencyStateRecord.getStateCode())
            ).collect(Collectors.toList());
            if(null != lineEfficiencyStateRecordListSbbjsc && lineEfficiencyStateRecordListSbbjsc.size() > 0){
                sendAlarmMinute(lineEfficiencyStateRecordListSbbjsc);
            }
        }
        lineEfficiencyStateLastService.saveOrUpdateBatch(lineEfficiencyStateLastList);

        //将线效状态写入到influxDB
        writeLineEfficiencyDataToInfluxDB(lineEfficiencyStateLastList);

        //实时线效率,杭州单独定制
        LineEfficiencyEhsFactory factoryVo = getFactoryCode();
        String factoryCode = factoryVo.getCode();
        if(factoryCode.equals("CR0122003195")){
            //计算实时线效率
            realTimeEfficiency(lineEfficiencyStateLastList);

            //能源计算
            productiveEnergy(lineEfficiencyStateRecordList);
        }

    }


    /**
     * 计算线效率
     * 计算公式：实际产量÷[(开机时间-关机时间)×（额定产量÷时间)]×100%
     * 注意：开机时间和关机时间不能以设备开关机信号点来进行判断
     * 开机时间：线效状态由0变成1的时间
     * 关机时间：线效状态由1变成0，且结束时间推最后一瓶，要加一个30秒进150瓶的判断，如果30秒内没进够150那么向前继续推，防止生产结束后误触光电传感器导致结束时间判断错误
     * 实际产量：在开机、关机时间段内的实际产量
     * 核定产量：配置。按线配置固定的额定产量
     * 时间：3600
     * @param lineEfficiencyStateRecordList 线效状态数据
     */
    public void countLineEfficiency(List<LineEfficiencyStateRecord> lineEfficiencyStateRecordList) {
        //限制数
        Double limits = 1.0;

        List<LineEfficiencyStateRecord> sendList = new ArrayList<>();

        for (LineEfficiencyStateRecord lineEfficiencyStateRecord : lineEfficiencyStateRecordList){

            if(lineEfficiencyStateRecord.getType() != 0 || !"1".equals(lineEfficiencyStateRecord.getStateCode())){
                continue;
            }

            //开机配置
            LineEfficiencyHandle lineEfficiencyHandle = new LineEfficiencyHandle();
            Optional<LineEfficiencyHandle> lineEfficiencyHandleOptional = lineEfficiencyStateRecord.getLineEfficiencyHandleList().stream().filter(e->e.getHandleType().equals(1)).findAny();
            if(lineEfficiencyHandleOptional.isPresent()){
                lineEfficiencyHandle = lineEfficiencyHandleOptional.get();
            }


            ProductionDto productionDto = new ProductionDto();
            productionDto.setProductionCode(lineEfficiencyHandle.getPointCode());
            Date firstDate = lineEfficiencyStateRecord.getEndTime();
            boolean  flag = true;
            while (flag){
                long millisAgoLong = firstDate.getTime() - 30 * 1000;
                Date startDateTime = new Date(millisAgoLong);

                //计算实际关机时间,使用系统默认时区
                LocalDateTime startLocalTime =startDateTime.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                LocalDateTime endLocalTime = firstDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                productionDto = getProductionByTime(startLocalTime,  endLocalTime,productionDto,limits);
                //根据返回的产量来判断是否达到了150瓶,或者时间范围内均不符合要求
                Double returnProduction = Objects.isNull(productionDto.getProduction()) ? 0 : productionDto.getProduction();
                if(returnProduction >= limits || startDateTime.before(lineEfficiencyStateRecord.getStartTime())){
                    flag = false;
                }else {
                    firstDate = startDateTime;
                }
            }
            //实际结束时间
            lineEfficiencyStateRecord.setEndTimeActual(firstDate);

            //计算实际关机时间,使用系统默认时区
            LocalDateTime startLocalTime =lineEfficiencyStateRecord.getStartTime().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            LocalDateTime endLocalTime = lineEfficiencyStateRecord.getEndTimeActual().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            //计算实际时长
            long seconds = Duration.between(startLocalTime, endLocalTime).getSeconds();

            lineEfficiencyStateRecord.setRunningTime(seconds);

            ProductionDto productionActualDto = new ProductionDto();
            productionActualDto.setProductionCode(lineEfficiencyHandle.getPointCode());
            //额定产量
            Double productionBase = lineEfficiencyStateRecord.getRatedProduction();
            //计算实际产量
            productionDto = getProductionByTime(startLocalTime,  endLocalTime,productionActualDto,null);
            Double productionActual = Objects.isNull(productionDto.getProduction()) ? 0.0 : productionDto.getProduction();

            lineEfficiencyStateRecord.setProduction(productionActual.toString());

            //计算公式 实际产量÷[(开机时间-关机时间)×（额定产量÷时间)]×100%
            double lineEfficiency = productionActual / (seconds * (productionBase / 3600.0)) * 100.0;
            lineEfficiencyStateRecord.setLineEfficiencyValue(String.format("%.4f", lineEfficiency));

            //初始化实际线效率
            lineEfficiencyStateRecord.setActualEfficiencyValue(String.format("%.4f", lineEfficiency));

            // 线效生产且 产量大于1000，运行时间大于300秒，则推送线效
            if(lineEfficiencyStateRecord.getType() == 0
                    && "1".equals(lineEfficiencyStateRecord.getStateCode())
                    && productionActual > 1000 && seconds > 300
                    && lineEfficiency > 20
            ){
                sendList.add(lineEfficiencyStateRecord);
            }
        }
        if(null != lineEfficiencyStateRecordList && lineEfficiencyStateRecordList.size() > 0){
            lineEfficiencyStateRecordService.saveOrUpdateBatch(lineEfficiencyStateRecordList);
        }
        //发送数据给第三方
        sendOee(sendList);

        //将线效率数据进行清零拆分
        //杭州单独进行拆分计算
        LineEfficiencyEhsFactory factoryVo = getFactoryCode();
        String factoryCode = factoryVo.getCode();
        if(factoryCode.equals("CR0122003195")){
            lineEfficiencySkuService.batchSplit(sendList);
        }
    }

    /**
     * 查询需要发送的数据
     */
    @Transactional(rollbackFor = Exception.class)
    public List<LineEfficiencyEhsRecords> getListRecords(){
        //开始转换成需要发送的数据
        List<LineEfficiencyEhsRecords> listRecords = new ArrayList<>();

        //查询需要发送的点位信息
        List<LineEfficiencyEhsPoints> listPoints = new ArrayList<>();
        LambdaQueryWrapper<LineEfficiencyEhsPoints> queryWrapper = Wrappers.lambdaQuery(LineEfficiencyEhsPoints.class);
        //查询点位不为空的记录
        queryWrapper.isNotNull(LineEfficiencyEhsPoints::getPointCode)
                .ne(LineEfficiencyEhsPoints::getPointCode, "").or().ne(LineEfficiencyEhsPoints::getPointCode, null);
        listPoints = lineEfficiencyEhsPointsService.list(queryWrapper);

        List<String> pointCodes = listPoints.stream().map(LineEfficiencyEhsPoints::getPointCode).collect(Collectors.toList());
        if(null == pointCodes || pointCodes.size() == 0){
            return listRecords;
        }

        LocalDateTime calculateTime = LocalDateTime.now();
        Date nowDate = new Date();
        // 定义日期格式化对象
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        // 将 Date 转换为指定格式的字符串
        String formattedDate = sdf.format(nowDate);

        System.out.println("pointCodes："+pointCodes);
        System.out.println("calculateTime："+calculateTime);

        List<MeasurementData> listMeasurementData = piComputationModelService.influxDbMoment(pointCodes, calculateTime, "down");

        System.out.println("查询结果："+listMeasurementData);

        if(null == listMeasurementData || listMeasurementData.size() == 0){
            return listRecords;
        }
        //查询当前工厂
        List<LineEfficiencyEhsFactory> listFactory = lineEfficiencyEhsFactoryServiceImpl.lambdaQuery().eq(LineEfficiencyEhsFactory::getStatus, "1").list();
        //工厂只能设置一个，多个工厂不处理
        if(null == listFactory || listFactory.size() != 1 ){
            return listRecords;
        }
        //当前工厂代码
        String factoryCode = listFactory.get(0).getCode();


        for(LineEfficiencyEhsPoints point : listPoints){
            LineEfficiencyEhsRecords record = new LineEfficiencyEhsRecords();

            String value = "";
            Optional<MeasurementData> dataOptional = listMeasurementData.stream().filter(data -> data.getCnlNum().equals(point.getPointCode())).findFirst();
            if(dataOptional.isPresent()){
                value = BigDecimal.valueOf(dataOptional.get().getValue()).setScale(4, RoundingMode.DOWN).toString();
                record.setMonitorDateTime(formattedDate);
                record.setMonitorValue(value);
                record.setOrgCode(factoryCode);
                record.setParamCode(point.getCode());
                record.setParamName(point.getName());
                record.setProcessMsg("1");
                record.setProcessStatus("0");
                record.setScadaInsertTime(formattedDate);
                record.setStatus("1");
                listRecords.add(record);
            }
        }
        lineEfficiencyEhsRecordsService.saveBatch(listRecords);
        return listRecords;
    }


    /**
     * 外排水推送程序
     */
    public void ehsSend(List<LineEfficiencyEhsRecords> listRecords) {
        //推送地址
        String httpsUrl = "";

        LineEfficiencyEhsFactory factory = getFactoryCode();
        if(StringUtils.isBlank(factory.getCode())){
            return;
        }
        //新都工厂特殊推送地址
        if(factory.getCode().equals("CR0121100014")){
            httpsUrl = "http://ecsb.crb.cn:8012/ecsb/gw/sys/rf?ssdp=QXBpX0lEPXNub3diZWVyLkVIUy5FSFMuc3dlYWdlTW9uaXRvciZBcGlfVmVyc2lvbj0xLjAmQXBwX1N1Yl9JRD0xMjAwMDAyNjAwMDImQXBwX1Rva2VuPTc1MjIxODRkZWVkNTQ5YTBhZjRhYTk4ZjBhZWFlMmQ3JkVOVj1wcmQmUGFydG5lcl9JRD0xMjAwMDAwMCZTaWduPU5PX1NJR04mU3lzX0lEPTEyMDAwMDI2JlRpbWVfU3RhbXA9MjAyMy0wOS0wNyAwOTo1Nzo0Mjo4MjImVXNlcl9Ub2tlbj0=";
        }else{
            httpsUrl = "https://ecsb.crb.cn/ecsb/gw/sys/rf?ssdp=QXBpX0lEPXNub3diZWVyLkVIUy5FSFMuc3dlYWdlTW9uaXRvciZBcGlfVmVyc2lvbj0xLjAmQXBwX1N1Yl9JRD0xMjAwMDAyNjAwMDImQXBwX1Rva2VuPTc1MjIxODRkZWVkNTQ5YTBhZjRhYTk4ZjBhZWFlMmQ3JkVOVj1wcmQmUGFydG5lcl9JRD0xMjAwMDAwMCZTaWduPU5PX1NJR04mU3lzX0lEPTEyMDAwMDI2JlRpbWVfU3RhbXA9MjAyMy0wOS0wNyAwOTo1Nzo0Mjo4MjImVXNlcl9Ub2tlbj0%3D";
        }

        List<EhsSendDto> listData = new ArrayList<>();

        for(LineEfficiencyEhsRecords record : listRecords){
            EhsSendDto ehsSendDto = new EhsSendDto();
            ehsSendDto.setCompanyId(record.getId());
            ehsSendDto.setCreateTime(record.getMonitorDateTime());
            ehsSendDto.setMonitorDateTime(record.getMonitorDateTime());
            ehsSendDto.setMonitorValue(record.getMonitorValue());
            ehsSendDto.setOrgCode(record.getOrgCode());
            ehsSendDto.setParamCode(record.getParamCode());
            ehsSendDto.setParamName(record.getParamName());
            ehsSendDto.setProcessMsg(record.getProcessMsg());
            ehsSendDto.setProcessStatus(record.getProcessStatus());
            ehsSendDto.setScadaInsertTime(record.getScadaInsertTime());
            ehsSendDto.setStatus(record.getStatus());
            ehsSendDto.setUpdateTime(record.getMonitorDateTime());

            record.setSendTime(record.getMonitorDateTime());
            listData.add(ehsSendDto);
        }
        String sendData = JSON.toJSONString(listData);

        try (CloseableHttpClient httpClient = createInsecureHttpClient()) {
            // 创建 HttpPost 请求
            HttpPost httpPost = new HttpPost(httpsUrl);
            // 设置请求头
            httpPost.setHeader("Content-Type", "application/json");
            // 构建请求体 建议指定字符编码
            StringEntity entity = new StringEntity(sendData, "UTF-8");
            httpPost.setEntity(entity);
            // 发送请求
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                String sendStatus = "";
                // 获取状态码
                int statusCode = response.getStatusLine().getStatusCode();
                sendStatus += statusCode + ";";
                // 获取响应体
                if (response.getEntity() != null) {
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    sendStatus += responseBody;
                } else {
                    sendStatus += "响应体为空";
                }
                for(LineEfficiencyEhsRecords record : listRecords){
                    record.setSendStatus(sendStatus);
                }
                lineEfficiencyEhsRecordsService.updateBatchById(listRecords);
            } finally {
                response.close();
                IOUtils.closeQuietly(httpClient);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 创建一个跳过 SSL 证书和主机名验证的 HttpClient
     * 仅用于测试环境！生产环境请勿使用！
     */
    public static CloseableHttpClient createInsecureHttpClient() {
        try {
            // 信任所有证书（包括自签名）
            // 信任所有
            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (chain, authType) -> true)
                    .build();

            // 使用自定义 SSL 工厂，跳过主机名验证
            // 跳过 hostname 验证（如 CN=192.168.100.254）
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    NoopHostnameVerifier.INSTANCE
            );

            return HttpClients.custom()
                    .setRedirectStrategy(new LaxRedirectStrategy())
                    .setSSLSocketFactory(socketFactory)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("创建不安全的 HttpClient 失败", e);
        }
    }

    /**
     * 设备运行时长推送
     * @param lineEfficiencyStateRecordList
     */
    public void sendMachineMinute(List<LineEfficiencyStateRecord> lineEfficiencyStateRecordList){
        List<EqutrunrecDto> listEqutrunrecDto = new ArrayList<>();
        for(LineEfficiencyStateRecord record : lineEfficiencyStateRecordList){
            //判断该点位是否推送
            long ifSend = record.getLineEfficiencyHandleList().stream().filter(item -> null != item.getIfSend() && item.getIfSend() == 1).count();
            if(ifSend == 0){
                continue;
            }

            if(record.getType() == 1 ){
                //设备运行时长超过24小时（86400秒）或者运行1秒钟的不推送
                if(record.getRunningTime() <= 86400 && record.getRunningTime() > 1){
                    int type = StringUtils.isBlank(record.getStateCode()) ? 0 : Integer.parseInt(record.getStateCode());

                    EqutrunrecDto equtrunrecDto = new EqutrunrecDto();
                    equtrunrecDto.setStatus(0);
                    equtrunrecDto.setOrgId(record.getLineCode());
                    equtrunrecDto.setOrgName(record.getLineName());
                    equtrunrecDto.setEqutID(record.getSbId());
                    equtrunrecDto.setEqutName(record.getSbName());
                    equtrunrecDto.setType(type);
                    equtrunrecDto.setBegTime(record.getStartTime());
                    equtrunrecDto.setEndTime(record.getEndTime());
                    equtrunrecDto.setRunTime(record.getRunningTime());
                    listEqutrunrecDto.add(equtrunrecDto);
                }
            }
        }
        //大数据量分批写入
        int BATCH_SIZE = 500;
        for (int i = 0; i < listEqutrunrecDto.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, listEqutrunrecDto.size());
            List<EqutrunrecDto> subList = listEqutrunrecDto.subList(i, end);
            handleMapper.insertEqutrunrecBatch(subList);
        }
    }

    /**
     * 报警时长推送
     * @param lineEfficiencyStateRecordList
     */
    public void sendAlarmMinute(List<LineEfficiencyStateRecord> lineEfficiencyStateRecordList){
        List<EqutalarmrecDto> listEqutalarmrecDto = new ArrayList<>();
        for(LineEfficiencyStateRecord record : lineEfficiencyStateRecordList){
            //判断该点位是否推送
            long ifSend = record.getLineEfficiencyHandleList().stream().filter(item -> null != item.getIfSend() && item.getIfSend() == 1).count();
            if(ifSend == 0){
                continue;
            }

            //报警点位，且为报警状态才推送
            if(record.getType() == 2 && "1".equals(record.getStateCode())){
                EqutalarmrecDto equtalarmrecDto = new EqutalarmrecDto();
                equtalarmrecDto.setStatus(0);
                equtalarmrecDto.setOrgId(record.getLineCode());
                equtalarmrecDto.setEqutId(record.getSbId());
                equtalarmrecDto.setEqutName(record.getSbName());
                equtalarmrecDto.setAlarmName(record.getAlarmName());
                equtalarmrecDto.setAlarmBegTime(record.getStartTime());
                equtalarmrecDto.setAlarmEndTime(record.getEndTime());
                equtalarmrecDto.setAlarmRunTime(record.getRunningTime());
                equtalarmrecDto.setAlarmtype(1);

                listEqutalarmrecDto.add(equtalarmrecDto);
            }
        }
        //大数据量分批写入
        int BATCH_SIZE = 500;
        for (int i = 0; i < listEqutalarmrecDto.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, listEqutalarmrecDto.size());
            List<EqutalarmrecDto> subList = listEqutalarmrecDto.subList(i, end);
            handleMapper.insertalarmMinuteBatch(subList);
        }
    }

    /**
     * 推送OEE-线效率
     * @param lineEfficiencyStateRecordList
     */
    public void sendOee(List<LineEfficiencyStateRecord> lineEfficiencyStateRecordList){
        List<EqutoeerecDto> listEqutoeerecDtoDto = new ArrayList<>();
        for(LineEfficiencyStateRecord record : lineEfficiencyStateRecordList){
            //判断该点位是否推送
            long ifSend = record.getLineEfficiencyHandleList().stream().filter(item -> null != item.getIfSend() && item.getIfSend() == 1).count();
            if(ifSend == 0){
                continue;
            }

            //报警点位，且为报警状态才推送
            if(record.getType() == 0 && "1".equals(record.getStateCode())){
                Integer production = StringUtils.isBlank(record.getProduction()) ? 0 : Double.valueOf(record.getProduction()).intValue();
                String runningTime = Objects.isNull(record.getRunningTime()) ? "0" : record.getRunningTime()+"";
                EqutoeerecDto equtoeerecDto = new EqutoeerecDto();
                equtoeerecDto.setStatus(0);
                equtoeerecDto.setOrgId(record.getLineCode());
                equtoeerecDto.setLineBTime(record.getStartTime());
                equtoeerecDto.setLineETime(record.getEndTimeActual());
                equtoeerecDto.setBBBottles(production);
                equtoeerecDto.setActRunLength(runningTime);
                equtoeerecDto.setOEE(record.getLineEfficiencyValue());
                equtoeerecDto.setLineCap(record.getRatedProduction());

                listEqutoeerecDtoDto.add(equtoeerecDto);
            }
        }
        //大数据量分批写入
        int BATCH_SIZE = 500;
        for (int i = 0; i < listEqutoeerecDtoDto.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, listEqutoeerecDtoDto.size());
            List<EqutoeerecDto> subList = listEqutoeerecDtoDto.subList(i, end);
            handleMapper.insertOeeBatch(subList);
        }
    }

    /**
     * 生成每一天的OEE数据（早晨8点到第二天早晨8点为1天）--废除
     * @param lineEfficiencyStateRecordList
     */
    public void generateOeeByDay(List<LineEfficiencyStateRecord> lineEfficiencyStateRecordList){
        List<LineEfficiencyStateRecordDay> listDay = new ArrayList<>();
        //只拆分线效状态为1的数据
        lineEfficiencyStateRecordList = lineEfficiencyStateRecordList.stream().filter(item -> item.getType() == 0 && "1".equals(item.getStateCode())).collect(Collectors.toList());
        //将总线效率数据按天拆分
        for(LineEfficiencyStateRecord record : lineEfficiencyStateRecordList){

            // 将Date转换为LocalDateTime
            LocalDateTime start = record.getStartTime().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            LocalDateTime end = record.getEndTimeActual().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            String dayDate = "";
            Date startDate = null;
            Date endDate = null;

            while(start.isBefore(end)){
                LineEfficiencyStateRecordDay dayLine = new LineEfficiencyStateRecordDay();

                dayLine.setLineCode(record.getLineCode());
                dayLine.setLineName(record.getLineName());
                dayLine.setSbId(record.getSbId());
                dayLine.setSbName(record.getSbName());
                dayLine.setStateCode(record.getStateCode());
                dayLine.setType(record.getType());
                dayLine.setRatedProduction(record.getRatedProduction());

                //如果dayDate为空，则是第一次进入
                if(StringUtils.isBlank(dayDate)){
                    //判断开始时间在当天的08点前还是08点后，08点前，日期算头一天，08点后，日期算当天
                    LocalDateTime startAt8AM = start.withHour(8).withMinute(0).withSecond(0);
                    startDate = Date.from(start.atZone(ZoneId.systemDefault()).toInstant());
                    if (start.toLocalTime().isBefore(startAt8AM.toLocalTime())) {
                        endDate = Date.from(startAt8AM.atZone(ZoneId.systemDefault()).toInstant());
                        dayDate = start.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    } else {
                        endDate = Date.from(startAt8AM.plusDays(1).atZone(ZoneId.systemDefault()).toInstant());
                        dayDate = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    }
                }else{
                    dayDate = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    startDate = Date.from(start.atZone(ZoneId.systemDefault()).toInstant());
                    endDate = Date.from(start.plusDays(1).atZone(ZoneId.systemDefault()).toInstant());
                }
                start = endDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                if(start.isAfter(end)){
                    endDate = Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
                }

                //继续赋值
                dayLine.setDayDate(dayDate);
                dayLine.setStartTime(startDate);
                dayLine.setEndTime(endDate);
                dayLine.setEndTimeActual(endDate);

                long seconds = Duration.between(startDate.toInstant(), endDate.toInstant()).getSeconds();
                dayLine.setRunningTime(seconds);

                ProductionDto pointDto = new ProductionDto();
                pointDto.setProductionCode(record.getLineEfficiencyHandleList().get(0).getPointCode());
                //计算产量
                pointDto = getProductionByTime(
                        startDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime(),
                        endDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime(),
                        pointDto,null);

                Double productionActual = Objects.isNull(pointDto.getProduction()) ? 0.0 : pointDto.getProduction();

                dayLine.setProduction(pointDto.getProduction()+"");

                //计算公式 实际产量÷[(开机时间-关机时间)×（额定产量÷时间)]×100%
                double lineEfficiency = productionActual / (seconds * (record.getRatedProduction() / 3600.0)) * 100.0;
                dayLine.setLineEfficiencyValue(String.format("%.4f", lineEfficiency));

                listDay.add(dayLine);
            }
        }
        if(null != listDay && listDay.size() > 0){
            lineEfficiencyStateRecordDayService.saveOrUpdateBatch(listDay);
        }
    }

    /**
     * 将线效状态写入到influxDB数据库中
     */
    public void writeLineEfficiencyDataToInfluxDB(List<LineEfficiencyStateLast> lineEfficiencyStateLastList){
        //只查看线效数据
        lineEfficiencyStateLastList = lineEfficiencyStateLastList.stream().filter(item -> item.getType() == 0).collect(Collectors.toList());

        if(null == lineEfficiencyStateLastList || lineEfficiencyStateLastList.size() == 0){
            return ;
        }
        //点位数据集合
        List<Point> pointList = new ArrayList<>();

        // 直接从lineEfficiencyStateLastList中提取所有lineEfficiencyPointCode并转换为List
        List<String> lineEfficiencyPointCodeList = lineEfficiencyStateLastList.stream()
                .filter(Objects::nonNull)
                .map(LineEfficiencyStateLast::getLineEfficiencyHandleList)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .map(LineEfficiencyHandle::getLineEfficiencyPointCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        //获取所有线效点位到目前位置，最新的一条记录
        List<MeasurementData> lineEfficiencyPointCodeData = piComputationModelService.influxDbMoment(lineEfficiencyPointCodeList,LocalDateTime.now(),"down");

        for (LineEfficiencyStateLast lineEfficiencyStateLast : lineEfficiencyStateLastList){
            //线效点位
            String lineEfficiencyPointCode = "";
            if(null != lineEfficiencyStateLast.getLineEfficiencyHandleList() && lineEfficiencyStateLast.getLineEfficiencyHandleList().size() > 0){
                lineEfficiencyPointCode = lineEfficiencyStateLast.getLineEfficiencyHandleList().get(0).getLineEfficiencyPointCode();
            }
            if(StringUtils.isBlank(lineEfficiencyPointCode)){
                continue;
            }

            //获取最新时间
            Instant latestInstant = null;

            String finalLineEfficiencyPointCode = lineEfficiencyPointCode;
            Optional<MeasurementData> latestMeasurementData = lineEfficiencyPointCodeData.stream()
                    .filter(data -> data.getCnlNum().equals(finalLineEfficiencyPointCode))
                    .max((o1, o2) -> o2.getTime().compareTo(o1.getTime()));

            if (latestMeasurementData.isPresent()) {
                latestInstant = latestMeasurementData.get().getTime();
            }
            Double stateCode = StringUtils.isBlank(lineEfficiencyStateLast.getStateCode())?0.0:Double.parseDouble(lineEfficiencyStateLast.getStateCode());
            if(null == latestInstant){
                latestInstant = lineEfficiencyStateLast.getLastTime().toInstant();
                Point point = Point.measurement(influxDBClientHelper.tagTable)
                        .time(latestInstant, WritePrecision.MS)
                        .addField(influxDBClientHelper.field, stateCode)
                        .addTag(influxDBClientHelper.tag, lineEfficiencyPointCode);
                pointList.add(point);
            }else{
                //如果influxDB中存在，查询最近一个线效点位的值是0还是1，跟现在的点位进行逻辑判断，如果不一致，则写入到startTime，一致，则写入到lastTime
                Double oldStateCode =latestMeasurementData.get().getValue();
                Double newStateCode = stateCode;
                Double insertStateCode = null;

                while (latestInstant.isBefore(lineEfficiencyStateLast.getLastTime().toInstant())){
                    latestInstant = latestInstant.plusSeconds(60);

                    if(!oldStateCode.equals(newStateCode) && latestInstant.isBefore(lineEfficiencyStateLast.getStartTime().toInstant())){
                        insertStateCode = oldStateCode;
                    }else{
                        insertStateCode = newStateCode;
                    }
                    Point point = Point.measurement(influxDBClientHelper.tagTable)
                            .time(latestInstant, WritePrecision.MS)
                            .addField(influxDBClientHelper.field, insertStateCode)
                            .addTag(influxDBClientHelper.tag, lineEfficiencyPointCode);
                    pointList.add(point);
                }
            }
        }
        if(null != pointList && pointList.size() > 0){
            influxDBClient.makeWriteApi().writePoints(pointList);
        }
    }



    /**
     * 找下一个30分钟的整数点
     * @param startLocalDateTime
     * @return
     */
    public LocalDateTime getNext30MinutePoint(LocalDateTime startLocalDateTime) {
        // 获取分钟数
        int minute = startLocalDateTime.getMinute();
        // 计算下一个30分钟的整数点
        LocalDateTime next30MinutePoint;
        if (minute < 30) {
            // 如果分钟数小于30，则下一个整点是当前小时的30分
            next30MinutePoint = startLocalDateTime.withMinute(30).withSecond(0).withNano(0);
        } else {
            // 如果分钟数大于等于30，则下一个整点是下一小时的0分
            next30MinutePoint = startLocalDateTime.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        }

        return next30MinutePoint;
    }

    /**
     * 获取外排水-工厂代码
     * @return
     */
    public LineEfficiencyEhsFactory getFactoryCode() {
        if(null == factoryEntity || StringUtils.isBlank(factoryEntity.getId())){
            List<LineEfficiencyEhsFactory> listFactory = lineEfficiencyEhsFactoryServiceImpl.lambdaQuery().eq(LineEfficiencyEhsFactory::getStatus, "1").list();
            if(null != listFactory && listFactory.size() == 1){
                factoryEntity = listFactory.get(0);
            }
        }
        return factoryEntity;
    }

    /**
     * 传入比较类型和值，返回是否成立
     * @param type 比较类型
     * @param actualValue 实际值
     * @param setValue 设定值
     * @return
     */
    public boolean isInclude(String type, double actualValue,double setValue) {
        boolean ret = false;
        if("1".equals(type)){
            if(actualValue > setValue){
                ret = true;
            }
        }
        if("2".equals(type)){
            if(actualValue >= setValue){
                ret = true;
            }
        }
        if("3".equals(type)){
            if(actualValue == setValue){
                ret = true;
            }
        }
        if("4".equals(type)){
            if(actualValue != setValue){
                ret = true;
            }
        }
        if("5".equals(type)){
            if(actualValue < setValue){
                ret = true;
            }
        }
        if("6".equals(type)){
            if(actualValue <= setValue){
                ret = true;
            }
        }
        return ret;
    }

    /**
     * 计算实时线效率
     * @param lineEfficiencyStateLastList
     */
    public void realTimeEfficiency(List<LineEfficiencyStateLast> lineEfficiencyStateLastList){
        //筛选出状态为0的线效率状态
        lineEfficiencyStateLastList = lineEfficiencyStateLastList.stream().filter(x->
                null  != x.getType() && x.getType() == 0
        ).collect(Collectors.toList());

        //需要重新更新的线效率状态
        List<LineEfficiencyStateLast> lineEfficiencyStateLasts = new ArrayList<>();

        //所有的历史记录
        List<LineEfficiencyStateRecord> listStateRecord = new ArrayList<>();

        if(null == lineEfficiencyStateLastList || lineEfficiencyStateLastList.size() == 0){
            return;
        }

        for (LineEfficiencyStateLast stateLast : lineEfficiencyStateLastList) {
            Date startTime = null != stateLast.getZeroClearingTime() ? stateLast.getZeroClearingTime() : stateLast.getStartTime();
            Date endTime = stateLast.getLastTime();

            //当前线效率状态为运行状态或者为非运行状态且运行开始时间等于最新更新时间,则进行计算（非运行状态，开始时间等于结束时间的时候，证明是刚好是从1变成0的时候）
            //处理刚好从0变成1或者从1变成0的时候，这种时候直接写入一条实时线效率为0的数据即可
            if(
                    stateLast.getStateCode().equals("0") || ( stateLast.getStateCode().equals("1") && stateLast.getStartTime().equals(stateLast.getLastTime()))
            ){
                LineEfficiencyStateRecord stateRecord = new LineEfficiencyStateRecord();
                stateRecord.setLineCode(stateLast.getLineCode());
                stateRecord.setLineName(stateLast.getLineName());
                stateRecord.setSbId(stateLast.getSbId());
                stateRecord.setSbName(stateLast.getSbName());
                stateRecord.setStateCode("0");
                stateRecord.setStartTime(stateLast.getStartTime());
                stateRecord.setEndTime(stateLast.getLastTime());

                // 计算时间差（秒）
                long seconds = Duration.between(
                        stateLast.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                        stateLast.getLastTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                ).getSeconds();

                stateRecord.setRunningTime( seconds);
                //3代表实时线效
                stateRecord.setType(3);
                stateRecord.setLineEfficiencyValue("0");

                listStateRecord.add(stateRecord);

                //从不运行到运行切换，后续步骤不需要执行
                if(
                        stateLast.getStateCode().equals("1") || (stateLast.getStateCode().equals("0") && !stateLast.getStartTime().equals(stateLast.getLastTime()))
                ){
                    stateLast.setZeroClearingTime(stateLast.getStartTime());
                    lineEfficiencyStateLasts.add(stateLast);
                    continue;
                }
            }

            //判断开始时间和结束时间之间是否有清零的情况
            //判断产量清零的时间段
            LocalDateTime startTimeLocal = startTime.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            LocalDateTime endTimeLocal = endTime.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            //20260115 需求调整，不按照产量清零而清零，改为按照批次清零
            List<Pair<LocalDateTime,LocalDateTime>> timePeriods = new ArrayList<>();
            timePeriods.add(Pair.of(startTimeLocal, endTimeLocal));
//            //清零时间段的开始时间和结束时间
//            List<Pair<LocalDateTime,LocalDateTime>> timePeriods = new ArrayList<>();
//            LocalDateTime onInstant = startTimeLocal;
//
//            LocalDateTime offTime = null;

            //产量code
            String pointCode = stateLast.getLineEfficiencyHandleList().stream().filter(lineEfficiencyHandle -> lineEfficiencyHandle.getHandleType() == 1).findAny().get().getPointCode();
//
//            if(startTimeLocal.isBefore(endTimeLocal)){
//                List<MeasurementData> listPointData = piComputationModelService.getMeasurementDataList(startTimeLocal,endTimeLocal,Arrays.asList(pointCode),1);
//                //筛选出为0的
//                List<MeasurementData> pointCodeListData = listPointData.stream().filter(measurementData -> measurementData.getValue() == 0).collect(Collectors.toList());
//
//                for(MeasurementData measurementData : pointCodeListData){
//                    offTime = LocalDateTime.ofInstant(measurementData.getTime(), ZoneId.systemDefault());
//                    Duration duration = Duration.between(onInstant, offTime);
//                    //连续等于0的点排除掉
//                    if(duration.getSeconds() >1){
//                        timePeriods.add(Pair.of(onInstant, offTime));
//                    }
//                    onInstant = offTime;
//                }
//            }
            //最后一段
//            timePeriods.add(Pair.of(onInstant, endTimeLocal));

            //查询线效率的SKU配置
            List<LineEfficiencySku> listSku = lineEfficiencySkuService.lambdaQuery()
                    .eq(LineEfficiencySku::getLineCode, stateLast.getLineCode())
                    .eq(LineEfficiencySku::getType, 1).list();

            //获取sku点位信息，任意取一条即可
            if(null == listSku || listSku.size() == 0){
                return;
            }
            String skuPointCode = listSku.get(0).getPointCode();

            //根据清零的时间段，获取SKU的运行数据
            for (int i = 0; i < timePeriods.size(); i++) {

                Pair<LocalDateTime,LocalDateTime> pair = timePeriods.get(i);

                // 转换开始时间
                Date startDate = Date.from(pair.getKey().atZone(ZoneId.systemDefault()).toInstant());
                Date endDate = Date.from(pair.getValue().atZone(ZoneId.systemDefault()).toInstant());

                //时间区间大于1，则说明有清零的情况，则需要将实时线效进行清零处理
                if(i > 0){
                    LineEfficiencyStateRecord stateRecord = new LineEfficiencyStateRecord();
                    stateRecord.setLineCode(stateLast.getLineCode());
                    stateRecord.setLineName(stateLast.getLineName());
                    stateRecord.setSbId(stateLast.getSbId());
                    stateRecord.setSbName(stateLast.getSbName());
                    stateRecord.setStateCode("0");
                    stateRecord.setStartTime(startDate);
                    stateRecord.setEndTime(startDate);

                    stateRecord.setRunningTime(0L);
                    //2代表实时线效
                    stateRecord.setType(3);
                    stateRecord.setLineEfficiencyValue("0");

                    listStateRecord.add(stateRecord);
                    //更新零清零时间
                    stateLast.setZeroClearingTime(startDate);
                }
                //实际产量
                double actualOutput = 0.0;
                //额定产量
                double ratedOutput = 0.0;

                //判断时间段内有多少SKU,如果SKU为空，则默认按照配置标准计算
                List<PointDateDto> dateDtoList = new ArrayList<>();
                if(StringUtils.isBlank(skuPointCode)){
                    if(Objects.isNull(listSku.get(0).getBottleShapeValue())){
                        return;
                    }
                    PointDateDto pointDateDto = new PointDateDto();
                    pointDateDto.setStartInstant(startDate.toInstant());
                    pointDateDto.setEndInstant(endDate.toInstant());
                    pointDateDto.setSkuValue(listSku.get(0).getBottleShapeValue());
                    long secondsBetween = Duration.between(startDate.toInstant(), endDate.toInstant()).getSeconds();
                    pointDateDto.setRunningTime(Double.valueOf(secondsBetween));
                    dateDtoList.add(pointDateDto);
                }else {
                    dateDtoList = lineEfficiencySkuService.getTimeRangeByPointCode(skuPointCode,pair.getKey(),pair.getValue());
                }

                for(PointDateDto data : dateDtoList){
                    //计算核定产量
                    //获取对应sku的信息
                    LineEfficiencySku lineEfficiencySku = new LineEfficiencySku();
                    if(listSku.size() > 1){
                        lineEfficiencySku = listSku.stream().filter(x->StringUtils.isNoneBlank(x.getSkuCode()) && Double.parseDouble(x.getSkuCode()) == data.getSkuValue()).findFirst().orElse(null);
                    }else{
                        if(StringUtils.isBlank(listSku.get(0).getPointCode()) && StringUtils.isBlank(listSku.get(0).getSkuCode())){
                            lineEfficiencySku = listSku.get(0);
                        }else {
                            lineEfficiencySku = listSku.stream().filter(x->StringUtils.isNoneBlank(x.getSkuCode()) && Double.parseDouble(x.getSkuCode()) == data.getSkuValue()).findFirst().orElse(null);
                        }
                    }
                    if(null == lineEfficiencySku){
                        continue;
                    }

                    double skuSpeed = null == lineEfficiencySku.getSkuSpeed() ? 0.0 : lineEfficiencySku.getSkuSpeed();

                    double runningTime = null == data.getRunningTime() ? 0.0 : data.getRunningTime();

                    ratedOutput += skuSpeed * runningTime / 3600.0;
                    //计算实际产量
                    Double output = lineEfficiencySkuService.queryDataProduction(pointCode,data.getStartInstant().getEpochSecond(),data.getEndInstant().getEpochSecond());
                    actualOutput += output;
                }

                LineEfficiencyStateRecord stateRecord = new LineEfficiencyStateRecord();
                stateRecord.setLineCode(stateLast.getLineCode());
                stateRecord.setLineName(stateLast.getLineName());
                stateRecord.setSbId(stateLast.getSbId());
                stateRecord.setSbName(stateLast.getSbName());

                stateRecord.setStateCode("1");
                stateRecord.setStartTime(startDate);
                stateRecord.setEndTime(endDate);

                // 计算时间差（秒）
                long seconds = Duration.between(
                        pair.getKey(),
                        pair.getValue()
                ).getSeconds();

                stateRecord.setRunningTime(seconds);
                //3代表实时线效
                stateRecord.setType(3);

                //计算公式 实际产量÷[(开机时间-关机时间)×（额定产量÷时间)]×100%
                double lineEfficiency = actualOutput / (ratedOutput * 1.0) * 100.0;
                stateRecord.setLineEfficiencyValue(String.format("%.4f", lineEfficiency));

                listStateRecord.add(stateRecord);
            }

            if(timePeriods.size() > 1){
                lineEfficiencyStateLasts.add(stateLast);
            }
        }

        //保存实时线效记录
        lineEfficiencyStateRecordService.saveOrUpdateBatch(listStateRecord);
        //更新实时状态表,只更新了清零时间
        if(null != lineEfficiencyStateLasts && lineEfficiencyStateLasts.size() > 0){
            // 1. 转换数据：只保留 id 和需要更新的字段，其他字段设为 null
//            List<LineEfficiencyStateLast> updateList = lineEfficiencyStateLasts.stream()
//                    .map(x -> {
//                        LineEfficiencyStateLast temp = new LineEfficiencyStateLast();
//                        temp.setId(x.getId());
//                        temp.setZeroClearingTime(x.getZeroClearingTime());
//                        // 其他字段不要 set，保持 null
//                        return temp;
//                    })
//                    .collect(Collectors.toList());
//
//            // 2. 执行批量更新
//            // 前提：确保全局配置 update-strategy 为 not_null (默认即为 not_null)
//            lineEfficiencyStateLastService.updateBatchById(updateList);
            // 批量更新 zeroClearingTime 字段
            for (LineEfficiencyStateLast stateLast : lineEfficiencyStateLasts) {
                LambdaUpdateWrapper<LineEfficiencyStateLast> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(LineEfficiencyStateLast::getId, stateLast.getId())
                        .set(LineEfficiencyStateLast::getZeroClearingTime, stateLast.getZeroClearingTime());
                lineEfficiencyStateLastService.update(updateWrapper);
            }
        }

    }

    /**
     * 计算能源耗量
     * @param recordList 传入批次数据
     */
    public void  productiveEnergy(List<LineEfficiencyStateRecord> recordList){
        if(null == recordList || recordList.size() == 0){
            return;
        }

        //查询所有能源的配置信息
        List<LineEfficiencySku> listSku = lineEfficiencySkuService.lambdaQuery().eq(LineEfficiencySku::getType,7).list();

        for (LineEfficiencyStateRecord stateLast : recordList) {

            //产线点位
//            String productionCode = stateLast.getLineEfficiencyHandleList().stream().filter(x->x.getHandleType() == 1).findAny().get().getPointCode();
            String productionCode = stateLast.getLineEfficiencyHandleList().stream()
                    .filter(x -> x.getHandleType() == 1)
                    .findFirst()
                    .map(LineEfficiencyHandle::getPointCode)
                    .orElse("");

            //SKU配置信息
            List<LineEfficiencySku> skuList = lineEfficiencySkuService.lambdaQuery()
                    .eq(LineEfficiencySku::getLineCode,stateLast.getLineCode())
                    .eq(LineEfficiencySku::getType,1)
                    .list();

            //获取能源配置信息
            List<LineEfficiencySku> pointList = listSku.stream().filter(x->
                    StringUtils.isNotBlank(x.getLineCode()) && x.getLineCode().equals(stateLast.getLineCode())
            ).collect(Collectors.toList());

            Date startDate = null;

            Date endDate = null;

            LocalDateTime startLocalDateTime = null;

            LocalDateTime endLocalDateTime = null;

            //批次产量瓶数
            Double production = StringUtils.isNotBlank(stateLast.getProduction()) ? Double.parseDouble(stateLast.getProduction()):0.0;

            //批次ML数
            Double bottleMl = 0.0;

            if ("0".equals(stateLast.getStateCode())){
                //查询非生产时间的实际开始时间，上一批次的实际结束时间
                LineEfficiencyStateRecord record = lineEfficiencyStateRecordService.lambdaQuery()
                        .eq(LineEfficiencyStateRecord::getLineCode,stateLast.getLineCode())
                        .eq(LineEfficiencyStateRecord::getStateCode,"1")
                        .lt(LineEfficiencyStateRecord::getEndTime,stateLast.getStartTime())
                        .orderByDesc(LineEfficiencyStateRecord::getEndTime)
                        .last("limit 1")
                        .one();
                if(null == record){
                    continue;
                }
                startDate = record.getEndTimeActual();
                if(null == startDate){
                    continue;
                }
                //获取实际的开始时间
                startLocalDateTime = startDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                endDate = stateLast.getEndTime();
                if(null == endDate){
                    continue;
                }
                endLocalDateTime = endDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
            }else{
                startDate = stateLast.getStartTime();
                if(null == startDate){
                    continue;
                }
                startLocalDateTime = startDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                endDate = stateLast.getEndTimeActual();
                if(null == endDate){
                    continue;
                }
                endLocalDateTime = endDate.toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                //计算KL数
                if (null == skuList || skuList.size() == 0){
                    continue;
                }

                if(skuList.size() == 1 && StringUtils.isBlank(skuList.get(0).getSkuCode())){
                    Double bottleShape = null != skuList.get(0).getBottleShapeValue() ? skuList.get(0).getBottleShapeValue() : 0.0;
                    bottleMl = production * bottleShape;
                }else{
                    //根据sku配置信息，获取时间段内kl数
                    String skuCode = skuList.get(0).getPointCode();

                    List<PointDateDto> listDto = lineEfficiencySkuService.getTimeRangeByPointCode(skuCode, startLocalDateTime, endLocalDateTime);
                    for (PointDateDto dto : listDto){
                        //换算成KL
                        //查询时间段内的产量
                        LineEfficiencySku lineEfficiencySku = skuList.stream().filter(x->StringUtils.isNotBlank(x.getSkuCode()) && Double.parseDouble(x.getSkuCode()) == dto.getSkuValue()).findAny().orElse(new LineEfficiencySku());
                        Double bottleShape = null != lineEfficiencySku.getBottleShapeValue() ? lineEfficiencySku.getBottleShapeValue() : 0.0;

                        if(bottleShape > 0){
                            Double productionTemp = lineEfficiencySkuService.queryDataProduction(productionCode,dto.getStartInstant().getEpochSecond(),dto.getEndInstant().getEpochSecond());
                            production = Objects.isNull(production) ? 0.0 : production;
                            bottleMl += productionTemp * bottleShape;
                        }
                    }
                }
            }

            //计算能源在时间段内的能耗情况
            List<LineEfficiencyStateRecord> recordsEnergy = new ArrayList<>();
            for (LineEfficiencySku point : pointList){
                ProductionDto pointDto = new ProductionDto();
                pointDto.setProductionCode(point.getPointCode());
                pointDto = getProductionByTime(startLocalDateTime, endLocalDateTime, pointDto,null);

                Double productionActual = Objects.isNull(pointDto.getProduction()) ? 0.0 : pointDto.getProduction();

                LineEfficiencyStateRecord stateRecord = new LineEfficiencyStateRecord();
                stateRecord.setLineCode(stateLast.getLineCode());
                stateRecord.setLineName(stateLast.getLineName());
                stateRecord.setSbId(point.getPointCode());
                stateRecord.setSbName(point.getBottleShape());
                stateRecord.setStateCode(stateLast.getStateCode());
                stateRecord.setStartTime(startDate);
                stateRecord.setEndTime(endDate);
                stateRecord.setRunningTime(Duration.between(startLocalDateTime,endLocalDateTime).getSeconds());
                stateRecord.setProduction(stateLast.getProduction());

                stateRecord.setEnergyConsumption(productionActual);

                if("0".equals(stateLast.getStateCode())){
                    stateRecord.setType(4);
                }else{
                    stateRecord.setType(5);

                    //瓶耗
                    Double bottleConsumption = 0.0;
                    //酒单耗
                    Double wineConsumption = 0.0;

                    if(null != productionActual && productionActual > 0){
                        //瓶单耗，瓶数/能耗
                        bottleConsumption = production / productionActual;

                        wineConsumption = bottleMl / 1000000.0 / productionActual;
                    }
                    //瓶单耗
                    stateRecord.setBottleConsumption(bottleConsumption);
                    //酒单耗
                    stateRecord.setWineConsumption(wineConsumption);

                    stateRecord.setProductionKl(bottleMl / 1000000.0);
                }
                recordsEnergy.add(stateRecord);
            }
            lineEfficiencyStateRecordService.saveOrUpdateBatch(recordsEnergy);
        }
    }

    /**
     * 按照总部的要求，新增入库产量(KL数)、入库产量(瓶/听数)、生产类型、生产周期、首台设备开机时间、末台设备开机时间的计算，只针对线效率
     * @param lineEfficiencyStateRecordList
     * @return
     */
    public List<LineEfficiencyStateRecord> calculatePiByHeadquarters(List<LineEfficiencyStateRecord> lineEfficiencyStateRecordList) {
        if (null == lineEfficiencyStateRecordList || lineEfficiencyStateRecordList.size() == 0){
            return lineEfficiencyStateRecordList;
        }

        //查询配置信息,查询码垛机的产量和sku配置
        List<LineEfficiencySku> listSku = lineEfficiencySkuService.lambdaQuery().in(LineEfficiencySku::getType,Arrays.asList(5,6,8)).list();

        for (LineEfficiencyStateRecord lineEfficiencyStateRecord : lineEfficiencyStateRecordList){
            //只计算线效，并且为运行状态的数据
            if(lineEfficiencyStateRecord.getType() == 0 && "1".equals(lineEfficiencyStateRecord.getStateCode())){
                //产线
                String lineCode = lineEfficiencyStateRecord.getLineCode();
                //码垛机产量配置
                List<LineEfficiencySku> skuListProductionNum = listSku.stream().filter(x->x.getLineCode().equals(lineCode) && x.getType() == 5).collect(Collectors.toList());
                //码垛机sku配置
                List<LineEfficiencySku> skuListProductionCapacity = listSku.stream().filter(x->x.getLineCode().equals(lineCode) && x.getType() == 6).collect(Collectors.toList());
                if (null == skuListProductionNum || skuListProductionNum.size() == 0 || null == skuListProductionCapacity || skuListProductionCapacity.size() == 0){
                    continue;
                }

                //特殊情况，时间为空跳过
                if(null == lineEfficiencyStateRecord.getStartTime() || null == lineEfficiencyStateRecord.getEndTimeActual()){
                    continue;
                }

                //开始时间
                LocalDateTime startTime = lineEfficiencyStateRecord.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                //结束时间
                LocalDateTime endTime = lineEfficiencyStateRecord.getEndTimeActual().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                //sku点位
                String skuPointCode = skuListProductionCapacity.get(0).getPointCode();
                //获取每个sku的持续时长
                List<PointDateDto> dateDtoList = lineEfficiencySkuService.getTimeRangeByPointCode(skuPointCode,startTime,endTime);
                //查询每个时间区间内的码垛机产量
                String productionPointCode = skuListProductionNum.get(0).getPointCode();
                //码垛机产量
                double productionNum = 0;
                //码垛机容量
                double productionCapacity = 0;

                String productionType = "";

                for (PointDateDto pointDateDto : dateDtoList){
                    long startLong = pointDateDto.getStartInstant().getEpochSecond();
                    long endLong = pointDateDto.getEndInstant().getEpochSecond();
                    double productionNumSku = lineEfficiencySkuService.queryDataProduction(productionPointCode,startLong,endLong);

                    //当前的sku_code
                    double skuCode = pointDateDto.getSkuValue();

                    //判断配置sku配置信息是否只有一条记录，并且查看一条记录中的sku_code是否为空，如果为空，则当前所有sku都按照这个码垛机配置来进行计算
                    //瓶数
                    double skuSpeed = 0;
                    //容量
                    double bottleShapeValue = 0;
                    if(null != skuListProductionCapacity && skuListProductionCapacity.size() == 1 && StringUtils.isBlank(skuListProductionCapacity.get(0).getSkuCode())){
                        if(null != skuListProductionCapacity.get(0).getSkuSpeed()){
                            skuSpeed = skuListProductionCapacity.get(0).getSkuSpeed();
                            productionType = skuListProductionCapacity.get(0).getSkuName();
                        }
                        if(null != skuListProductionCapacity.get(0).getBottleShapeValue()){
                            bottleShapeValue = skuListProductionCapacity.get(0).getBottleShapeValue();
                        }
                    }else{
                        Optional<LineEfficiencySku> skuOptional = skuListProductionCapacity.stream().filter(x->StringUtils.isNotBlank(x.getSkuCode()) && Double.parseDouble(x.getSkuCode()) == skuCode).findAny();
                        if(skuOptional.isPresent()){
                            if(null != skuOptional.get().getSkuSpeed()){
                                skuSpeed = skuOptional.get().getSkuSpeed();
                            }
                            if(StringUtils.isBlank(productionType)){
                                productionType = skuOptional.get().getSkuName();
                            }else{
                                productionType = productionType + "、" + skuOptional.get().getSkuName();
                            }
                            if(null != skuOptional.get().getBottleShapeValue()){
                                bottleShapeValue = skuOptional.get().getBottleShapeValue();
                            }
                        }
                    }
                    //查询当前时间段内的瓶/听数
                    productionNum += productionNumSku * skuSpeed;
                    //计算容量
                    productionCapacity += productionNumSku * bottleShapeValue / 1000000.0;
                }
                lineEfficiencyStateRecord.setProductionStoredNum(productionNum);
                lineEfficiencyStateRecord.setProductionStoredCapacity(productionCapacity);
                lineEfficiencyStateRecord.setFirstStartTime(lineEfficiencyStateRecord.getStartTime());
                lineEfficiencyStateRecord.setProductionType(productionType);

                //计算末台设备开机时间,筛选出码垛机运行状态的点位
                List<LineEfficiencySku> listSkuState = listSku.stream().filter(x->x.getType() == 8 && x.getLineCode().equals(lineCode)).collect(Collectors.toList());
                if(null == listSkuState || listSkuState.size() != 1){
                    continue;
                }
                String statePointCode = listSkuState.get(0).getPointCode();
                List<PointDateDto> dateDtoListState = lineEfficiencySkuService.getTimeRangeByPointCode(statePointCode,startTime,endTime);
                for (PointDateDto pointDateDto : dateDtoListState){
                    if(pointDateDto.getSkuValue() == 1 && pointDateDto.getRunningTime() > 30){
                        lineEfficiencyStateRecord.setLastStartTime(Date.from(pointDateDto.getStartInstant()));
                        break;
                    }
                }
            }
        }
        return lineEfficiencyStateRecordList;
    }

}
