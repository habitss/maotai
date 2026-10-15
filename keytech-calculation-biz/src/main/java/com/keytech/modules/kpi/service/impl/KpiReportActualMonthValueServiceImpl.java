package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.entity.KpiReportActualMonthValue;
import com.keytech.modules.kpi.entity.KpiReportTargetValue;
import com.keytech.modules.kpi.mapper.KpiReportActualMonthValueMapper;
import com.keytech.modules.kpi.service.IKpiReportActualMonthValueService;
import com.keytech.modules.kpi.service.IKpiReportTargetValueService;
import com.keytech.modules.kpi.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description: kpi报表实际值月表
 * @Author: jeecg-boot
 * @Date:   2025-02-24
 * @Version: V1.0
 */
@Service
public class KpiReportActualMonthValueServiceImpl extends BaseServiceImpl<KpiReportActualMonthValueMapper, KpiReportActualMonthValue> implements IKpiReportActualMonthValueService {

    @Autowired
    private IKpiReportTargetValueService targetValueService;

    @Override
    public void saveOrUpdateBy(KpiReportActualMonthValue monthValue, String compareValue) {
        //处理颜色对比
        //获取目标值
        KpiReportTargetValue targetValue = targetValueService.queryTargetValueBy(monthValue.getReportId(), monthValue.getYear(), monthValue.getKpiCode(),
                monthValue.getFactoryCode(), null);
        //没色
        int colour = 0;
        if (Objects.nonNull(targetValue)) {
            String monthStr = monthValue.getMonth() + "";
            String monthValueTarget = targetValue.getKpiTypeMonthValue();
            JSONObject targetObject = JSONUtil.parseObj(monthValueTarget);
            String value = monthValue.getValue();
            if (monthStr.length() > 4 && "1".equals(monthValue.getIndicator()+"")) {
                //月 ******
                Map<String, String> monthMap = targetValueService.monthMap(false);
                String key = monthStr.substring(4);
                String str = targetObject.getStr(monthMap.get(key));
                colour = targetValueService.getColour(targetObject, value, str, compareValue);
            } else if("3".equals(monthValue.getIndicator()+"")){
                // 年 先按照当前某月年累
                //String month = monthValue.getYear()+"-"+(monthStr.length() < 2?"0"+monthStr:monthStr);
                String yearMonth = monthStr.substring(0,4)+"-"+monthStr.substring(4,6);
                String yearTarget = targetObject.getStr(yearMonth+"-yearAccumulate");
                if (StrUtil.isEmpty(yearTarget)) {
                    yearTarget = targetObject.getStr("yearTarget");
                }
                colour = targetValueService.getColour(targetObject, value, yearTarget, compareValue);
                //monthValue.setMonth(NumberUtil.parseInt(month.replace("-","")));
            }else{
                // 年的 先按照当前年累来取
                String yearTarget = targetObject.getStr("currentYearTarget");
                if (StrUtil.isEmpty(yearTarget)) {
                    yearTarget = targetObject.getStr("yearTarget");
                }
                colour = targetValueService.getColour(targetObject, value, yearTarget, compareValue);
            }
            monthValue.setExtJson(targetObject.toString());
        }
        monthValue.setColour(colour);
        LambdaUpdateWrapper<KpiReportActualMonthValue> updateWrapper = Wrappers.lambdaUpdate(KpiReportActualMonthValue.class);
        updateWrapper.eq(KpiReportActualMonthValue::getYear, monthValue.getYear());
        updateWrapper.eq(KpiReportActualMonthValue::getMonth, monthValue.getMonth());
        updateWrapper.eq(KpiReportActualMonthValue::getIndicator, monthValue.getIndicator());//统计指标
//        updateWrapper.eq(StrUtil.isNotBlank(monthValue.getReportId()), MdmKpiReportActualMonthValue::getReportId, monthValue.getReportId());
        updateWrapper.eq(KpiReportActualMonthValue::getStartTime, DateUtil.format(monthValue.getStartTime(), "yyyy-MM-dd"));
        updateWrapper.eq(KpiReportActualMonthValue::getEndTime, DateUtil.format(monthValue.getEndTime(), "yyyy-MM-dd"));
//        updateWrapper.eq(MdmKpiReportActualMonthValue::getActualType, actualType);  容易异构和非异构切换
        updateWrapper.eq(KpiReportActualMonthValue::getKpiCode, monthValue.getKpiCode());
        saveOrUpdate(monthValue, updateWrapper);
    }

