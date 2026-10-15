package com.keytech.modules.ems.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 采集异常日志状态(FAILED,SUCCESS,TERMINATED)
 * @author yepeng
 * @since 2025.05.04
 */
@Getter
@AllArgsConstructor
public enum CollectErrLogStateEnum {

    FAILED("FAILED", "失败"),
    SUCCESS("SUCCESS", "成功"),
    TERMINATED("TERMINATED", "终止"),
    ;

    /**
     * 取值
     */
    private final String val;
    /**
     * 名称
     */
    private final String name;

}