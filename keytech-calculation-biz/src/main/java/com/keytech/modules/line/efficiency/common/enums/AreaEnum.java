package com.keytech.modules.line.efficiency.common.enums;

/**
 * @FileName AreaEnum
 * @Description 区域枚举
 * @Author HQ
 * @date 2025-11-13
 **/
public enum AreaEnum {
    AREA_BBT("Area_BBT", "清酒区域"),
    AREA_BH("Area_BH", "糖化区域"),
    AREA_FM("Area_FM", "发酵区域"),
    AREA_RM("Area_RM", "原料区域");

    private final String code;
    private final String name;

    AreaEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    // 根据code获取名称
    public static String getNameByCode(String code) {
        for (AreaEnum area : AreaEnum.values()) {
            if (area.getCode().equals(code)) {
                return area.getName();
            }
        }
        return null; // 或者返回默认值
    }

    // 根据code获取枚举
    public static AreaEnum getByCode(String code) {
        for (AreaEnum area : AreaEnum.values()) {
            if (area.getCode().equals(code)) {
                return area;
            }
        }
        return null;
    }
}
