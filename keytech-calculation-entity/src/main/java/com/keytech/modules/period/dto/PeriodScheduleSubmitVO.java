package com.keytech.modules.period.dto;

import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import lombok.Data;

import java.util.List;


/**
 * @author qipengbo
 * @version 1.0.0
 * @ClassName PeriodScheduleSubmitVO
 * @Description 核查项提交VO
 * @createTime 2025年03月13日 13:54:00
 */
@Data
public class PeriodScheduleSubmitVO {

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
    private String periodScheduleId;

    /**年*/
    private int planYear;

    /**月*/
    private int planMonth;

    /**周*/
    private int planWeek;

    /**检查日期*/
    private String checkDate;

    /**数据json字符串*/
    private String dataJson;

    /**检查人*/
    private String checkUser;

    /**检查结果*/
    private int checkResult;

    /**核查人*/
    private String verifyUser;

    /**核查结果*/
    private int verifyResult;

    /**核查结果备注*/
    private String verifyRemark;

    private List<ActionPlanTrack> actionPlanTrackList;


}
