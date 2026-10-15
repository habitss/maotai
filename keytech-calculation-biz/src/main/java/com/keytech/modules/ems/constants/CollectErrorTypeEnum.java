package com.keytech.modules.ems.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 采集异常类型(0-正常 1-连接失败 2-无数据 3-异常跳小 4-异常跳大 5-手动清零 6-全量异常)
 * @author yepeng
 * @since 2025.05.04
 */
@Getter
@AllArgsConstructor
public enum CollectErrorTypeEnum {

    NORMAL(0, "正常"),
    NETWORK_ERROR(1, "连接失败"),
    NULL_DATA_ERROR(2, "没有数据"),
    JUMP_MIN_ERROR(3, "异常跳小"),
    JUMP_MAX_ERROR(4, "异常跳大"),
    MANUAL_ZERO_ERROR(5, "手动清零"),
    ALL_ZERO_ERROR(6, "全零值异常"),
    NOT_SET_FLUX_CODE(7, "未配置FLUX点位"),
    ALL_NEGATE_ERROR(8, "全负值异常"),
    TIME_OUT_ERROR(9, "获取数据超时"),
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
        for (CollectErrorTypeEnum type : CollectErrorTypeEnum.values()) {
            if (type.getVal() == val) {
                return type.getName();
            }
        }
        return "未知";
    }

}