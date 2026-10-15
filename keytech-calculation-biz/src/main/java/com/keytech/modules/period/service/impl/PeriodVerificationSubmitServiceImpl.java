package com.keytech.modules.period.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.period.dto.PeriodScheduleSubmitVO;
import com.keytech.modules.period.dto.ScheduleJson;
import com.keytech.modules.period.entity.PeriodVerificationSchedule;
import com.keytech.modules.period.entity.PeriodVerificationSubmit;
import com.keytech.modules.period.mapper.PeriodVerificationSubmitMapper;
import com.keytech.modules.period.service.IPeriodVerificationScheduleService;
import com.keytech.modules.period.service.IPeriodVerificationSubmitService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @Description: 期间排查排查内容提交
 * @Author: jeecg-boot
 * @Date:   2025-03-12
 * @Version: V1.0
 */
@Service
public class PeriodVerificationSubmitServiceImpl extends BaseServiceImpl<PeriodVerificationSubmitMapper, PeriodVerificationSubmit> implements IPeriodVerificationSubmitService {

    @Autowired
    private IPeriodVerificationScheduleService periodVerificationScheduleService;

    @Override
    public PeriodVerificationSubmit findSubmitByParam(PeriodScheduleSubmitVO periodScheduleSubmitVO) {
        return this.getOne(new LambdaQueryWrapper<PeriodVerificationSubmit>()
                .eq(PeriodVerificationSubmit::getPeriodScheduleId, periodScheduleSubmitVO.getPeriodScheduleId())
                .eq(PeriodVerificationSubmit::getPlanYear, periodScheduleSubmitVO.getPlanYear())
                .eq(PeriodVerificationSubmit::getPlanMonth, periodScheduleSubmitVO.getPlanMonth())
                .eq(PeriodVerificationSubmit::getPlanWeek, periodScheduleSubmitVO.getPlanWeek()));
    }

    @Override
    public void scheduleTask() {
        //读取当年排期计划
        int currentYear = LocalDate.now().getYear();
        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 定义周的开始（例如周一）和结束（例如周日）
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        // 获取当前日期是当年的第几周
        int weekOfYear = today.get(weekFields.weekOfWeekBasedYear());
        List<PeriodVerificationSchedule> periodVerificationSchedules = periodVerificationScheduleService.list(new LambdaQueryWrapper<PeriodVerificationSchedule>().eq(PeriodVerificationSchedule::getPlanYear, currentYear));

        //List<PeriodVerificationSubmit> submitList = this.list(new LambdaQueryWrapper<PeriodVerificationSubmit>().eq(PeriodVerificationSubmit::getPlanYear, currentYear));
        if (CollectionUtils.isNotEmpty(periodVerificationSchedules)) {
            for (PeriodVerificationSchedule periodVerificationSchedule : periodVerificationSchedules) {
                String weekJson = periodVerificationSchedule.getWeekJson();
                List<ScheduleJson> weekList = JSONArray.parseArray(weekJson, ScheduleJson.class);
                if (null != periodVerificationSchedule.getFrequency()) {
                    int frequency = Integer.parseInt(periodVerificationSchedule.getFrequency());
                        // 已填报信息
                        for (ScheduleJson scheduleJson : weekList) {
                                if ("4".equals(scheduleJson.getState()) && weekOfYear >= scheduleJson.getNum()) {
                                    if (frequency > 2 && frequency <= 6) {
                                        //需要在计划时间后4周内审核,未审核的直接置为不合格
                                        if (weekOfYear - scheduleJson.getNum() > 4) {
                                            scheduleJson.setState("0");
                                        }
                                    } else if (frequency == 2) {
                                        //需要在计划时间后2周内审核
                                        if (weekOfYear - scheduleJson.getNum() > 2) {
                                            scheduleJson.setState("0");
                                        }
                                    } else if (frequency == 1) {
                                        //需要在计划时间后1周内审核
                                        if (weekOfYear - scheduleJson.getNum() > 1) {
                                            scheduleJson.setState("0");
                                        }
                                    }
                                //未执行的周数
                                } else if ("-1".equals(scheduleJson.getState()) && weekOfYear >= scheduleJson.getNum()) {
                                    if (frequency > 2 && frequency <= 6) {
                                        //需要在计划时间后4周,直接置为未执行
                                        if (weekOfYear - scheduleJson.getNum() > 4) {
                                            scheduleJson.setState("5");
                                        } else if(weekOfYear - scheduleJson.getNum() < 4 && weekOfYear - scheduleJson.getNum() >= 1) {
                                            //在4周内设置为延迟
                                            scheduleJson.setState("2");
                                        }
                                    } else if (frequency == 2) {
                                        //需要在计划时间后2周内审核
                                        if (weekOfYear - scheduleJson.getNum() > 2) {
                                            scheduleJson.setState("5");
                                        } else if(weekOfYear - scheduleJson.getNum() == 1) {
                                            scheduleJson.setState("2");
                                        }
                                    } else if (frequency == 1) {
                                        //需要在计划时间后1周内
                                        if (weekOfYear - scheduleJson.getNum() > 1) {
                                            scheduleJson.setState("5");
                                        }
                                    }
                                }
                        }

                }
                weekJson = JSONArray.toJSONString(weekList);
                periodVerificationSchedule.setWeekJson(weekJson);
                periodVerificationScheduleService.updateById(periodVerificationSchedule);
            }
        }
    }
}
