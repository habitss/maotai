package com.keytech.modules.alarm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.modules.alarm.constants.AlarmStatusEnum;
import com.keytech.modules.alarm.dto.AlarmReceiveRequest;
import com.keytech.modules.alarm.dto.AlarmReceiveResult;
import com.keytech.modules.alarm.entity.AlarmEvent;
import com.keytech.modules.alarm.entity.AlarmPoint;
import com.keytech.modules.alarm.mapper.AlarmEventMapper;
import com.keytech.modules.alarm.mapper.AlarmPointMapper;
import com.keytech.modules.alarm.service.IAlarmReceiveService;
import com.keytech.modules.ems.util.Java8DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 报警接收Service
 * @author yepeng
 * @since 2026.01.20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlarmReceiveServiceImpl implements IAlarmReceiveService {
    // 报警幂等性Service
    private final AlarmIdempotentService idempotentService;
    // 报警事件Mapper
    private final AlarmEventMapper alarmEventMapper;
    // 报警PI Mapper
    private final AlarmPointMapper alarmPointMapper;

    /**
     * 批量接收报警数据
     * 主要针对DF基于Kafka的报警接入场景
     * @param reqList 报警数据接收请求参数列表
     */
    @Override
    public void batchReceive(List<AlarmReceiveRequest> reqList) {
        if (reqList.isEmpty()) {
            return;
        }
        List<String> alarmPiCodeList = reqList.stream().map(AlarmReceiveRequest::getAlarmPiCode).distinct().toList();
        List<AlarmPoint> pointList = alarmPointMapper.selectList(Wrappers.<AlarmPoint>lambdaQuery().in(AlarmPoint::getAlarmPiCode, alarmPiCodeList));
        Map<String, AlarmPoint> pointMap = pointList.stream().collect(Collectors.toMap(AlarmPoint::getAlarmPiCode, Function.identity()));
        for (AlarmReceiveRequest req : reqList) {
            if (pointMap.containsKey(req.getAlarmPiCode())) {
                receive(req, pointMap.get(req.getAlarmPiCode()));
            }
        }
    }

    /**
     * 接收报警数据
     * 主要针对外部标准报警接入场景
     * @param req 报警数据接收请求参数
     * @param point 报警点位
     * @return 报警接收结果
     */
    @Override
    public AlarmReceiveResult receive(AlarmReceiveRequest req, AlarmPoint point) {
        try {
            // 幂等校验
            if (idempotentService.isDuplicate(req)) {
                log.info("重复报警，已忽略 source={}, requestId={}", req.getSourceSystem(), req.getRequestId());
                return new AlarmReceiveResult("1001", true, "报警已存在，重复请求已忽略", req.getRequestId());
            }

            // 查询报警点位
            if (point == null){
                point = alarmPointMapper.selectOne(Wrappers.<AlarmPoint>lambdaQuery().eq(AlarmPoint::getAlarmPiCode, req.getAlarmPiCode()).last("limit 1"));
            }
            if (point == null) {
                log.error("报警点位不存在 pointCode={}", req.getAlarmPiCode());
                return new AlarmReceiveResult("1002", false, "报警点位不存在", req.getRequestId());
            }

            // 构建报警事件
            AlarmEvent event = new AlarmEvent();
            event.setRequestId(req.getRequestId());
            event.setTenantId(point.getTenantId());
            event.setFactoryCode(point.getFactoryCode());
            event.setDeptCode(point.getDeptCode());
            event.setLineCode(point.getLineCode());
            event.setAlarmPiCode(req.getAlarmPiCode());
            event.setAlarmPointId(point.getId());
            event.setSourceSystem(req.getSourceSystem());
            event.setFuncLocCode(point.getFuncLocCode());
            event.setDeviceCode(point.getDeviceCode());
            event.setAlarmLevel(req.getAlarmLevel() != null ? req.getAlarmLevel() : 1);
            event.setAlarmTime(Java8DateUtil.parse(req.getAlarmTime()));
            event.setAlarmDesc(req.getAlarmDesc());
            event.setOnDutyPerson(req.getOnDutyPerson());
            event.setStatus(AlarmStatusEnum.UNRESPOND.getVal());
            event.setSort(0);
            event.setCreateBy("system");
            event.setCreateTime(LocalDateTime.now());
            event.setUpdateBy("system");
            event.setUpdateTime(LocalDateTime.now());
            event.setDelFlag(0);
            // 入库:幂等兜底靠 uk_source_request(source_system+request_id) 唯一索引,
            // 并发重放在这里抛 DuplicateKeyException,catch 后按重复忽略(与 SCADA rqi 幂等同款,弃 count 预查)
            alarmEventMapper.insert(event);
            log.info("报警接收成功 pi={}, level={}", event.getAlarmPiCode(), event.getAlarmLevel());
            return new AlarmReceiveResult("200", true, "报警接收成功", req.getRequestId());
        } catch (org.springframework.dao.DuplicateKeyException e) {
            log.info("重复报警，已忽略(唯一索引拦截) source={}, requestId={}", req.getSourceSystem(), req.getRequestId());
            return new AlarmReceiveResult("1001", true, "报警已存在，重复请求已忽略", req.getRequestId());
        } catch (Exception e) {
            log.error("报警接收失败 pi={}, requestId={} ", req.getAlarmPiCode(), req.getRequestId(), e);
            return new AlarmReceiveResult("500", false, "报警接收失败", req.getRequestId());
        }

    }

}
