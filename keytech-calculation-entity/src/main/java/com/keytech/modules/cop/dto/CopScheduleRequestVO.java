package com.keytech.modules.cop.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * @author qipengbo
 * @version 1.0.0
 * @ClassName CopScheduleRequest
 * @Description TODO
 * @createTime 2025年03月05日 13:10:00
 */
@Getter
@Setter
@ToString
public class CopScheduleRequestVO {

    private String factoryCode;

    private String deptCode;

    private String lineCode;

    private String section;

    private String copDeviceId;

    private String copDeviceName;

    private String copPointId;

    private String copPointName;

    private String temperature;

    private String formula;

    private String frequency;

    private String clearBy;

    private String checkBy;

    private String planYear;

    private String months;

    private Long delFlag;

}
