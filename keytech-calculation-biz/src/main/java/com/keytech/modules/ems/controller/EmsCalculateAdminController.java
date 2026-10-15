package com.keytech.modules.ems.controller;

import com.keytech.modules.ems.constants.EmsConstantsX;
import com.keytech.modules.ems.dto.HistoryCurveDto;
import com.keytech.modules.ems.job.*;
import com.keytech.modules.ems.service.impl.EmsCalculateService;
import com.keytech.modules.ems.service.impl.EmsInfluxDbService;
import com.keytech.modules.ems.vo.FluxZone8Data;
import com.keytech.modules.ems.vo.HistoryCurveVo;
import com.keytech.modules.ems.wx.WxCpMessageService;
import com.xxl.job.core.biz.model.ReturnT;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
* EMS 计算模块管理端接口
* @author yepeng
* @since 2025.05.06
* */
@Tag(name="计算模块管理端接口")
@RestController
@RequestMapping("/ems/admin")
@Slf4j
@RequiredArgsConstructor
public class EmsCalculateAdminController  {
    private final InitCollectPointStatusJob initCollectPointStatusJob;
    private final CollectorJobHandler collectorJobHandler;
    private final RetryJobHandler retryJobHandler;
    private final ManualBackfillJobHandler manualBackfillHandler;
    private final EmsInfluxDbService emsInfluxDbService;
    private final AggregationCalcJobHandler aggregationCalcJobHandler;
    private final WarningCalcErrorJobHandler warningCalcErrorJobHandler;
    private final WxCpMessageService wxCpMessageService;
    private final EmsCalculateService emsCalculateService;
    private final RedisTemplate<String, String> redisTemplate;

    @Operation(summary="执行一次初始化小时点位状态任务")
    //@RequiresPermissions("ems:calculateJob:runOnce")
    @GetMapping(value = "/xxlJob/initHourCollectPointStatus")
    public Result<ReturnT<String>> initHourCollectPointStatus(@RequestParam(required = false) String params) {
        ReturnT<String> result = initCollectPointStatusJob.initCollectPointStatus("{\"x-tenant-id\":1006, \"grainType\":1}");
        return Result.OK(result);
    }

    @Operation(summary="执行一次初始半小时化点位状态任务")
    //@RequiresPermissions("ems:calculateJob:runOnce")
    @GetMapping(value = "/xxlJob/initHalfHourCollectPointStatus")
    public Result<ReturnT<String>> initHalfHourCollectPointStatus(@RequestParam(required = false) String params) {
        ReturnT<String> result = initCollectPointStatusJob.initCollectPointStatus("{\"x-tenant-id\":1006, \"grainType\":0}");
        return Result.OK(result);
    }

    @Operation(summary="执行一次小时周期采集任务")
    //@RequiresPermissions("ems:calculateJob:runOnce")
    @GetMapping(value = "/xxlJob/collectHourPointJob")
    public Result<ReturnT<String>> collectHourPointJob(@RequestParam(required = false) String params) {
        ReturnT<String> result = collectorJobHandler.collectHourPointJob(params);
        return Result.OK(result);
    }

    @Operation(summary="执行一次半小时周期采集任务")
    //@RequiresPermissions("ems:calculateJob:runOnce")
    @GetMapping(value = "/xxlJob/collectHalfHourPointJob")
    public Result<ReturnT<String>> collectHalfHourPointJob(@RequestParam(required = false) String params) {
        ReturnT<String> result = collectorJobHandler.collectHalfHourPointJob(params);
        return Result.OK(result);
    }

    @Operation(summary="执行一次失败重试任务")
    //@RequiresPermissions("ems:calculateJob:runOnce")
    @GetMapping(value = "/xxlJob/retryFailedCollectJob")
    public Result<ReturnT<String>> retryFailedCollectJob(@RequestParam(required = false) String params) {
        ReturnT<String> result = retryJobHandler.retryFailedCollectJob(params);
        return Result.OK(result);
    }

    @Operation(summary="执行一次人工补采任务")
    //@RequiresPermissions("ems:calculateJob:runOnce")
    @GetMapping(value = "/xxlJob/manualBackfillCollectJob")
    public Result<ReturnT<String>> manualBackfillCollectJob(@RequestParam(required = false) String params) {
        ReturnT<String> result = manualBackfillHandler.manualBackfillCollectJob(params);
        return Result.OK(result);
    }

    @Operation(summary="执行一次中间点小时聚合计算任务")
    //@RequiresPermissions("ems:calculateJob:runOnce")
    @GetMapping(value = "/xxlJob/orsHourAggregationCalcJob")
    public Result<ReturnT<String>> orsHourAggregationCalcJob(@RequestParam(required = false) String params) {
        ReturnT<String> result = aggregationCalcJobHandler.handleOrsHourAggregationCalcJob(params);
        return Result.OK(result);
    }

    @Operation(summary="执行一次采集到中间点班聚合计算任务")
    //@RequiresPermissions("ems:calculateJob:runOnce")
    @GetMapping(value = "/xxlJob/classAggregationCalcJob")
    public Result<ReturnT<String>> classAggregationCalcJob(@RequestParam(required = false) String params) {
        ReturnT<String> result = aggregationCalcJobHandler.handleClassAggregationCalcJob(params);
        return Result.OK(result);
    }

