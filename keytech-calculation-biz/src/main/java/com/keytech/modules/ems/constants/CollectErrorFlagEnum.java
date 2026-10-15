package com.keytech.modules.ems.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 计算状态是否异常(0-正常 1-异常)
 * @author yepeng
 * @since 2025.05.04
 */
@Getter
@AllArgsConstructor
public enum CollectErrorFlagEnum {

    NORMAL(0, "正常"),
    UNUSUAL(1, "异常"),
    NULL(2, "空值"),
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