package com.keytech.modules.maotai.cockpit.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 *
 * 趋势时段数据VO
 * <p>
 * 对应前端 trend.periods 的结构。
 * 因 Java 标识符不能以数字开头，字段名用 t6h/t12h/t24h/t48h，
 * 通过 @JsonProperty 让 JSON 输出键名为 6h/12h/24h/48h，与前端匹配。
 *
 * @author keytech
 * @since 2026-07-29
 */
@Data
@Accessors(chain = true)
@Schema(description = "趋势时段数据")
public class TrendPeriodsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 近6小时（JSON键名: 6h） */
    @JsonProperty("6h")
    @Schema(description = "近6小时趋势")
    private TrendPointVO t6h;

    /** 近12小时（JSON键名: 12h） */
    @JsonProperty("12h")
    @Schema(description = "近12小时趋势")
    private TrendPointVO t12h;

    /** 近24小时（JSON键名: 24h） */
    @JsonProperty("24h")
    @Schema(description = "近24小时趋势")
    private TrendPointVO t24h;

    /** 近48小时（JSON键名: 48h） */
    @JsonProperty("48h")
    @Schema(description = "近48小时趋势")
    private TrendPointVO t48h;

    /**
     * 单个时段的趋势数据点
     */
    @Data
    @Accessors(chain = true)
    @Schema(description = "趋势数据点")
    public static class TrendPointVO implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 液位时序数据（%） */
        @Schema(description = "液位时序数据")
        private List<Double> liquid;

        /** 温度时序数据（℃） */
        @Schema(description = "温度时序数据")
        private List<Double> temperature;

        /** X轴标签 */
        @Schema(description = "X轴标签")
        private List<String> labels;
    }
}
