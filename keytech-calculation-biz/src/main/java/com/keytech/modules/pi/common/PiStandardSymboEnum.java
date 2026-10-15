package com.keytech.modules.pi.common;

/**
 * @Description: pi标准条件
 * @Author: sjw
 * @Date: 2025/6/6 16:00
 */
public enum PiStandardSymboEnum {
    SYMBOL_GT("1", ">"),
    SYMBOL_GE("2", ">="),
    SYMBOL_EQ("3", "="),
    SYMBOL_NE("4","!="),
    SYMBOL_LT("5","<"),
    SYMBOL_LE("6","<=");

    public final String code;
    public final String str;

    PiStandardSymboEnum(String code, String str) {
        this.code = code;
        this.str = str;
    }

    // 静态方法：根据 code 获取对应的 str
    public static String getStrByCode(String code) {
        for (PiStandardSymboEnum value : PiStandardSymboEnum.values()) {
            if (value.code.equals(code)) {
                return value.str;
            }
        }
        return null; // 或抛出异常，如：throw new IllegalArgumentException("Invalid code: " + code);
    }
}
