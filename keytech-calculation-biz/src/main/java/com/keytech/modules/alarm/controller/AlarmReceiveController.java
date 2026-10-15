package com.keytech.modules.alarm.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.modules.alarm.dto.AlarmReceiveRequest;
import com.keytech.modules.alarm.dto.AlarmReceiveResponse;
import com.keytech.modules.alarm.dto.AlarmReceiveResult;
import com.keytech.modules.alarm.dto.AlarmReceiveValidator;
import com.keytech.modules.alarm.entity.AlarmIngestLog;
import com.keytech.modules.alarm.entity.AlarmPoint;
import com.keytech.modules.alarm.service.IAlarmIngestLogService;
import com.keytech.modules.alarm.service.IAlarmPointService;
import com.keytech.modules.alarm.service.IAlarmReceiveService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 标准报警接入接口
 * 包括 内外系统标准报警接入接口和外部系统标准报警接入接口
 * @author yepeng
 * @since 2026.01.20
 */
@RestController
@RequestMapping("/alarm")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "标准报警接入接口")
public class AlarmReceiveController {
    private final IAlarmReceiveService alarmReceiveService;
    private final IAlarmIngestLogService alarmIngestLogService;
    private final IAlarmPointService alarmPointService;

    /**
     * 内部系统标准报警接入接口
     */
    @PostMapping("/simple/receive")
    public Result<AlarmReceiveResponse> simpleReceive(@RequestBody AlarmReceiveRequest request) {
        // 记录报警接入日志
        AlarmIngestLog ingestLog = new AlarmIngestLog();
        ingestLog.setSourceSystem(request.getSourceSystem());
        ingestLog.setRequestId(request.getRequestId());
        ingestLog.setAlarmPiCode(request.getAlarmPiCode());
        ingestLog.setRawPayload(JSON.toJSONString(request));
        ingestLog.setReceiveTime(LocalDateTime.now());
        ingestLog.setIngestStatus(0);
        ingestLog.setCreateTime(LocalDateTime.now());
        try {
            //语法验证
            Map<String, String> validateResult = AlarmReceiveValidator.validate(request);
            if (!validateResult.isEmpty()) {
                String result = validateResult.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).reduce((a, b) -> a + ";" + b).orElse("");
                log.warn("报警接入语法验证失败 pointCode={}", request.getAlarmPiCode());
                AlarmReceiveResponse resp = new AlarmReceiveResponse("1003", false, result, request.getRequestId(), LocalDateTime.now());
                // 更新报警接入日志状态为失败
                ingestLog.setIngestStatus(2);
                ingestLog.setErrorMsg(result);
                return Result.OK(resp);
            }

            // 查询报警点位
            AlarmPoint point = alarmPointService.getOne(Wrappers.<AlarmPoint>lambdaQuery().eq(AlarmPoint::getAlarmPiCode, request.getAlarmPiCode()).last("limit 1"));
            if (point == null) {
                log.warn("报警点位不存在 pointCode={}", request.getAlarmPiCode());
                AlarmReceiveResponse resp = new AlarmReceiveResponse("1002", false, "报警点位不存在", request.getRequestId(), LocalDateTime.now());
                resp.setRequestId(request.getRequestId());
                resp.setServerTime(LocalDateTime.now());
                // 更新报警接入日志状态为失败
                ingestLog.setIngestStatus(2);
                ingestLog.setErrorMsg("报警点位不存在");
                return Result.OK(resp);
            }
            AlarmReceiveResult result = alarmReceiveService.receive(request, point);
            AlarmReceiveResponse resp = new AlarmReceiveResponse(result.getCode(), result.isSuccess(), result.getMessage(), request.getRequestId(), LocalDateTime.now());
            // 更新报警接入日志状态为成功
            ingestLog.setIngestStatus(1);
            ingestLog.setFactoryCode(point.getFactoryCode());
            ingestLog.setDeptCode(point.getDeptCode());
            ingestLog.setLineCode(point.getLineCode());
            return Result.OK(resp);
        }catch (Exception e) {
            log.error("报警接入异常 pointCode={}", request.getAlarmPiCode(), e);
            ingestLog.setIngestStatus(2);
            ingestLog.setErrorMsg(e.getMessage());
            return Result.error("报警接入异常");
        }finally {
            alarmIngestLogService.save(ingestLog);
        }
    }

    /**
     * 外部系统授权报警接入接口
     */
    @PostMapping("/auth/receive")
    public Result<AlarmReceiveResponse> authReceive(@Valid @RequestBody AlarmReceiveRequest request) {
        // 记录报警接入日志
        AlarmIngestLog ingestLog = new AlarmIngestLog();
        ingestLog.setSourceSystem(request.getSourceSystem());
        ingestLog.setRequestId(request.getRequestId());
        ingestLog.setAlarmPiCode(request.getAlarmPiCode());
        ingestLog.setRawPayload(JSON.toJSONString(request));
        ingestLog.setReceiveTime(LocalDateTime.now());
        ingestLog.setIngestStatus(0);
        ingestLog.setCreateTime(LocalDateTime.now());
        try {
            //语法验证
            Map<String, String> validateResult = AlarmReceiveValidator.validate(request);
            if (!validateResult.isEmpty()) {
                String result = validateResult.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).reduce((a, b) -> a + ";" + b).orElse("");
                log.warn("报警接入语法验证失败 pointCode={}", request.getAlarmPiCode());
                AlarmReceiveResponse resp = new AlarmReceiveResponse("1003", false, result, request.getRequestId(), LocalDateTime.now());
                // 更新报警接入日志状态为失败
                ingestLog.setIngestStatus(2);
                ingestLog.setErrorMsg(result);
                return Result.OK(resp);
            }

            // 查询报警点位
            AlarmPoint point = alarmPointService.getOne(Wrappers.<AlarmPoint>lambdaQuery().eq(AlarmPoint::getAlarmPiCode, request.getAlarmPiCode()).last("limit 1"));
            if (point == null) {
                log.warn("报警点位不存在 pointCode={}", request.getAlarmPiCode());
                AlarmReceiveResponse resp = new AlarmReceiveResponse("1002", false, "报警点位不存在", request.getRequestId(), LocalDateTime.now());
                resp.setRequestId(request.getRequestId());
                resp.setServerTime(LocalDateTime.now());
                // 更新报警接入日志状态为失败
                ingestLog.setIngestStatus(2);
                ingestLog.setErrorMsg("报警点位不存在");
                return Result.OK(resp);
            }
            AlarmReceiveResult result = alarmReceiveService.receive(request, point);
            AlarmReceiveResponse resp = new AlarmReceiveResponse(result.getCode(), result.isSuccess(), result.getMessage(), request.getRequestId(), LocalDateTime.now());
            // 更新报警接入日志状态为成功
            ingestLog.setIngestStatus(1);
            ingestLog.setFactoryCode(point.getFactoryCode());
            ingestLog.setDeptCode(point.getDeptCode());
            ingestLog.setLineCode(point.getLineCode());
            return Result.OK(resp);
        }catch (Exception e) {
            log.error("报警接入异常 pointCode={}", request.getAlarmPiCode(), e);
            ingestLog.setIngestStatus(2);
            ingestLog.setErrorMsg(e.getMessage());
            return Result.error("报警接入异常");
        }finally {
            alarmIngestLogService.save(ingestLog);
        }
    }


}