    /**
     * 获取年月数据
     * @param dto
     * @param kpiCodeIds
     * @return
     */
    @Override
    public List<KpiReportActualMonthValue> listMonthDataBy(KpiReportValueDto dto, List<String> kpiCodeIds) {
        String dept = dto.getDept();
        String factory = dto.getFactory();
        String orgCode = dto.getReportId();
        int year = dto.getYear();
        LambdaQueryWrapper<KpiReportActualMonthValue> lambdaQuery = Wrappers.lambdaQuery(KpiReportActualMonthValue.class);
//        lambdaQuery.eq(StrUtil.isNotBlank(orgCode), MdmKpiReportActualMonthValue::getReportId, orgCode);
        lambdaQuery.eq(KpiReportActualMonthValue::getYear, year);
        lambdaQuery.in(CollUtil.isNotEmpty(kpiCodeIds), KpiReportActualMonthValue::getKpiCode, kpiCodeIds);
        lambdaQuery.likeRight(StrUtil.isNotBlank(factory), KpiReportActualMonthValue::getFactoryCode, factory);
//        lambdaQuery.likeRight(StrUtil.isNotBlank(dept), MdmKpiReportActualMonthValue::getDept, dept);
        //月份
        List<String> months = dto.getMonths();
        lambdaQuery.and(wrapper -> {
            for (String monthStr : months) {
                String dateStr = StrUtil.replace(monthStr, "-", "");
                int month = NumberUtil.parseInt(dateStr);
                //月
                wrapper.or(wq -> wq.eq(KpiReportActualMonthValue::getMonth, month));
            }
            //年统计
//            wrapper.or(wq -> wq.eq(MdmKpiReportActualMonthValue::getIndicator, 2));
            for (String monthStr : months) {
                String dateStr = StrUtil.replace(monthStr, "-", "");
                int month = NumberUtil.parseInt(dateStr);
                wrapper.or(wq -> wq.eq(KpiReportActualMonthValue::getIndicator, 3).eq(KpiReportActualMonthValue::getMonth, month));
            }
        });
        lambdaQuery.orderBy(true, false, Lists.newArrayList(KpiReportActualMonthValue::getKpiCode, KpiReportActualMonthValue::getCreateTime));

        return list(lambdaQuery);
    }


    /**
     * 获取年月数据指定年份
     * @param dto
     * @param kpiCodeIds
     * @return
     */
    @Override
    public List<KpiReportActualMonthValue> listMonthDataByYear(KpiReportValueDto dto, List<String> kpiCodeIds) {
        String factory = dto.getFactory();
        //年份集合
        List<Integer> years = dto.getYears();
        LambdaQueryWrapper<KpiReportActualMonthValue> lambdaQuery = Wrappers.lambdaQuery(KpiReportActualMonthValue.class);
        lambdaQuery.in(KpiReportActualMonthValue::getYear, years)
                .in(CollUtil.isNotEmpty(kpiCodeIds), KpiReportActualMonthValue::getKpiCode, kpiCodeIds)
                .likeRight(StrUtil.isNotBlank(factory), KpiReportActualMonthValue::getFactoryCode, factory);
        //月份
        //List<String> months = dto.getMonths();
        List<String> yearMonths = DateUtils.generateYearMonthListStream(years);
        lambdaQuery.and(wrapper -> {
            for (String monthStr : yearMonths) {
                String dateStr = StrUtil.replace(monthStr, "-", "");
                int month = NumberUtil.parseInt(dateStr);
                //月份
                wrapper.or(wq -> wq.eq(KpiReportActualMonthValue::getMonth, month));
            }
            //年统计
            for (String monthStr : yearMonths) {
                String dateStr = StrUtil.replace(monthStr, "-", "");
                int month = NumberUtil.parseInt(dateStr);
                wrapper.or(wq -> wq.eq(KpiReportActualMonthValue::getIndicator, 3).eq(KpiReportActualMonthValue::getMonth, month));
            }
        });
        lambdaQuery.orderBy(true, false, Lists.newArrayList(KpiReportActualMonthValue::getKpiCode, KpiReportActualMonthValue::getCreateTime));
        return list(lambdaQuery);
    }

}
