package com.keytech.modules.maotai.scada.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.modules.alarm.dto.AlarmReceiveRequest;
import com.keytech.modules.alarm.dto.AlarmReceiveResult;
import com.keytech.modules.alarm.entity.AlarmIngestLog;
import com.keytech.modules.alarm.entity.AlarmPoint;
import com.keytech.modules.alarm.service.IAlarmIngestLogService;
import com.keytech.modules.alarm.service.IAlarmPointService;
import com.keytech.modules.alarm.service.IAlarmReceiveService;
import com.keytech.modules.maotai.scada.dto.ScadaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import cn.hutool.json.JSONUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;

/**
 * 中集自控(SCADA)工艺报警接收(入站:中集自控 → 平台)。
 * <p>与工单入站接口(API06/13/17)同前缀风格,走 /api/** 入站(无鉴权,原 X-Api-Token 鉴权已移除,靠 Shiro 放行 anon)。
 * 复用通用报警接入 {@link IAlarmReceiveService#receive} 落 alarm_event;回中集自控公共响应(rqi/resultCode/resultDesp)。
 *
 * <p>对接方式:中集自控在报警发生时主动 POST 到本接口;平台按 requestId(对应中集 rqi)做幂等。
 * 接收前置条件:alarm_point 表需已录入对应的 alarm_pi_code,否则返回"报警点位不存在"。
 *
 * @author maotai
 * @since 2026-08-12
 */
@Tag(name = "中集自控-工艺报警接入")
@RestController
@RequestMapping("/openapi/v1/api/Alarm")
@RequiredArgsConstructor
@Slf4j
public class ScadaAlarmReceiveController {

    private final IAlarmReceiveService alarmReceiveService;
    private final IAlarmPointService alarmPointService;
    private final IAlarmIngestLogService alarmIngestLogService;
    private final ApiOpLogService opLogService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private static final String API_ALARM_RECEIVE = "API_ALARM_IN";

    /**
     * 接收中集自控工艺报警:记接入日志 → 查点位 → 落 alarm_event → 回中集公共响应。
     * <p>入站接口无鉴权(原 X-Api-Token 已移除)。
     * @param request 报警数据(sourceSystem/requestId/alarmPiCode/alarmTime/alarmLevel/alarmDesc 等)。
     *                requestId 对应中集 rqi,用于幂等;sourceSystem 建议传"中集自控"。
     * @return 中集自控公共响应(resultCode 1=成功 0=失败;rqi 回传同 request.requestId)
     */
    @Operation(summary = "接收中集自控工艺报警", description = "中集自控 → 平台。入站走 OAuth2 Bearer(/openapi/v1/**)")
    @PostMapping("/Receive")
    public ScadaResponse receive(@Valid @RequestBody AlarmReceiveRequest request) {
        String rqi = request.getRequestId();
        // 记接入日志(抄 AlarmReceiveController#simpleReceive L44-52 模式)
        AlarmIngestLog ingestLog = new AlarmIngestLog();
        ingestLog.setSourceSystem(request.getSourceSystem());
        ingestLog.setRequestId(rqi);
        ingestLog.setAlarmPiCode(request.getAlarmPiCode());
        ingestLog.setRawPayload(JSON.toJSONString(request));
        ingestLog.setReceiveTime(LocalDateTime.now());
        ingestLog.setIngestStatus(0);
        ingestLog.setCreateTime(LocalDateTime.now());
        try {
            // 查报警点位 —— receive() 内部也会查一次,但这里先查是为了在点位缺失时把 ingestLog 的
            // factory/dept/line 填上(成功路径需要),以及让 1002 走干净的失败分支
            AlarmPoint point = alarmPointService.getOne(
                    Wrappers.<AlarmPoint>lambdaQuery()
                            .eq(AlarmPoint::getAlarmPiCode, request.getAlarmPiCode())
                            .last("limit 1"));
            if (point == null) {
                log.warn("[SCADA-Alarm] 报警点位不存在 piCode={}", request.getAlarmPiCode());
                ingestLog.setIngestStatus(2);
                ingestLog.setErrorMsg("报警点位不存在");
                recordAlarmInbound(rqi, request, 0, "报警点位不存在: " + request.getAlarmPiCode());
                ScadaResponse resp = ScadaResponse.fail(rqi, "报警点位不存在: " + request.getAlarmPiCode());
                opLogService.attachInboundResponse(API_ALARM_RECEIVE, JSONUtil.toJsonStr(resp));
                return resp;
            }
            // 落 alarm_event(内部按 sourceSystem+requestId 幂等)
            AlarmReceiveResult result = alarmReceiveService.receive(request, point);
            ingestLog.setIngestStatus(result.isSuccess() ? 1 : 2);
            ingestLog.setErrorMsg(result.isSuccess() ? null : result.getMessage());
            ingestLog.setFactoryCode(point.getFactoryCode());
            ingestLog.setDeptCode(point.getDeptCode());
            ingestLog.setLineCode(point.getLineCode());

            // 映射成中集公共响应:成功(200)+ 重复(1001,幂等成功)→ resultCode=1;其余 → 0
            if (result.isSuccess()) {
                recordAlarmInbound(rqi, request, 1, result.getMessage());
                ScadaResponse resp = ScadaResponse.ok(rqi, result.getMessage());
                opLogService.attachInboundResponse(API_ALARM_RECEIVE, JSONUtil.toJsonStr(resp));
                return resp;
            }
            recordAlarmInbound(rqi, request, 0, result.getMessage());
            ScadaResponse resp = ScadaResponse.fail(rqi, result.getMessage());
                opLogService.attachInboundResponse(API_ALARM_RECEIVE, JSONUtil.toJsonStr(resp));
                return resp;
        } catch (Exception e) {
            log.error("[SCADA-Alarm] 接收异常 piCode={} requestId={}", request.getAlarmPiCode(), rqi, e);
            ingestLog.setIngestStatus(2);
            ingestLog.setErrorMsg(e.getMessage());
            recordAlarmInbound(rqi, request, 0, "报警接收异常: " + e.getMessage());
            ScadaResponse resp = ScadaResponse.fail(rqi, "报警接收异常: " + e.getMessage());
            opLogService.attachInboundResponse(API_ALARM_RECEIVE, JSONUtil.toJsonStr(resp));
            return resp;
        } finally {
            try {
                alarmIngestLogService.save(ingestLog);
            } catch (Exception logEx) {
                log.error("[SCADA-Alarm] 接入日志落库失败 requestId={}", rqi, logEx);
            }
        }
    }

    /** 报警入站记 op_log(operator=中集自控;内部 try 不抛,落日志失败不影响主流程) */
    private void recordAlarmInbound(String rqi, AlarmReceiveRequest request, int result, String msg) {
        try {
            opLogService.recordInbound("SCADA", API_ALARM_RECEIVE, request.getAlarmPiCode(), rqi, result,
                    com.alibaba.fastjson.JSON.toJSONString(request), msg);
        } catch (Exception ignore) {
        }
    }
}
