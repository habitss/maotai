package com.keytech.modules.period.dto;

import lombok.Data;

/**
 * @author qipengbo
 * @version 1.0.0
 * @ClassName PeriodScheduleVO
 * @Description 核查计划VO
 * @createTime 2025年03月11日 13:43:00
 */
@Data
public class PeriodScheduleVO {

    private String id;

    private String factoryCode;

    private String deptCode;

    private String lineCode;

    private String section;

    private String apparatusId;

    private String apparatusName;

    private String frequency;

    private String checkUser;

    private String verifyUser;

    private String planYear;

    private int months;

    private String date;

    private int num;

    private int quarter;

    private int halfYear;

    private Long delFlag;

    private int result;

    private String status;

    //根据类型判断是操作当前周（0）还是操作后续所有（1）
    private int type;

}
