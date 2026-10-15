package com.keytech.modules.alarm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.alarm.constants.AlarmStatusEnum;
import com.keytech.modules.alarm.entity.AlarmEvent;
import com.keytech.modules.alarm.mapper.AlarmEventMapper;
import com.keytech.modules.alarm.service.IAlarmEventService;
import com.keytech.modules.alarm.service.IAlarmPointService;
import com.keytech.modules.alarm.vo.AlarmCountVo;
import com.keytech.modules.ems.util.Java8DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报警事实
 * @author liu
 * @since 2026.01.12
 * */
@Service
public class AlarmEventServiceImpl extends BaseServiceImpl<AlarmEventMapper, AlarmEvent> implements IAlarmEventService {

    /**
     * 报警中心大屏-同比环比
     */
    @Override
    public Map<String, Object> bigScreenComparison(String factoryCode){
        Map<String, Object> resultMap = new HashMap<String, Object>();
        //1.日环比
        LocalDate today = LocalDate.now();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime now = LocalDateTime.now();
        Long todayCnt = baseMapper.selectCount(Wrappers.<AlarmEvent>lambdaQuery()
                .likeRight(StringUtils.isNotEmpty(factoryCode), AlarmEvent::getDeptCode, factoryCode)
                .between(AlarmEvent::getAlarmTime, Java8DateUtil.format1(today.atStartOfDay()), Java8DateUtil.format1(now)));
        Long yesterdayCnt = baseMapper.selectCount(Wrappers.<AlarmEvent>lambdaQuery()
                .likeRight(StringUtils.isNotEmpty(factoryCode), AlarmEvent::getDeptCode, factoryCode)
                .between(AlarmEvent::getAlarmTime, Java8DateUtil.format1(yesterday.atStartOfDay()), Java8DateUtil.format1(yesterday.atTime(LocalTime.MAX))));

        double dayCompareRate;
        // 日环比计算规则
        if (yesterdayCnt == 0 && todayCnt == 0) {
            // 都为 0：无变化
            dayCompareRate = 0d;
        } else if (yesterdayCnt == 0) {
            // 昨天 0，今天 >0：从无到有
            dayCompareRate = 100d;
        } else if (todayCnt == 0) {
            // 昨天 >0，今天 0：从有到无
            dayCompareRate = -100d;
        } else {
            // 正常环比
            dayCompareRate = BigDecimal.valueOf(todayCnt - yesterdayCnt)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(yesterdayCnt), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        resultMap.put("todayCnt", todayCnt);
        resultMap.put("yesterdayCnt", yesterdayCnt);
        resultMap.put("dayCompareRate", dayCompareRate);

        //2.月环比
        LocalDateTime startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfLastMonth = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
        Long monthCnt = baseMapper.selectCount(Wrappers.<AlarmEvent>lambdaQuery()
                .likeRight(StringUtils.isNotEmpty(factoryCode), AlarmEvent::getDeptCode, factoryCode)
                .between(AlarmEvent::getAlarmTime, Java8DateUtil.format1(startOfMonth), Java8DateUtil.format1(now)));
        Long lastMonthCnt = baseMapper.selectCount(Wrappers.<AlarmEvent>lambdaQuery()
                .likeRight(StringUtils.isNotEmpty(factoryCode), AlarmEvent::getDeptCode, factoryCode)
                .between(AlarmEvent::getAlarmTime, Java8DateUtil.format1(startOfLastMonth), Java8DateUtil.format1(endOfLastMonth)));

        double monthCompareRate;
        if (lastMonthCnt == 0 && monthCnt == 0) {
            // 都为 0：无变化
            monthCompareRate = 0d;
        } else if (lastMonthCnt == 0) {
            // 上月 0，本月 >0：从无到有
            monthCompareRate = 100d;
        } else if (monthCnt == 0) {
            // 上月 >0，本月 0：从有到无
            monthCompareRate = -100d;
        } else {
            // 正常环比
            monthCompareRate = BigDecimal.valueOf(monthCnt - lastMonthCnt)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(lastMonthCnt), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        resultMap.put("monthCnt", monthCnt);
        resultMap.put("lastMonthCnt", lastMonthCnt);
        resultMap.put("monthCompareRate", monthCompareRate);

        //3.年环比
        LocalDateTime startOfYear = now.with(TemporalAdjusters.firstDayOfYear()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastYear = now.minusYears(1).with(TemporalAdjusters.firstDayOfYear()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfLastYear = now.minusYears(1).with(TemporalAdjusters.lastDayOfYear()).withHour(23).withMinute(59).withSecond(59);
        Long yearCnt = baseMapper.selectCount(Wrappers.<AlarmEvent>lambdaQuery()
                .likeRight(StringUtils.isNotEmpty(factoryCode), AlarmEvent::getDeptCode, factoryCode)
                .between(AlarmEvent::getAlarmTime, Java8DateUtil.format1(startOfYear), Java8DateUtil.format1(now)));
        Long lastYearCnt = baseMapper.selectCount(Wrappers.<AlarmEvent>lambdaQuery()
                .likeRight(StringUtils.isNotEmpty(factoryCode), AlarmEvent::getDeptCode, factoryCode)
                .between(AlarmEvent::getAlarmTime, Java8DateUtil.format1(startOfLastYear), Java8DateUtil.format1(endOfLastYear)));

        double yearCompareRate;
        if (lastYearCnt == 0 && yearCnt == 0) {
            // 都为 0：无变化
            yearCompareRate = 0d;
        } else if (lastYearCnt == 0) {
            // 去年 0，今年 >0：从无到有
            yearCompareRate = 100d;
        } else if (yearCnt == 0) {
            // 去年 >0，今年 0：从有到无
            yearCompareRate = -100d;
        } else {
            // 正常同比/环比
            yearCompareRate = BigDecimal.valueOf(yearCnt - lastYearCnt)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(lastYearCnt), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        resultMap.put("yearCnt", yearCnt);
        resultMap.put("lastYearCnt", lastYearCnt);
        resultMap.put("yearCompareRate", yearCompareRate);

        return resultMap;
    }

    /**
     * 报警中心大屏-报警类型
     * 按报警类型统计当日的报警数量
     */
    @Override
    public List<AlarmCountVo> bigScreenAlarmType(String factoryCode){
        LocalDateTime stime = null;
        LocalDateTime etime = null;
        List<AlarmCountVo> resultList = baseMapper.selectCountByAlarmCategory(factoryCode, stime, etime);
        return resultList;
    }

    /**
     * 报警中心大屏-设备异常排名TOP5
     */
    @Override
    public List<AlarmCountVo> bigScreenDeviceTopFive(String factoryCode){
        LocalDateTime stime = null;
        LocalDateTime etime = null;
        List<AlarmCountVo> resultList = baseMapper.selectCountByDevice(factoryCode, stime, etime, "cnt DESC", 5);
        return resultList;
    }

    /**
     * 报警中心大屏-报警分析
     */
    @Override
    public List<AlarmCountVo> bigScreenStatus(String factoryCode){
        LocalDateTime stime = null;
        LocalDateTime etime = null;
        QueryWrapper<AlarmEvent> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("status, COUNT(id) AS cnt")
                .ge(stime != null, "alarm_time", stime)
                .lt(etime != null, "alarm_time", etime)
                .likeRight(StringUtils.isNotEmpty(factoryCode), "dept_code", factoryCode)
                .groupBy("status");
        List<AlarmEvent> alarmEventList = baseMapper.selectList(queryWrapper);
        List<AlarmCountVo> resultList = alarmEventList.stream().map(item->{
            AlarmCountVo vo = new AlarmCountVo();
            vo.setItem(String.valueOf(item.getStatus()));
            vo.setCnt(item.getCnt());
            return vo;
        }).collect(Collectors.toList());
        return resultList;
    }

}
