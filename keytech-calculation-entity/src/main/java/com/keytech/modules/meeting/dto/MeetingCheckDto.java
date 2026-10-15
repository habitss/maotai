package com.keytech.modules.meeting.dto;

import lombok.Data;

/**
 * @author qipengbo
 * @version 1.0.0
 * @ClassName MeetingCheckDto
 * @Description 会议签到二维码
 * @createTime 2025年03月24日 13:19:00
 */
@Data
public class MeetingCheckDto {

    private String meetingId;

    /**
     * ip 地址
     */
    private String ipAddress;

    /**
     * 工厂名称
     */
    private String factoryCode;
}
