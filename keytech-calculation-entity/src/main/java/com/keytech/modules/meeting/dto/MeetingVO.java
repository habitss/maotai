package com.keytech.modules.meeting.dto;

import lombok.Data;

import java.util.List;

@Data
public class MeetingVO {

    private String deptCode;

    private String times;

    private List<String> timeRange;

    private int type;

}
