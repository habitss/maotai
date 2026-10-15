package com.keytech.modules.meeting.dto;

import lombok.Data;

/**
 * @author qipengbo
 * @version 1.0.0
 * @ClassName MeetingScheduleVO
 * @Description 会议排期对象
 * @createTime 2025年03月23日 17:18:00
 */
@Data
public class MeetingScheduleVO {

    //会议类型
    private String meetingType;
    //排期类型
    private int type;
    //周
    private String week;
    //天
    private String day;

    private String dayWeek;

    //时间
    private String time;
    //排期ID
    private String scheduleId;
}
