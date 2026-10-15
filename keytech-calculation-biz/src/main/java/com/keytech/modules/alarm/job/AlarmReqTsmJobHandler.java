package com.keytech.modules.alarm.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.alarm.constants.AlarmStatusEnum;
import com.keytech.modules.alarm.constants.TsmStatusEnum;
import com.keytech.modules.alarm.entity.AlarmEvent;
import com.keytech.modules.alarm.entity.AlarmPoint;
import com.keytech.modules.alarm.entity.AlarmTsmLog;
import com.keytech.modules.alarm.service.IAlarmEventService;
import com.keytech.modules.alarm.service.IAlarmPointService;
import com.keytech.modules.alarm.service.IAlarmTsmLogService;
import com.keytech.modules.alarm.util.HttpUtil;
import com.keytech.modules.ems.util.Java8DateUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 报警事实-TSM请求 定时任务
 * @author yepeng
 * @since 2026.01.26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmReqTsmJobHandler {
    private final IAlarmPointService alarmPointService;
    private final IAlarmEventService alarmEventService;
    private final IAlarmTsmLogService alarmTsmLogService;
    @Value("${third-config.tsm.url}")
    private String tsmUrlPrefix;
    @Value("${third-config.tsm.token}")
    private String tsmToken;

    /**
     * 报警事实-TSM请求 定时任务（每2分钟执行一次）
     * 每次查询时间最早的50条报警事件，推送到TSM，推送成功后，将报警事件状态修改为已响应
     */
    @XxlJob("pushAlarmToTsmJob")
    public ReturnT<String> handlePushAlarmToTsmJob(String params) {
        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        long start = System.currentTimeMillis();
        log.info("开始执行推送报警事实-TSM请求任务[{}]", LocalDateTime.now().format(Java8DateUtil.sdf1));
        // 1. 查询需要推TSM的报警点
        List<AlarmPoint> alarmPoints = alarmPointService.lambdaQuery()
                .select(List.of(AlarmPoint::getId))
                .eq(AlarmPoint::getPushTsm, 1).list();
        if(CollectionUtils.isEmpty(alarmPoints)) {
            log.info("没有需要推TSM的报警点");
            return ReturnT.SUCCESS;
        }
        List<Integer> alarmPointIds = alarmPoints.stream().map(AlarmPoint::getId).toList();
        // 2. 查询需要推TSM的报警事件
        Page<AlarmEvent> page = new Page<>(1, 50);
        List<AlarmEvent> alarmEventList = alarmEventService.lambdaQuery()
                .in(AlarmEvent::getAlarmPointId, alarmPointIds)
                .orderByAsc(AlarmEvent::getId)
                .page(page).getRecords();
        if(CollectionUtils.isEmpty(alarmEventList)) {
            log.info("没有需要推TSM的报警事件");
            return ReturnT.SUCCESS;
        }
        // 3. 推送报警事件到TSM
        for (AlarmEvent item : alarmEventList) {
            //APAC-CN9322A4_AggreAlr1-INV_8 后半部分
            String piCode = StringUtils.substringAfter(item.getAlarmPiCode(), "_");
            String exceptionId = item.getRequestId();
            String exceptionTime = item.getAlarmTime().format(DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss"));
            String thingId = item.getFuncLocCode();
            String factory = item.getFactoryCode();
            String district = item.getLineCode();
            String cellId = "-1";
            StringBuilder sb = new StringBuilder("production-plan-0.0.1-SNAPSHOT/ccrAbnormalPushWork/api/createOrder?");
            sb.append("piCode="+piCode)
                    .append("&factory="+factory)
                    .append("&district="+district)
                    .append("&eventId="+exceptionId)
                    .append("&exceptionId="+exceptionId)
                    .append("&sysFlag=CCR")
                    .append("&exceptionTime="+exceptionTime)
                    .append("&cellId="+cellId)
                    .append("&vFunlocCode="+thingId);
            String url = tsmUrlPrefix + sb;
            log.info("发起异常推送TSM请求,url:{}", url);
            
            // 记录请求开始
            AlarmTsmLog logEntity = new AlarmTsmLog();
            // 1-推送报警
            logEntity.setType(1);
            logEntity.setRequestParams(url);
            logEntity.setRequestTime(LocalDateTime.now());
            
            String result = HttpUtil.httpSendPost(url);
            log.info("发起异常推送TSM请求,result:{}",result);
            
            // 记录请求结束
            logEntity.setResponseTime(LocalDateTime.now());
            logEntity.setResponseResult(result);
            if(StringUtils.isEmpty(result)) {
                // 0-失败
                logEntity.setStatus(0);
                logEntity.setErrorMsg("返回结果为空");
            } else {
                try {
                    Integer code = JSON.parseObject(result).getInteger("code");
                    if(200 == code) {
                        // 1-成功
                        logEntity.setStatus(1);
                    } else {
                        // 0-失败
                        logEntity.setStatus(0);
                        logEntity.setErrorMsg("返回码异常: " + code);
                    }
                } catch (Exception e) {
                    // 0-失败
                    logEntity.setStatus(0);
                    logEntity.setErrorMsg("解析返回结果失败: " + e.getMessage());
                }
            }
            alarmTsmLogService.save(logEntity);
            if(StringUtils.isEmpty(result)) {
                log.info("发起异常推送TSM请求,result is empty!");
                continue;
            }
            //解析返回值
            JSONObject resultJson = JSON.parseObject(result);
            Integer code = resultJson.getInteger("code");
            //2.1、将推送TSM成功的异常特殊标记
            if (200 == code) {
                log.info("发起异常推送TSM请求,Success!");
                //修改报警事实状态为 1-已响应
                alarmEventService.lambdaUpdate().eq(AlarmEvent::getId, item.getId())
                        .set(AlarmEvent::getUpdateTime, LocalDateTime.now())
                        .set(AlarmEvent::getStatus, AlarmStatusEnum.RESPONDED.getVal()).update();
            } else {
                log.info("发起异常推送TSM请求,Failed!");
            }
        }
        log.info("结束执行推送报警事实-TSM请求任务[{}], 耗时：{} ms", LocalDateTime.now().format(Java8DateUtil.sdf1), System.currentTimeMillis() - start);
        return ReturnT.SUCCESS;
    }

    /**
     * 报警事实-TSM任务状态查询 定时任务（每2分钟执行一次）
     * 每次查询时间最早的50条已响应的报警事件，通过TSM查询接口查询，将报警事件状态修改为处理中/已处理
     */
    @XxlJob("queryAlarmTsmStateJob")
    public ReturnT<String> handleQueryAlarmTsmStateJob(String params) {
        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        long start = System.currentTimeMillis();
        log.info("开始执行查询TMS中报警事实任务状态[{}]", LocalDateTime.now().format(Java8DateUtil.sdf1));
        // 1. 查询最早的50条已响应的报警事件，按报警时间从小到大排序，如果最小时间到最大时间超过3天，则用eventId查询，否则用时间段查询
        Page<AlarmEvent> page = new Page<>(1, 50);
        List<AlarmEvent> alarmEventList = alarmEventService.lambdaQuery()
                .select(List.of(AlarmEvent::getId, AlarmEvent::getAlarmTime, AlarmEvent::getRequestId))
                .eq(AlarmEvent::getStatus, AlarmStatusEnum.RESPONDED.getVal())
                .orderByAsc(AlarmEvent::getId)
                .page(page).getRecords();
        if(CollectionUtils.isEmpty(alarmEventList)) {
            log.info("没有需要查询状态的报警事件");
            return ReturnT.SUCCESS;
        }
        // 2. 先排序再查询，避免查询时报错
        alarmEventList.sort(Comparator.comparing(AlarmEvent::getAlarmTime));
        LocalDateTime minTime = null;
        LocalDateTime maxTime = alarmEventList.get(alarmEventList.size() - 1).getAlarmTime();
        int size = alarmEventList.size();
        boolean isQueryByTime = false;
        for (int i = 0; i < size; i++) {
            AlarmEvent item = alarmEventList.get(i);
            minTime = item.getAlarmTime();
            if(maxTime.isAfter(minTime.plusDays(3)) || i + 3 > size) {
                StringBuilder sb = new StringBuilder("openapi/tsm/ccrabnormalpush/getResult?");
                sb.append("&sysFlag=CCR").append("&eventId="+item.getRequestId());
                String url = tsmUrlPrefix + sb;
                log.info("发起TSM报警状态查询请求,url:{}", url);
                
                // 记录请求开始
                AlarmTsmLog logEntity = new AlarmTsmLog();
                // 2-查询状态
                logEntity.setType(2);
                logEntity.setRequestParams(url);
                logEntity.setRequestTime(LocalDateTime.now());
                
                String result = HttpUtil.httpSendGet(url, tsmToken);
                log.info("发起TSM报警状态查询请求,result:{}",result);
                
                // 记录请求结束
                logEntity.setResponseTime(LocalDateTime.now());
                logEntity.setResponseResult(result);
                if(StringUtils.isEmpty(result)) {
                    // 0-失败
                    logEntity.setStatus(0);
                    logEntity.setErrorMsg("返回结果为空");
                } else {
                    try {
                        Integer code = JSON.parseObject(result).getInteger("code");
                        if(200 == code) {
                            // 1-成功
                            logEntity.setStatus(1);
                        } else {
                            // 0-失败
                            logEntity.setStatus(0);
                            logEntity.setErrorMsg("返回码异常: " + code);
                        }
                    } catch (Exception e) {
                        // 0-失败
                        logEntity.setStatus(0);
                        logEntity.setErrorMsg("解析返回结果失败: " + e.getMessage());
                    }
                }
                alarmTsmLogService.save(logEntity);
                if(StringUtils.isEmpty(result)) {
                    log.info("发起TSM报警状态查询请求,result is empty!");
                    continue;
                }
                //解析返回值
                JSONObject resultJson = JSON.parseObject(result);
                Integer code = resultJson.getInteger("code");
                if (200 == code) {
                    log.info("发起TSM报警状态查询请求,Success!");
                    JSONObject data = resultJson.getJSONObject("data");
                    handleTsmStateUpdate(data, item);
                } else {
                    log.info("发起TSM报警状态查询请求,Failed!");
                }
            }else {
                //跳出去用时间段查询
                isQueryByTime = true;
                break;
            }
        }
        if(isQueryByTime) {
            Map<String, AlarmEvent> alarmEventMap = alarmEventList.stream().collect(Collectors.toMap(AlarmEvent::getRequestId, Function.identity(), (k1, k2) -> k1));
            StringBuilder sb = new StringBuilder("openapi/tsm/ccrabnormalpush/getResult?");
            sb.append("&sysFlag=CCR")
              .append("&startTime="+minTime.format(DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss")))
              .append("&endTime="+maxTime.format(DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss")));
            String url = tsmUrlPrefix + sb;
            log.info("发起TSM报警状态查询请求,url:{}", url);
            
            // 记录请求开始
            AlarmTsmLog logEntity = new AlarmTsmLog();
            logEntity.setType(2); // 2-查询状态
            logEntity.setRequestParams(url);
            logEntity.setRequestTime(LocalDateTime.now());
            
            String result = HttpUtil.httpSendGet(url, tsmToken);
            log.info("发起TSM报警状态查询请求,result:{}",result);
            
            // 记录请求结束
            logEntity.setResponseTime(LocalDateTime.now());
            logEntity.setResponseResult(result);
            if(StringUtils.isEmpty(result)) {
                logEntity.setStatus(0); // 0-失败
                logEntity.setErrorMsg("返回结果为空");
            } else {
                try {
                    Integer code = JSON.parseObject(result).getInteger("code");
                    if(200 == code) {
                        // 1-成功
                        logEntity.setStatus(1);
                    } else {
                        // 0-失败
                        logEntity.setStatus(0);
                        logEntity.setErrorMsg("返回码异常: " + code);
                    }
                } catch (Exception e) {
                    // 0-失败
                    logEntity.setStatus(0);
                    logEntity.setErrorMsg("解析返回结果失败: " + e.getMessage());
                }
            }
            alarmTsmLogService.save(logEntity);
            if(StringUtils.isNotEmpty(result)) {
                //解析返回值
                JSONObject resultJson = JSON.parseObject(result);
                Integer code = resultJson.getInteger("code");
                if (200 == code) {
                    log.info("发起TSM报警状态查询请求,Success!");
                    JSONArray data = resultJson.getJSONArray("data");
                    //数组中是多个反应方案记录，状态和处理人都是一样的，只用取第一个
                    if (data != null && data.size() > 0) {
                        JSONObject item = data.getJSONObject(0);
                        handleTsmStateUpdate(item, alarmEventMap.get(item.getString("eventId")));
                    }else {
                        log.info("发起TSM报警状态查询请求,data is empty!");
                    }
                } else {
                    log.info("发起TSM报警状态查询请求,Failed!");
                }
            }
        }

        log.info("结束执行查询TMS中报警事实任务状态[{}], 耗时：{} ms", LocalDateTime.now().format(Java8DateUtil.sdf1), System.currentTimeMillis() - start);
        return ReturnT.SUCCESS;
    }

    /**
     * 处理单个TSM报警状态更新
     * @param data TMS返回的报警状态
     * @param alarmEvent 报警事实
     */
    private void handleTsmStateUpdate(JSONObject data, AlarmEvent alarmEvent) {
        Integer tsmStatus = data.getInteger("status");
        String onDutyPerson = data.getString("executorName");
        String eventId = data.getString("eventId");
        //修改报警事实状态为：0、未处理->已响应 1、处理中->处理中 (2、已解决 3、未解决 6、无法解决 7、待班长审核)->已处理
        Integer status;
        if (TsmStatusEnum.UNPROCESS.getVal().equals(tsmStatus)) {
            status = AlarmStatusEnum.UNRESPOND.getVal();
        }else if (TsmStatusEnum.PROCESSING.getVal().equals(tsmStatus)) {
            status = AlarmStatusEnum.PROCESSING.getVal();
        }else {
            status = AlarmStatusEnum.PROCESSED.getVal();
        }
        //支持主键及eventId更新
        alarmEventService.lambdaUpdate()
                .eq(alarmEvent != null, AlarmEvent::getId, alarmEvent.getId())
                .eq(alarmEvent == null, AlarmEvent::getRequestId, eventId)
                .eq(AlarmEvent::getStatus, AlarmStatusEnum.RESPONDED.getVal())
                .set(AlarmEvent::getOnDutyPerson, onDutyPerson)
                .set(AlarmEvent::getUpdateTime, LocalDateTime.now())
                .set(AlarmEvent::getStatus, status)
                .set(AlarmEvent::getTsmState, tsmStatus)
                .update();
        //如果状态为待班长审核，则更新报警点状态为不可用，仅显示在待办事项中，直到处理完成后手动调整点位状态为可用才会重新监测此点位
        if(TsmStatusEnum.WAITING_AUDIT.getVal().equals(tsmStatus)) {
            alarmPointService.lambdaUpdate()
                    .eq(AlarmPoint::getEnableFlag, 0)
                    .set(AlarmPoint::getUpdateTime, LocalDateTime.now())
                    .update();
        }
    }

}
