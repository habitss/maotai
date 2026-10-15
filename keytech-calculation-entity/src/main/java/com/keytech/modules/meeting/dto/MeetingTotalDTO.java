package com.keytech.modules.meeting.dto;

import lombok.Data;

@Data
public class MeetingTotalDTO {

    //单位
    private String dept;

    //会议频率
    private int type;

    //应召开次数
    private int all_count_day;

    //计划召开数
    private int plan_count_day;

    //实际召开数
    private int open_count_day;

    //取消次数
    private int canceled_count_day;

    //评估次数
    private int record_count_day;

    //会议召开率
    private String range_count_day;

    //应召开次数
    private int all_count_week;

    //计划召开数
    private int plan_count_week;

    //实际召开数
    private int open_count_week;

    //取消次数
    private int canceled_count_week;

    //评估次数
    private int record_count_week;

    //会议召开率
    private String range_count_week;

    //应召开次数
    private int all_count_month;

    //计划召开数
    private int plan_count_month;

    //实际召开数
    private int open_count_month;

    //取消次数
    private int canceled_count_month;

    //评估次数
    private int record_count_month;

    //会议召开率
    private String range_count_month;
}
