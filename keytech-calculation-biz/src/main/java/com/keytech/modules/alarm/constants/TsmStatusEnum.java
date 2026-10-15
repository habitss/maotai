package com.keytech.modules.alarm.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * TMS任务状态枚举(0、未处理 1、处理中 2、已解决 3、未解决 6、无法解决 7、待班长审核)
 * @author yepeng
 * @since 2026.01.20
 */
@Getter
@AllArgsConstructor
public enum TsmStatusEnum {
    UNPROCESS(0, "未处理"),
    PROCESSING(1, "处理中"),
    SOLVED(2, "已解决"),
    UNSOLVED(3, "未解决"),
    CANNOT_SOLVED(6, "无法解决"),
    WAITING_AUDIT(7, "待班长审核");

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
        for (TsmStatusEnum type : TsmStatusEnum.values()) {
            if (Objects.equals(type.getVal(), val)) {
                return type.getName();
            }
        }
        return "未知";
    }

}