package com.keytech.modules.maotai.cockpit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 *
 * 液位与温度趋势数据VO
 * <p>
 * 对应前端 updateTrendData 的标准结构。
 * 包含四个时段（6h/12h/24h/48h）的液位和温度时序数组。
 *
 * @author keytech
 * @since 2026-07-29
 */
@Data
@Accessors(chain = true)
@Schema(description = "液位与温度趋势数据")
public class TrendVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 当前选中的时段 */
    @Schema(description = "当前时段", example = "12h")
    private String current;

    /** 液位正常范围 [下限, 上限] */
    @Schema(description = "液位正常范围", example = "[60, 90]")
    private List<Integer> liquidNormalRange;

    /** 温度正常范围 [下限, 上限] */
    @Schema(description = "温度正常范围", example = "[18, 28]")
    private List<Integer> temperatureNormalRange;

    /** 各时段趋势数据 */
    @Schema(description = "各时段趋势数据")
    private TrendPeriodsVO periods;
}
