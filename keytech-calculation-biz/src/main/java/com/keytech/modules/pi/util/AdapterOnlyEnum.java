package com.keytech.modules.pi.util;

import com.keytech.modules.kpi.util.MonthEnum;

/**
 * Adapter唯一约束
 */
public enum AdapterOnlyEnum {
    YEAR_NO("批次年", "year_no"),
    ORDER_NO("订单号", "order_no"),
    BATCH_NO("批次号", "batch_no"),
    EOP_NO("EOP号", "eop_no"),
    LINE_NO("产线", "line_no"),
    RECIPE_NAME("配方", "recipe_name"),
    START_TIME("开始时间", "start_time");

    private final String code;
    private final String value;

    AdapterOnlyEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
