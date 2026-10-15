package com.keytech.modules.cop.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.cop.dto.CopSubmitDto;
import com.keytech.modules.cop.dto.CopSubmitJson;
import com.keytech.modules.cop.entity.CopSchedule;
import com.keytech.modules.cop.entity.CopSubmit;
import com.keytech.modules.cop.mapper.CopSubmitMapper;
import com.keytech.modules.cop.service.ICopScheduleService;
import com.keytech.modules.cop.service.ICopSubmitService;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.cop.utils.DateUtils;
import com.keytech.modules.period.dto.ScheduleJson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * <p>
 * cop填报 服务实现类
 * </p>
 *
 * @author lijun
 * @since 2025-02-20 02:19:51
 */
@Slf4j
@Service
public class CopSubmitServiceImpl extends BaseServiceImpl<CopSubmitMapper, CopSubmit> implements ICopSubmitService {

    @Autowired
    private ICopScheduleService copScheduleService;

    @Override
    public List<CopSubmitJson> initSubmitJsonList(CopSubmit copSubmit) throws JeecgBootException {
        List<CopSubmitJson> copSubmitJsons;
        try {
            copSubmitJsons = JSONArray.parseArray(copSubmit.getDataJson(), CopSubmitJson.class);
        }catch (Exception e){
            log.error("dataJson转换异常--copSchedule:{}",copSubmit.toString(),e);
            throw new JeecgBootException("dataJson转换异常");
        }
        return copSubmitJsons;
    }

    @Override
    public List<CopSubmitDto> initSubmitJson(List<CopSubmit> list) {
        List<CopSubmitDto> result = new ArrayList<>();
        for (CopSubmit copSubmit : list) {
            CopSubmitDto copSubmitDto = new CopSubmitDto();
            BeanUtils.copyProperties(copSubmit,copSubmitDto);
//            List<CopSubmitJson> copSubmitJsons = this.initSubmitJsonList(copSubmit);
            //copSubmitDto.setDataJson(copSubmitJsons);
            result.add(copSubmitDto);
        }
        return result;
    }

    @Override
    public CopSubmit findSubmitByParam(CopSubmitDto copSubmitDto) {
        return this.getOne(new LambdaQueryWrapper<CopSubmit>()
                .eq(CopSubmit::getCopScheduleId, copSubmitDto.getCopScheduleId())
                .eq(CopSubmit::getPlanYear, copSubmitDto.getPlanYear())
                .eq(CopSubmit::getPlanMonth, copSubmitDto.getPlanMonth())
                .eq(CopSubmit::getPlanWeek, copSubmitDto.getPlanWeek()));
    }

    @Override
    public void scheduleTask() {
        int currentYear = DateUtils.getCurrentYear();
        int weekOfYear = DateUtils.getDayOfWeek();
        List<CopSchedule> copSchedules = copScheduleService.list(new LambdaQueryWrapper<CopSchedule>().eq(CopSchedule::getPlanYear, currentYear));
        if (CollectionUtils.isNotEmpty(copSchedules)) {
            for (CopSchedule copSchedule : copSchedules) {
                String weekJson = copSchedule.getWeekJson();
                List<ScheduleJson> weekList = JSONArray.parseArray(weekJson, ScheduleJson.class);
                if (null != copSchedule.getFrequency()) {
                    int frequency = Integer.parseInt(copSchedule.getFrequency());
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
                copSchedule.setWeekJson(weekJson);
                copScheduleService.updateById(copSchedule);
            }
        }
    }
}
