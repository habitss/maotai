package com.keytech.modules.ems.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 计算状态是否异常(0-未完成 1-已完成 2-失败)
 * @author yepeng
 * @since 2025.05.04
 */
@Getter
@AllArgsConstructor
public enum StatusFinishFlagEnum {

    UNDONE(0, "未完成"),
    FINISHED(1, "已完成"),
    FAILURE(2, "失败"),
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