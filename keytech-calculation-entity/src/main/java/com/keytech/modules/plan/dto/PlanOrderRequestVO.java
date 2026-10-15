package com.keytech.modules.plan.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlanOrderRequestVO {

    private String deptCode;

    private String lineCode;

    private String year;

    private String month;

    private String realStartTime_begin;

    private String realStartTime_end;

    /**
     * 任务
     */
    private String taskId;

    /**
     * 身材计划类型
     */
    private String planType;

    /**
     * 设备名称
     */
    private String deviceIds;

    /**
     * 设备名称
     */
    private List<String> deviceList;

    /**
     * 设备名称
     */
    private List<String> taskIdList;

    /**
     * 检查项
     */
    private String itemName;

    //任务种类
    private String taskKindCode;
    /**
     * 岗位id
     */
    private String postId;
}
