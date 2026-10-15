package com.keytech.modules.kpi.util;

import lombok.AllArgsConstructor;
import org.jeecg.common.util.oConvertUtils;

/**
 * @author jacksparrow
 * @version v1.0.0
 * @date 2023/3/13 17:05
 */
@AllArgsConstructor
public enum PiColourEnum {
    GREEN(1, "green"),
    RED(2, "red"),
    YELLOW(3, "yellow");
    private int code;

    public static String getValueByCode(int code) {
        if (code == 0) {
            return "none";
        }
        for (PiColourEnum val : values()) {
            if (val.getCode() == (code)) {
                return val.getName();
            }
        }
        return "none";
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private String name;

    public static PiColourEnum getPiEnumByValue(String value) {
        if (oConvertUtils.isEmpty(value)) {
            return null;
        }
        for (PiColourEnum val : values()) {
            if (val.getName().equals(value)) {
                return val;
            }
        }
        return null;
    }

    public static int getCodeByValue(String value) {
        if (oConvertUtils.isEmpty(value)) {
            return 0;
        }
        for (PiColourEnum val : values()) {
            if (val.getName().equals(value)) {
                return val.getCode();
            }
        }
        return 0;
    }

}
