package com.keytech.modules.line.efficiency.dto;

import lombok.Data;

import java.util.Date;

/**
 * 报警推送对象
 * @author aolei
 * @create 2020-06-25 16:07
 */
@Data
public class EqutalarmrecDto {
    /**
     * 推送状态，默认为0
     */
    private Integer status;

    /**
     * 产线code
     */
    private String orgId;

    /**
     * 设备编码
     */
    private String equtId;

    /**
     * 设备名称
     */
    private String equtName;

    /**
     * 报警名称
     */
    private String alarmName;

    /**
     * 开始时间
     */
    private Date alarmBegTime;

    /**
     * 结束时间
     */
    private Date alarmEndTime;

    /**
     * 运行时长
     */
    private Long alarmRunTime;

    /**
     * 运行状态，0未运行 1运行
     */
    private Integer alarmtype;

}
