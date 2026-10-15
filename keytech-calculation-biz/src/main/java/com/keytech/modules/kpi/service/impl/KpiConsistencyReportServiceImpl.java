package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.kpi.dto.ConsistencyDto;
import com.keytech.modules.kpi.entity.KpiConsistencyReport;
import com.keytech.modules.kpi.mapper.KpiConsistencyReportMapper;
import com.keytech.modules.kpi.service.IKpiConsistencyReportService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: 一致KPI数据表
 * @Author: jeecg-boot
 * @Date:   2026-03-24
 * @Version: V1.0
 */
@Service
public class KpiConsistencyReportServiceImpl extends BaseServiceImpl<KpiConsistencyReportMapper, KpiConsistencyReport> implements IKpiConsistencyReportService {

    @Override
    public List<ConsistencyDto> queryPageList(ConsistencyDto consistencyDto) {
        // week查询条件为空，返回空数据
        if (StrUtil.isBlank(consistencyDto.getWeek())) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<KpiConsistencyReport> queryWrapper = new LambdaQueryWrapper<>();
        // 工厂查询条件（前端传factoryCode参数）
        queryWrapper.eq(StrUtil.isNotBlank(consistencyDto.getFactoryCode()), KpiConsistencyReport::getFactory, consistencyDto.getFactoryCode());
        // 周日期查询条件（前端传week参数）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            queryWrapper.eq(KpiConsistencyReport::getWeekDate, sdf.parse(consistencyDto.getWeek()));
        } catch (ParseException e) {
            // 日期格式错误，返回空数据
            return new ArrayList<>();
        }
        // 按排序字段升序排列
        queryWrapper.orderByAsc(KpiConsistencyReport::getSort);

        List<KpiConsistencyReport> list = list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }

        // 转换为DTO
        List<ConsistencyDto> resultList = new ArrayList<>();
        for (KpiConsistencyReport report : list) {
            ConsistencyDto dto = new ConsistencyDto();
            BeanUtils.copyProperties(report, dto);
            dto.setFactoryCode(report.getFactory());
            dto.setFactory(null);
            if (report.getWeekDate() != null) {
                dto.setWeekDate(sdf.format(report.getWeekDate()));
            }
            resultList.add(dto);
        }
        return resultList;
    }

    @Override
    public int physicalDeleteByWeekDate(Date weekDate) {
        return baseMapper.physicalDeleteByWeekDate(weekDate);
    }

}
