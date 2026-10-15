package com.keytech.modules.common.enums;

/**
 * @FileName TimeDimension
 * @Description 时间维度枚举
 * @Author HQ
 * @date 2025-08-20
 **/
public enum TimeDimension {
    YEAR("year"),
    MONTH("month"),
    WEEK("week");

    private final String value;

    TimeDimension(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // 一个常用的方法：根据字符串值获取对应的枚举实例
    public static TimeDimension fromValue(String value) {
        for (TimeDimension dimension : TimeDimension.values()) {
            if (dimension.value.equals(value)) {
                return dimension;
            }
        }
        throw new IllegalArgumentException("未知的时间维度: " + value);
    }
}
