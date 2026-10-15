package com.keytech.modules.kpi.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * @author she
 * @date 2023/03/29/19:05
 */
@Data
public class KpiTypeDto {

    private String factory;
    private String dept;

    private String id;
//    private Integer isomerism;
    private String orgCategory;
    //KPI名称
    private String kpiName;
    /**
     * 参数统一为这个 项目和kpi
     */
    private String perentId;
    //组织层级
    private String orgCode;
    /**
     * 开启排序
     */
    private Boolean enableSort =false;

    /**
     * 是否显示所有的KPI，不做当前用户部门权限控制
     */
    private Boolean isShowAll = false;

    /**
     * 排序类型
     * report 报表
     * input  录入
     */
    private String sortType = "";

    //当前页
    private long pageNo = 1;
    //每页数量
    private long pageSize = 10;
    //年
    private String year;
    //填写部门
    private String inputDeptQuery;
    //层级部门
    private String deptCode;


}
