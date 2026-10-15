package com.keytech.modules.ems.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 预警等级(1-低 2-中 3-高)
 * @author yepeng
 * @since 2025.05.23
 */
@Getter
@AllArgsConstructor
public enum WarnLevelEnum {

    LOW(1, "低"),
    MID(2, "中"),
    HIGH(3, "高"),
    ;

    /**
     * 取值
     */
    private final int val;
    /**
     * 名称
     */
    private final String name;

    /**
     * 通过值获取名称
     * @param val
     */
    public static String getNameByVal(int val) {
        for (WarnLevelEnum type : WarnLevelEnum.values()) {
            if (type.getVal() == val) {
                return type.getName();
            }
        }
        return "未知";
    }

}