package com.keytech.modules.line.efficiency.dto;

import lombok.Data;

import java.util.Date;

/**
 * 设备运行时长推送对象
 * @author aolei
 * @create 2020-06-25 16:07
 */
@Data
public class EqutrunrecDto {
    /**
     * 推送状态，默认为0
     */
    private Integer status;

    /**
     * 产线code
     */
    private String orgId;

    /**
     * 产线名称
     */
    private String orgName;

    /**
     * 设备编码
     */
    private String equtID;

    /**
     * 设备名称
     */
    private String equtName;

    /**
     * 运行状态，0未运行 1运行
     */
    private Integer type;

    /**
     * 开始时间
     */
    private Date begTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 运行时长
     */
    private Long runTime;

}
