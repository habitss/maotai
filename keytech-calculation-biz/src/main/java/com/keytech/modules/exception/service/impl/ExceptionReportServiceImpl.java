package com.keytech.modules.exception.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.cop.utils.DateUtils;
import com.keytech.modules.exception.dto.ExceptionReportVO;
import com.keytech.modules.exception.dto.ExceptionTotalVO;
import com.keytech.modules.exception.dto.ReportViewDTO;
import com.keytech.modules.exception.entity.ExceptionReport;
import com.keytech.modules.exception.mapper.ExceptionReportMapper;
import com.keytech.modules.exception.service.IExceptionReportService;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.service.IMdmDictService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @Description: 异常处理报告
 * @Author: jeecg-boot
 * @Date:   2025-03-26
 * @Version: V1.0
 */
@Service
public class ExceptionReportServiceImpl extends BaseServiceImpl<ExceptionReportMapper, ExceptionReport> implements IExceptionReportService {

    @Autowired
    private IMdmDictService mdmDictService;
    @Autowired
    private ISysDepartService sysDepartService;

    private LambdaQueryWrapper<ExceptionReport> buildQueryWrapper(ExceptionReportVO exceptionReport) {
        LambdaQueryWrapper<ExceptionReport> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(exceptionReport.getTimes())) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String[] times = exceptionReport.getTimes().split(",");
            try {
                queryWrapper.between(ExceptionReport::getExceptionTime, sdf.parse(times[0]), sdf.parse(times[1]));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
            queryWrapper.between(ExceptionReport::getExceptionTime, DateUtils.getFirstDayOfYear(DateUtils.getCurrentYear()), DateUtils.getLastDayOfYear(DateUtils.getCurrentYear()));
        }
        if (StringUtils.isNotEmpty(exceptionReport.getDeptCode())) {
            queryWrapper.eq(ExceptionReport::getDeptCode, exceptionReport.getDeptCode());
        }
        if (StringUtils.isNotEmpty(exceptionReport.getLineCode())) {
            queryWrapper.eq(ExceptionReport::getLineCode, exceptionReport.getLineCode());
        }
        if (StringUtils.isNotEmpty(exceptionReport.getTrigger())) {
            queryWrapper.eq(ExceptionReport::getTriggerConditions, exceptionReport.getTrigger());
        }
        return queryWrapper;
    }

    @Override
    public List<ReportViewDTO> reportView(ExceptionReportVO exceptionReport) {
        LocalDate now = LocalDate.now();
        if(CollectionUtils.isEmpty(exceptionReport.getTimeRange())){
            exceptionReport.setTimes(DateUtils.getFirstDayOfYear(now.getYear()) + "," + DateUtils.getLastDayOfYear(now.getYear()));
        } else {
            exceptionReport.setTimes(exceptionReport.getTimeRange().get(0) + "," + exceptionReport.getTimeRange().get(1));
        }
        LambdaQueryWrapper<ExceptionReport> queryWrapper = buildQueryWrapper(exceptionReport);
        List<ExceptionReport> list = this.list(queryWrapper);
        List<ReportViewDTO> result = new ArrayList<>();
        // 按照产线分组和计数
        if(exceptionReport.getType() ==1) {
            result = groupAndCount(list, ExceptionReport::getLineCode,exceptionReport.getType());
        } else if(exceptionReport.getType() ==2) {
            // 按照异常条件分组和计数
            result = groupAndCount(list, ExceptionReport::getTriggerConditions,exceptionReport.getType());
        } else {
            // 按照问题类型分组和计数
            result = groupAndCount(list, ExceptionReport::getExceptionType,exceptionReport.getType());
        }
        return result;
    }

    @Override
    public List<ExceptionTotalVO> reportStatistics(ExceptionReportVO exceptionReport) {
        LambdaQueryWrapper<ExceptionReport> queryWrapper = buildQueryWrapper(exceptionReport);
        List<ExceptionReport> list = this.list(queryWrapper);
        List<ExceptionTotalVO> result = new ArrayList<>();
        //按照异常条件分组
        Map<String, List<ExceptionReport>> triggerGroups = list.stream()
                .collect(Collectors.groupingBy(ExceptionReport::getTriggerConditions));
        triggerGroups.forEach((key, value) -> {
            ExceptionTotalVO view = new ExceptionTotalVO();
            view.setName(lineName(key));
            view.setValue(value.size());
            result.add(view);
        });
        return result;
    }

    private List<ReportViewDTO> groupAndCount(List<ExceptionReport> list, Function<ExceptionReport, Object> groupByFunction,int type) {
        Map<Object, List<ExceptionReport>> groups = list.stream()
                .collect(Collectors.groupingBy(groupByFunction));

        List<ReportViewDTO> result = new ArrayList<>();
                groups.forEach((key, value) -> {
            Map<Integer, Long> countMap = value.stream()
                    .collect(Collectors.groupingBy(ExceptionReport::getMonth, Collectors.counting()));
            Map<Integer, Long> map = IntStream.rangeClosed(1, 12)
                            .boxed()
                            .collect(Collectors.toMap(i -> i, i -> countMap.getOrDefault(i, 0L)));
            ReportViewDTO view = new ReportViewDTO();
            if(type == 1){
                view.setType(deptName(String.valueOf(key)));
            } else if(type == 2){
                view.setType(lineName(String.valueOf(key)));
            } else {
                view.setType(typeName((Integer) key));
            }
            long totalCount = 0;
                    for (Map.Entry<Integer, Long> entry : map.entrySet()) {
                        Integer month = entry.getKey();
                        Long count = entry.getValue();
                        totalCount += count;
                        if (month == 1) {
                            view.setJan(count);
                        } else if (month == 2) {
                            view.setFeb(count);
                        } else if (month == 3) {
                            view.setMar(count);
                        } else if (month == 4) {
                            view.setApr(count);
                        } else if (month == 5) {
                            view.setMay(count);
                        } else if (month == 6) {
                            view.setJun(count);
                        } else if (month == 7) {
                            view.setJul(count);
                        } else if (month == 8) {
                            view.setAug(count);
                        } else if (month == 9) {
                            view.setSep(count);
                        } else if (month == 10) {
                            view.setOct(count);
                        } else if (month == 11) {
                            view.setNov(count);
                        } else if (month == 12) {
                            view.setDec(count);
                        }
                    }
                    view.setTotal(totalCount);
                    result.add(view);
        });

        return result;
    }

    private String deptName(String deptCode) {
        // TODO: 根据部门代码获取部门名称
        SysDepart sysDepart = sysDepartService.getOne(new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgCode, deptCode));
        if(sysDepart!= null) {
            return sysDepart.getDepartName();
        }
        return null;
    }

    private String lineName(String lineCode) {
        List<MdmDict> dict =mdmDictService.getDictAllOrByGroupCode(Collections.singletonList("MES3_EXCEPTION_TRRIGER_CONDITION"));
        if(dict!= null) {
            return dict.stream().filter(d -> d.getItemVal().equals(lineCode)).findFirst().get().getItemKey();
        }
        return null;
    }

    private String typeName(int type) {
        // TODO: 根据部门代码获取部门名称
       if(type == 1) {
           return "操作问题";
       } else if (type == 2){
           return "质量问题";
       } else if (type == 3){
           return "维护问题";
       } else {
           return "";
       }
    }

}
