package com.keytech.modules.ems.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 定时任务类型(1-周期采集任务 2-错误重试任务 3-人工补采任务)
 * @author yepeng
 * @since 2025.05.07
 */
@Getter
@AllArgsConstructor
public enum JobTypeEnum {

    PERIOD_COLLECT_JOB(1, "周期采集任务"),
    ERROR_RETRY_JOB(2, "错误重试任务"),
    MANUAL_BACK_JOB(3, "人工补采任务"),
    ;

    /**
     * 取值
     */
    private final int val;
    /**
     * 名称
     */
    private final String name;

}