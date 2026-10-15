package com.keytech.modules.action.plan.dto;

import lombok.Data;

/**
 * @Description: TODO
 * @Author: sjw
 * @Date: 2025/2/6 16:41
 */
@Data
public class ActionPlanSummaryQueryDto {
    /**
     * 检索对象 1:提出日期 2:计划完成日期 3:实际完成日期
     */
    private String objectType;

    /**
     * 日期检索 1:按月 2:按天 3:日期区间 4:年份
     */
    private String dateType;

    /**
     * 年份值
     */
    private String yearField;

    /**
     * 月度值
     */
    private String monthField;

    /**
     * 日期值
     */
    private String dayField;

    /**
     * 日期区间值-开始时间
     */
    private String dateRangeField_begin;

    /**
     * 日期区间值-结束时间
     */
    private String dateRangeField_end;

    /**
     * 问题分类id
     */
    private String problemTypeIds;

    /**
     * 部门id
     */
    private String deptIds;

    /**
     * 责任人或提出人 1:责任 2:提出
     */
    private String dutyOrPut;

    /**
     * 个人或部门 1:个人 2:部门
     */
    private String userOrDept;

    /**
     * 单独查询某个人
     */
    private String userId;
}
