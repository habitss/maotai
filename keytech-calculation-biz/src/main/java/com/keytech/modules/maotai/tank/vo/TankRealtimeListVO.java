package com.keytech.modules.maotai.tank.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 *
 * 罐体实时数据列表包装VO
 * @author keytech
 * @since 2026-07-28
 */
@Data
@Accessors(chain = true)
@Schema(description = "罐体实时数据列表")
public class TankRealtimeListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 车间索引（0=中转酒罐车间,1=系列酒勾调厂房,2=成品酒罐房,3=包装厂房） */
    @Schema(description = "车间索引")
    private Integer workshopIndex;

    /** 数据更新时间 */
    @Schema(description = "数据更新时间")
    private String updatedAt;

    /** 单罐实时数据列表 */
    @Schema(description = "单罐实时数据列表")
    private List<TankRealtimeVO> list;
}
