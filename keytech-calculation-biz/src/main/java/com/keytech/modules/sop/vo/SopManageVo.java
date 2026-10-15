package com.keytech.modules.sop.vo;

import lombok.Data;

/**
 * @author hq
 * @date 2025/04/21/
 * 工厂SOP统计
 */
@Data
public class SopManageVo {
    /** 对应SysDepart中的id字段,前端数据树中的key*/
    private String key;
    //部门
    private String title;
    //部门code
    private String dept;
    //总SOP数
    private Integer total;


    //部门sop诊断
    //sop完成
    private Integer sopSuccess;
    //sop计划
    private Integer sopPlan;
    //sop延迟
    private Integer sopDelay;


    // 工厂sop管理跟踪
    //计划回顾数
    private Integer planReview;
    //实际回顾数
    private Integer realityReview;
    //回顾完成率
    private String reviewRate;
    //月-计划回顾数
    private Integer ninePlanReview;
    //月-实际回顾数
    private Integer nineRealityReview;
    //月-回顾完成率
    private String nineReviewRate;
}
