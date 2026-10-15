package com.keytech.modules.pi.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.pi.common.MeasurementData;
import com.keytech.modules.pi.entity.PiBasePoint;
import com.keytech.modules.pi.mapper.PiBasePointMapper;
import com.keytech.modules.pi.service.IPiBasePointService;
import com.keytech.modules.pi.service.IPiComputationModelService;
import com.keytech.modules.pi.util.MeasurementDataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 基础点位维护
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
@Service
public class PiBasePointServiceImpl extends BaseServiceImpl<PiBasePointMapper, PiBasePoint> implements IPiBasePointService {

    //6小时
    private static final int HOUR = 6;
    @Autowired
    private IPiComputationModelService iPiComputationModelService;

    @Override
    public void getPointData(Page<PiBasePoint> list) {
        //获取点位集合 list中的pointCode 集合
        List<String> pointList = list.getRecords().stream().map(PiBasePoint::getPointCode).toList();
        //startTime是前6小时时间  endTime当前时间  时间类型LocalDateTime
        //LocalDateTime startTime = LocalDateTime.now().minusHours(HOUR);
        //当前时间
        LocalDateTime time = LocalDateTime.now();
        //获取点位值
        List<MeasurementData> measurementDataList = iPiComputationModelService.influxDbMoment(pointList, time, "down");

        //遍历 measurementDataList 将每个最新值赋给pointCode 做key  值为MeasurementData 的map中
        Map<String, MeasurementData> latestDataMap = measurementDataList.stream()
                .collect(Collectors.toMap(
                        MeasurementData::getCnlNum,
                        Function.identity(),
                        (existing, replacement) ->
                                existing.getTime().isAfter(replacement.getTime()) ? existing : replacement
                ));
        //遍历list 将PiBasePoint 中的pointCode 与latestDataMap key相同对象 取出value 赋值给latestValue time赋值给lastUpdateTime
        list.getRecords().forEach(point -> {
            MeasurementData latestData = latestDataMap.get(point.getPointCode());
            if (latestData != null) {
                point.setLatestValue(latestData.getValue());
                // 时间转
                point.setLastUpdateTime(
                  latestData.getTime() != null ? Date.from(latestData.getTime()) : null
                );
            }
        });


    }
}
