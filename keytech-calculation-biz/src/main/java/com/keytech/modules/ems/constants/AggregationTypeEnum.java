package com.keytech.modules.ems.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 聚合类型枚举
 * @author yepeng
 * @since 2025.05.13
 */
@Getter
@AllArgsConstructor
public enum AggregationTypeEnum {
    COLLECT_SHIFT(1, "采集表班聚合"),
    COLLECT_DAY(2, "采集表日聚合"),
    THIRD_SHIFT(3, "第三方班聚合"),
    THIRD_DAY(4, "第三方日聚合"),
    ORS_DATA_HOUR(5, "中间点小时计算"),
    ORS_DATA_SHIFT(6, "中间点班计算"),
    ORS_DATA_DAY(7, "中间点日计算"),
    ORS_DATA_WEEK(8, "中间点周计算"),
    ORS_DATA_MTD(9, "中间点月计算"),
    ORS_DATA_YTD(9, "中间点年计算")
    ;

    /**
     * 取值
     */
    private final Integer val;
    /**
     * 名称
     */
    private final String name;

    public static AggregationTypeEnum of(Integer val) {
        return Arrays.stream(values())
                .filter(e -> e.val.equals(val))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知聚合类型: " + val));
    }
}