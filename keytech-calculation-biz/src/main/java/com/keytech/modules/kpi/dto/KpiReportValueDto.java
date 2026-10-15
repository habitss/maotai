package com.keytech.modules.kpi.dto;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Kpi录入/报表查询接收对象
 */
@Data
public class KpiReportValueDto {
    /**工厂*/
    private String factory;
    /**部门*/
    private String dept;
    /**类型id(分类id)*/
    private String typeId;
    /**年月*/
    private String month;
    /**年*/
    private Integer year;
    /**层级部门*/
    private String deptQuery;
    /**层级部门类型(0当前层级/1下级)*/
    private String deptQueryType;
    /**排序标识(report日报, input录入)*/
    private String typeSort;
    /**是否在填录入中显示*/
    private Integer visibleWrite;
    /**是否在日报中显示*/
    private Integer visible;
    /**是否显示二级KPI*/
    Boolean showChildrenKpi = false;
    /**层级(分类)*/
    private String reportId;
    /**KPICode*/
    private String kpiCode;
    /**KPI名称*/
    private String kpiName;
    /**KPI填报部门(查询条件)*/
    private String inputDeptQuery;
    /**KPI标签*/
    private String label;
    /**月集合*/
    List<String> months;
    /**是否显示周*/
    Boolean showWeek = false;
    /**是否显示KPI职责分工*/
    Boolean showKpiDuty = false;
    /**月集合*/
    private List<Integer> monthList;

    /**目标值-存储数据集合*/
    private List<Map<String, Object>> targetDatas;
    /** 年份集合 */
    List<Integer> years;
    /**重复引用功能使用，kpiCode*/
    private String citeKpiCode;


    public List<String> getMonths() {
        if (CollUtil.isEmpty(months)) {
            List<String> monthStrList = Lists.newArrayList();
            for (int i = 1; i <= 12; i++) {
                String monthStr = StrUtil.padPre(i + "", 2, "0");
                monthStrList.add(year + "-" + monthStr);
            }
            this.months = monthStrList;
        } else {
            //排序
            this.months = months.stream().sorted().collect(Collectors.toList());
        }
        return months;
    }
}
