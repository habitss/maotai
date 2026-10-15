package com.keytech.modules.maotai.tank.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 罐体配置表实体（静态属性：罐号/品牌/容量/液位基准/状态类型）
 * <p>
 * 对应数据库表 mes3_maotai_tank_config，165 行固定配置数据。
 * 动态实时值（液位波动/温度/进出量）由 TankRealtimeServiceImpl 运行时计算。
 * 真实数据接入后，base_level 字段将被 InfluxDB/PI 实时液位替代。
 *
 * @author keytech
 * @since 2026-07-31
 */
@Data
@TableName("mes3_maotai_tank_config")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "罐体配置表")
public class TankConfig extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 罐体唯一标识（与前端 indicator.title 一致，如 ZTK101储罐） */
    @Schema(description = "罐体唯一标识")
    private String tankId;

    /** 车间索引（0~3） */
    @Schema(description = "车间索引")
    private Integer workshopIndex;

    /** 品牌 */
    @Schema(description = "品牌")
    private String brand;

    /** 总容量（m³） */
    @Schema(description = "总容量")
    private Double capacityM3;

    /** 液位基准值（0~100），运行时在此基础上随机波动 */
    @Schema(description = "液位基准值")
    private Double baseLevel;

    /** 状态类型：normal（正常，运行时按阈值判定 warning）/ standby（待检/清洗，固定） */
    @Schema(description = "状态类型")
    private String statusType;

    /** 液位点位编码（influx cnl_num，对应 mes3_pi_base_point.point_code） */
    @Schema(description = "液位点位编码")
    private String levelPointCode;

    /** 温度点位编码（influx cnl_num） */
    @Schema(description = "温度点位编码")
    private String tempPointCode;

    /** 酒精度点位编码（influx cnl_num） */
    @Schema(description = "酒精度点位编码")
    private String alcoholPointCode;

    /** 位置编码（车间码-序号，对应设备台账 funloc_code，如 ZZG-001） */
    @Schema(description = "位置编码")
    private String funlocCode;
}
