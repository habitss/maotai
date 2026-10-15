package com.keytech.modules.plan.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 触发器枚举
 * @Author: sjw
 * @Date: 2025/6/6 16:00
 */
public enum PlanTriggersEnum {
    TASK_CORN_HOUR_END(0,"29"),
    TASK_CORN_HOUR_1(1,"30"),
    TASK_CORN_HOUR_2(2,"31"),
    TASK_CORN_HOUR_4(4,"32"),
    TASK_CORN_HOUR_6(6,"37"),
    TASK_CORN_HOUR_8(8,"33"),
    TASK_CORN_HOUR_10(10,"34"),
    TASK_CORN_HOUR_12(12,"35"),
    TASK_CORN_HOUR_24(24,"36"),
    TASK_CORN_60(60,"60"),
    AUTO_TRIGGER_CONFIG_TYPE_START(1, "开始步骤"),
    AUTO_TRIGGER_CONFIG_TYPE_END(2, "结束步骤"),
    AUTO_TRIGGER_CONDITION_TYPE_ENTER(1, "进入"),
    AUTO_TRIGGER_CONDITION_TYPE_OVER(2, "结束");

    public int code;
    public String name;

    PlanTriggersEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    private static final Map<String, Integer> codeMap = new HashMap<>();

    static {
        for (PlanTriggersEnum status : PlanTriggersEnum.values()) {
            codeMap.put(status.name.toLowerCase(), status.code);
        }
    }
    public static int getCodeByName(String name) {
        Integer code = codeMap.get(name.toLowerCase());
        return code == null ? -1 : code;
    }
}
