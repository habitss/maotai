package com.keytech.modules.maotai.cockpit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 *
 * 储罐风险数据VO
 * <p>
 * 对应前端 updateTankRiskData 的标准结构。
 * 前端兼容字段：running/runningCount/runningTankCount、idle/idleCount、
 * cleaning/pendingInspection、alarm/alarmCount/warningCount、
 * loadingRate/averageLoadingRate/avgLoadingRate。
 *
 * @author keytech
 * @since 2026-07-29
 */
@Data
@Accessors(chain = true)
@Schema(description = "储罐风险数据")
public class TankRiskVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 运行中罐数 */
    @Schema(description = "运行中罐数", example = "39")
    private Integer running;

    /** 空闲罐数 */
    @Schema(description = "空闲罐数", example = "6")
    private Integer idle;

    /** 待检/清洗罐数 */
    @Schema(description = "待检/清洗罐数", example = "3")
    private Integer cleaning;

    /** 预警罐数 */
    @Schema(description = "预警罐数", example = "2")
    private Integer alarm;

    /** 平均装载率（%） */
    @Schema(description = "平均装载率", example = "72.2")
    private Double loadingRate;
}
