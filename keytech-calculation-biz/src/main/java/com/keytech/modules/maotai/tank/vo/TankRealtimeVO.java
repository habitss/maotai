package com.keytech.modules.maotai.tank.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 *
 * 单罐实时数据VO（对应前端 updateTankRealtimeData 的标准字段）
 * @author keytech
 * @since 2026-07-28
 */
@Data
@Accessors(chain = true)
@Schema(description = "单罐实时数据")
public class TankRealtimeVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 罐体唯一标识（与前端 indicator.title 一致，如 ZTK101储罐） */
    @Schema(description = "罐体唯一标识")
    private String tankId;

    /** 位置编码（车间码-序号，对应设备台账，如 ZZG-001） */
    @Schema(description = "位置编码")
    private String funlocCode;

    /** 品牌 */
    @Schema(description = "品牌")
    private String brand;

    /** 液位百分比（0~100） */
    @Schema(description = "液位百分比")
    private Double levelPct;

    /** 温度（℃） */
    @Schema(description = "温度")
    private Double temperatureC;

    /** 酒精度（%vol） */
    @Schema(description = "酒精度")
    private Double alcoholPct;

    /** 当前容量（m³） */
    @Schema(description = "当前容量")
    private Double volumeM3;

    /** 总容量（m³） */
    @Schema(description = "总容量")
    private Double capacityM3;

    /** 状态：normal/standby/warning/alarm */
    @Schema(description = "状态")
    private String status;

    /** 批次号 */
    @Schema(description = "批次号")
    private String batchNo;

    /** 入罐量 */
    @Schema(description = "入罐量")
    private Double inbound;

    /** 出罐量 */
    @Schema(description = "出罐量")
    private Double outbound;

    /** 数据更新时间 */
    @Schema(description = "数据更新时间")
    private String updatedAt;

    /** 当前未消除的工艺报警摘要(来自 alarm_event,与工艺报警页同源;无报警为 null) */
    private String alarmDesc;

    /** 未消除报警明细(同源多条:级别/内容/时间),前端点击红罐弹窗展示 */
    private java.util.List<java.util.Map<String, Object>> alarmList;
}
