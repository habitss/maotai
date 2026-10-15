package com.keytech.modules.plan.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @ Description: APP搜索字段类型
 * @ Author: sjw
 * @ Date: 2025/6/6 16:00
 */
public enum PlanAppSearchEnum {
    SEARCH_TYPE_1("1","生产任务"),
    SEARCH_TYPE_2("2","循环任务"),
    SEARCH_TYPE_3("3","交接班任务"),
    SEARCH_TYPE_10("10","自动触发任务"),
    SEARCH_TYPE_20("20","手动触发任务");

    public final String code;
    public final String name;

    PlanAppSearchEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    private static final Map<String, String> codeMap = new HashMap<>();

    static {
        for (PlanAppSearchEnum status : PlanAppSearchEnum.values()) {
            codeMap.put(status.name.toLowerCase(), status.code);
        }
    }
    public static String getCodeByName(String name) {
        return codeMap.get(name.toLowerCase());
    }
}
