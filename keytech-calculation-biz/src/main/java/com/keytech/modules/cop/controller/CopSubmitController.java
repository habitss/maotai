package com.keytech.modules.cop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.cop.dto.CopSubmitDto;
import com.keytech.modules.cop.entity.CopSchedule;
import com.keytech.modules.cop.entity.CopSubmit;
import com.keytech.modules.cop.service.ICopScheduleService;
import com.keytech.modules.cop.service.ICopSubmitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * cop填报 前端控制器
 * </p>
 *
 * @author lijun
 * @since 2025-02-20 02:19:51
 */
@Tag(name = "COP填报")
@Slf4j
@RestController
@RequestMapping("/cop/copSubmit")
public class CopSubmitController extends BaseController<ICopSubmitService, CopSubmit> {
    @Autowired
    private ICopSubmitService iCopSubmitService;
    @Autowired
    private ICopScheduleService iCopScheduleService;

    @AutoLog(value = "COP填报-结果查询")
    @Operation(summary = "COP计划排期-分页列表查询", description = "COP计划排期-分页列表查询")
    @GetMapping("/getScheduleResult")
    public Result<List<CopSubmitDto>> getScheduleResult(@RequestParam(name = "lineCode") String lineCode,
                                                @RequestParam(name = "copScheduleId",required = false) String copScheduleId,
                                                @RequestParam(name = "copPointId") String copPointId,
                                                @RequestParam(name = "planYear") Integer planYear,
                                                @RequestParam(name = "months") String months,
                                                @RequestParam(name = "planWeek") String planWeek) {
        List<String> copScheduleIds = null;
        if (StringUtils.isNotBlank(copPointId)) {
            List<CopSchedule> copSchedules = iCopScheduleService.list(new LambdaQueryWrapper<CopSchedule>().select(CopSchedule::getId).eq(CopSchedule::getCopPointId, copPointId));
            if (CollectionUtils.isNotEmpty(copSchedules)) {
                copScheduleIds = copSchedules.stream().map(CopSchedule::getId).collect(Collectors.toList());
            }else {
                return Result.OK(new ArrayList<>());
            }
        }
        List<Integer> monthList = Arrays.stream(months.split(",")).map(Integer::valueOf).collect(Collectors.toList());
        LambdaQueryWrapper<CopSubmit> copSubmitLambdaQueryWrapper = new LambdaQueryWrapper<CopSubmit>()
                .eq(CopSubmit::getLineCode, lineCode)
                .eq(StringUtils.isNotBlank(copScheduleId), CopSubmit::getCopScheduleId, copScheduleId)
                .eq(CopSubmit::getPlanYear, planYear)
                .eq(CopSubmit::getPlanWeek, planWeek)
                .in(CopSubmit::getPlanMonth, monthList)
                .in(CollectionUtils.isNotEmpty(copScheduleIds), CopSubmit::getCopScheduleId, copScheduleIds);
        List<CopSubmit> list = iCopSubmitService.list(copSubmitLambdaQueryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            return Result.OK(iCopSubmitService.initSubmitJson(list));
        }
        return Result.OK(new ArrayList<>());
    }

