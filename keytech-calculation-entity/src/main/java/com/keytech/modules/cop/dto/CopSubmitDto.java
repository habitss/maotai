package com.keytech.modules.cop.dto;

import lombok.Data;


/**
 * @ClassName: CopSubmitDto
 * @Description: cop填报内容
 * @Author: Jun
 * @Date: 2025/2/24 14:07
 **/
@Data
public class CopSubmitDto {
    private String id;

    /**工厂Code*/
    private String factoryCode;

    /**部门Code*/
    private String deptCode;

    /**产线Code*/
    private String lineCode;

    /**工段*/
    private String section;

    /**排期计划id*/
    private String copScheduleId;

    /**年*/
    private int planYear;

    /**月*/
    private int planMonth;

    /**周*/
    private String planWeek;

    /**检查日期*/
    private String clearDate;

    /**检查日期*/
    private String checkDate;

    /**数据json字符串*/
    private String dataJson;

    /**检查人*/
    private String clearBy;

    /**检查结果*/
    private int checkResult;

    /**核查人*/
    private String checkBy;

    /**核查结果备注*/
    private String checkRemark;
}
