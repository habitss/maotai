package com.keytech.modules.opl.vo;

import lombok.Data;

/**
 * @date 2025-09-04
 * 工厂Opl统计
 */
@Data
public class OplManageVo {
    /** 对应SysDepart中的id字段,前端数据树中的key*/
    private String key;
    //部门
    private String title;
    //部门code
    private String dept;
    //总Opl数
    private Integer total;


    //部门opl诊断
    //opl完成
    private Integer oplSuccess;
    //opl计划
    private Integer oplPlan;
    //opl延迟
    private Integer oplDelay;


    // 工厂opl管理跟踪
    //计划回顾数
    private Integer planReview;
    //实际回顾数
    private Integer realityReview;
    //回顾完成率
    private String reviewRate;
    //月-计划回顾数
    private Integer monthPlanReview;
    //月-实际回顾数
    private Integer monthRealityReview;
    //月-回顾完成率
    private String monthReviewRate;
}