    @Operation(summary="执行一次采集到中间点天(周月年)聚合计算任务")
    //@RequiresPermissions("ems:calculateJob:runOnce")
    @GetMapping(value = "/xxlJob/dayWeekMtdYtdAggregationCalcJob")
    public Result<ReturnT<String>> dayWeekMtdYtdAggregationCalcJob(@RequestParam(required = false) String params) {
        ReturnT<String> result = aggregationCalcJobHandler.handleDayWeekMtdYtdAggregationCalcJob(params);
        return Result.OK(result);
    }

    @Operation(summary="执行一次采集异常预警扫描任务")
    //@RequiresPermissions("ems:calculateJob:runOnce")
    @GetMapping(value = "/xxlJob/doWarningCalcErrorJob")
    public Result<ReturnT<String>> doWarningCalcErrorJob(@RequestParam(required = false) String params) {
        ReturnT<String> result = warningCalcErrorJobHandler.handleWarningCalcErrorJob(params);
        return Result.OK(result);
    }

    @Operation(summary="执行一次补采记录参考值计算任务")
    //@RequiresPermissions("ems:calculateJob:runOnce")
    @GetMapping(value = "/xxlJob/doInitManualBackfillPointStatusJob")
    public Result<ReturnT<String>> doInitManualBackfillPointStatusJob(@RequestParam(required = false) String params) {
        ReturnT<String> result = initCollectPointStatusJob.initManualBackfillPointStatus(params);
        return Result.OK(result);
    }

    /**
     * 查询指定点位及时间段的X分钟底数
     */
    @Operation(summary="查询指定点位及时间段的X分钟底数")
    //@RequiresPermissions("ems:calculateJob:runOnce")
    @GetMapping(value = "/influx/queryMultiPointsWithIntervalLast")
    public Result<Object> queryMultiPointsWithIntervalLast(@RequestParam String points,
                                                                    @RequestParam String startTime,
                                                                    @RequestParam String endTime,
                                                                    @RequestParam Integer interval) {
        List<String> pointList = Arrays.asList(points.split(","));
        LocalDateTime sTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime eTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<FluxZone8Data> resultList = emsInfluxDbService.queryMultiPointsWithIntervalLast(pointList, sTime, eTime, interval);
        Map<String, List<FluxZone8Data>> result =  resultList.stream().collect(Collectors.groupingBy(FluxZone8Data::getFluxCode));
        return Result.OK(result);
    }

    /**
     * 发送企业微信消息
     * @param userIds 用户ID串
     * @param message 发送消息
     */
    @GetMapping(value = "/wx/cp/sendMessage")
    public Result<String> sendCpMessage(@RequestParam String userIds, @RequestParam String message) {
        String res = this.wxCpMessageService.sendTextMessage(Arrays.asList(userIds.split(",")), List.of(), List.of(), message);
        return Result.OK(res);
    }

    /**
     * 执行一次异常预警消息推送任务
     */
    @GetMapping(value = "/webhook/sendMessage")
    public Result<ReturnT<String>> doWarningMsgPushJob(@RequestParam(required = false) String params) {
        ReturnT<String> result = this.warningCalcErrorJobHandler.handleWarningMsgPushJob(params);
        return Result.OK(result);
    }

    /**
     * 获取历史曲线
     */
    @PostMapping(value = "/influx/getHistoryCurveData")
    public Result<List<HistoryCurveVo>> getHistoryCurveData(@RequestBody HistoryCurveDto dto){
        try {
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String facOrgCode = loginUser.getOrgCode();
            if(StringUtils.isNotEmpty(facOrgCode)) {
                facOrgCode = facOrgCode.substring(0, Math.min(facOrgCode.length(), 9));
            }
            dto.setFactoryCode(facOrgCode);
            return Result.OK(this.emsCalculateService.getHistoryCurveData(dto));
        } catch (Exception e) {
            log.error("获取历史曲线时:", e);
            return Result.error("获取历史曲线时异常");
        }
    }

    /**
     * 设置采集计算调试模式，中断正常采集，从Redis中读取设置的点位采集或计算
     * @param debugFlag 1-开启 2-关闭
     */
    @GetMapping(value = "/calculate/debug/{debugFlag}")
    public Result<String> setCalculateDebug(@PathVariable Integer debugFlag) {
        try {
            if(debugFlag == 1) {
                redisTemplate.opsForValue().set(EmsConstantsX.J_CALCULATE_DEBUG, "1");
                return Result.OK("开启采集计算调试模式成功");
            }else if(debugFlag == 0) {
                redisTemplate.opsForValue().set(EmsConstantsX.J_CALCULATE_DEBUG, "0");
                return Result.OK("关闭采集计算调试模式成功");
            }else {
                return Result.error("参数错误");
            }
        } catch (Exception e) {
            log.error("设置采集计算调试模式时:", e);
            return Result.error("设置采集计算调试模式时异常");
        }
    }

    /**
     * 设置采集计算调试模式，中断正常采集，从Redis中读取设置的点位采集或计算
     * @param ctags 采集点位
     * @param otags 中间点位
     */
    @GetMapping(value = "/calculate/debug/tagId")
    public Result<String> setDebugCollectTags(@RequestParam String ctags, @RequestParam String otags) {
        try {
            redisTemplate.opsForValue().set(EmsConstantsX.J_CALC_DEBUG_C_TAGS, ctags);
            redisTemplate.opsForValue().set(EmsConstantsX.J_CALC_DEBUG_O_TAGS, otags);
            return Result.OK("设置采集中间调试点位成功");
        } catch (Exception e) {
            log.error("设置采集中间调试点位时:", e);
            return Result.error("设置采集中间调试点位时异常");
        }
    }

}
