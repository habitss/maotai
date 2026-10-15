package com.keytech.modules.pi.dto;

import lombok.Data;

import java.util.List;

/**
 * 定时任务下拉数据
 */
@Data
public class TaskDataDto {


    /**
     * 名称
     */
    private String label;

    /**
     * 值(编码)
     */
    private String value;

    /**
     * 不需要查询的任务
     */
    private String exclusionTask;

    /**
     * 分组id
     */
    private String groupId;

    /**
     * 查询的任务类型
     */
    private String exclusionParam;
}
