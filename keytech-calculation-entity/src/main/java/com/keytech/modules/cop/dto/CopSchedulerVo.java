package com.keytech.modules.cop.dto;

import lombok.Data;

import java.util.List;

/**
 * @ClassName: CopSchedulerVo
 * @Description: cop计划排期vo
 * @Author: Jun
 * @Date: 2025/2/21 17:29
 **/
@Data
public class CopSchedulerVo {
    private String id;
    private String copPointId;
    private String temperature;
    private String formula;
    private String timeValue;
    private String frequency;
    private String clearBy;
    private String checkBy;
    private String state;
    private String date;
    private String sort;
    private String planYear;
    private Integer month;
    private int num;
    private Integer quarter;
    private Integer halfYear;
    private int result;
    private List<CopScheduleJson> scheduleJson;
    private int type;
}
