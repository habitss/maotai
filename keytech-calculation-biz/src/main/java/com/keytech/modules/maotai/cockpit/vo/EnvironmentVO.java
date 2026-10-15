package com.keytech.modules.maotai.cockpit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 *
 * 室内环境数据VO
 * <p>
 * 对应前端 updateEnvironmentData 的标准结构。
 * 包含温度、湿度及其正常范围、预警/报警阈值、仪表量程。
 *
 * @author keytech
 * @since 2026-07-29
 */
@Data
@Accessors(chain = true)
@Schema(description = "室内环境数据")
public class EnvironmentVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 当前温度（℃） */
    @Schema(description = "当前温度", example = "23.5")
    private Double temperature;

    /** 当前湿度（%RH） */
    @Schema(description = "当前湿度", example = "62.8")
    private Double humidity;

    /** 温度正常范围 [下限, 上限] */
    @Schema(description = "温度正常范围", example = "[18, 28]")
    private List<Integer> temperatureRange;

    /** 湿度正常范围 [下限, 上限] */
    @Schema(description = "湿度正常范围", example = "[40, 70]")
    private List<Integer> humidityRange;

    /** 温度仪表量程 [最小, 最大] */
    @Schema(description = "温度仪表量程", example = "[0, 40]")
    private List<Integer> temperatureScale;

    /** 湿度仪表量程 [最小, 最大] */
    @Schema(description = "湿度仪表量程", example = "[0, 100]")
    private List<Integer> humidityScale;

    /** 温度预警阈值（℃） */
    @Schema(description = "温度预警阈值", example = "26")
    private Integer temperatureWarning;

    /** 温度报警阈值（℃） */
    @Schema(description = "温度报警阈值", example = "28")
    private Integer temperatureAlarm;

    /** 湿度预警阈值（%RH） */
    @Schema(description = "湿度预警阈值", example = "65")
    private Integer humidityWarning;

    /** 湿度报警阈值（%RH） */
    @Schema(description = "湿度报警阈值", example = "70")
    private Integer humidityAlarm;
}
