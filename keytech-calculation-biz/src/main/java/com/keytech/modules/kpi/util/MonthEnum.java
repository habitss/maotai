package com.keytech.modules.kpi.util;

/**
 * 月对照
 */
public enum MonthEnum {
    JANUARY("01", "janValue"),
    FEBRUARY("02", "febValue"),
    MARCH("03", "marValue"),
    APRIL("04", "aprValue"),
    MAY("05", "mayValue"),
    JUNE("06", "junValue"),
    JULY("07", "julValue"),
    AUGUST("08", "augValue"),
    SEPTEMBER("09", "sepValue"),
    OCTOBER("10", "octValue"),
    NOVEMBER("11", "novValue"),
    DECEMBER("12", "decValue"),
    YEAR("13", "yearTarget"),
    UNKNOWN_MONTH("-1", "未知");

    private final String month;
    private final String value;

    MonthEnum(String month, String value) {
        this.month = month;
        this.value = value;
    }

    public String getMonth() {
        return month;
    }

    public String getValue() {
        return value;
    }

    // 根据月份获取对应的值
    public static String getValueByMonth(String month) {
        for (MonthEnum monthEnum : values()) {
            if (monthEnum.getMonth() .equals(month)) {
                return monthEnum.getValue();
            }
        }
        return UNKNOWN_MONTH.getValue();
    }

    public static String getMonthByValue(String value) {
        for (MonthEnum monthEnum : values()) {
            if (monthEnum.getValue() .equals(value)) {
                return monthEnum.getMonth();
            }
        }
        return UNKNOWN_MONTH.getMonth();
    }
}
