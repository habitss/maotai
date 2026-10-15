package com.keytech.modules.alarm.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Objects;

/**
 * 报警状态枚举(0-未响应 1-已响应 2-处理中 3-已处理)
 * @author yepeng
 * @since 2026.01.20
 */
@Getter
@AllArgsConstructor
public enum AlarmStatusEnum {
    UNRESPOND(0, "未响应"),
    RESPONDED(1, "已响应"),
    PROCESSING(2, "处理中"),
    PROCESSED(3, "已处理");

    /**
     * 取值
     */
    private final Integer val;
    /**
     * 名称
     */
    private final String name;

    /**
     * 通过值获取名称
     */
    public static String getNameByVal(Integer val) {
        for (AlarmStatusEnum type : AlarmStatusEnum.values()) {
            if (Objects.equals(type.getVal(), val)) {
                return type.getName();
            }
        }
        return "未知";
    }

}