    /**
     *   添加
     *
     * @param scheduleSubmit
     * @return
     */
    @AutoLog(value = "COP填报-结果内容提交-添加")
    @Operation(summary="COP填报-结果内容提交-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody CopSubmitDto scheduleSubmit) {
        CopSubmit copSubmit =iCopSubmitService.findSubmitByParam(scheduleSubmit);
        if(null == copSubmit){
            copSubmit = new CopSubmit();
        }
        BeanUtils.copyProperties(scheduleSubmit, copSubmit);
//        if(StringUtils.isNotEmpty(copSubmit.getId())){
//            copSubmit =iCopSubmitService.getById(copSubmit.getId());
//        }
        if(StringUtils.isNotEmpty(scheduleSubmit.getClearDate())){
            try {
                LocalDate localDate = LocalDate.parse(scheduleSubmit.getClearDate().substring(0,10));
                ZoneId zoneId = ZoneId.systemDefault();
                ZonedDateTime zonedDateTime = localDate.atStartOfDay(zoneId);
                Date date = Date.from(zonedDateTime.toInstant());
                copSubmit.setClearDate(date);
            } catch (Exception e) {
                log.error("日期格式错误，请检查日期格式yyyy-MM-dd HH:mm:ss");
            }
        }
        copSubmit.setPlanWeek(scheduleSubmit.getPlanWeek());
        if(StringUtils.isBlank(scheduleSubmit.getClearBy())){
            copSubmit.setClearBy(null);
        }
        if(StringUtils.isBlank(scheduleSubmit.getCheckBy())){
            copSubmit.setCheckBy(null);
        }
        copSubmit.setCheckResult(String.valueOf(scheduleSubmit.getCheckResult()));
        if(0 != scheduleSubmit.getCheckResult()){
            copSubmit.setCheckDate(new Date());
        }
        iCopSubmitService.saveOrUpdate(copSubmit);
        CopSchedule copSchedule = iCopScheduleService.getById(scheduleSubmit.getCopScheduleId());
        if(null != copSchedule) {
            int frequency = Integer.parseInt(copSchedule.getFrequency());
            if (scheduleSubmit.getCheckResult() == 1) {
                if (null != copSubmit.getCheckDate()) {
                    Instant instant = copSubmit.getClearDate().toInstant();
                    LocalDate checkDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                    // 定义周的开始（例如周一）和结束（例如周日）
                    WeekFields weekFields = WeekFields.of(Locale.getDefault());
                    // 获取当前日期是当年的第几周
                    int weekOfYear = checkDate.get(weekFields.weekOfWeekBasedYear());
                    //判断处理时间是否在计划时间内
                    int planWeek = Integer.parseInt(scheduleSubmit.getPlanWeek());
                    String state = "1";
                    if (planWeek == weekOfYear) {
                        iCopScheduleService.processCopSchedule(scheduleSubmit.getCopScheduleId(), Integer.parseInt(scheduleSubmit.getPlanWeek()), state);
                    } else {
                        if (frequency > 2) {
                            //需要在计划时间后4周内审核
                            if (weekOfYear - planWeek > 4) {
                                state = "3";
                            }
                        } else if (frequency == 2) {
                            //需要在计划时间后2周内审核
                            if (weekOfYear - planWeek > 2) {
                                state = "3";
                            }
                        } else if (frequency == 1) {
                            state = "3";
                        }
                        iCopScheduleService.processCopSchedule(scheduleSubmit.getCopScheduleId(), Integer.parseInt(scheduleSubmit.getPlanWeek()), state);
                    }
                }
            } else if (scheduleSubmit.getCheckResult() == 2) {
                iCopScheduleService.processCopSchedule(scheduleSubmit.getCopScheduleId(), Integer.parseInt(scheduleSubmit.getPlanWeek()), "0");
            } else {
                iCopScheduleService.processCopSchedule(scheduleSubmit.getCopScheduleId(), Integer.parseInt(scheduleSubmit.getPlanWeek()), "4");
            }
        }

        return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
    }

    /**
     * 通过年月周查询
     *
     * @param copSubmitDto
     * @return
     */
    @Operation(summary="COP填报-结果内容提交-通过年月周查询")
    @PostMapping(value = "/queryByParam")
    public Result<CopSubmit> queryByParam(@RequestBody CopSubmitDto copSubmitDto) {
        CopSubmit copSubmit = iCopSubmitService.findSubmitByParam(copSubmitDto);
        return Result.OK(copSubmit);
    }

    /**
     * 定时任务
     * @return
     */
    @AutoLog(value = "COP填报-定时任务")
    @Operation(summary="COP填报-定时任务")
    @GetMapping(value = "/copSubmitByJob")
    public Result<String> updateDataByJob() {
        iCopSubmitService.scheduleTask();
        return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
    }

}
