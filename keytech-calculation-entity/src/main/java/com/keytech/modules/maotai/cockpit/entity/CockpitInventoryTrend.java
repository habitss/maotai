package com.keytech.modules.maotai.cockpit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 驾驶舱-品牌库存趋势(mes3_maotai_cockpit_inventory_trend)
 *
 * @author cockpit
 * @since 2026-09-10
 */
@Data
@TableName("mes3_maotai_cockpit_inventory_trend")
@Accessors(chain = true)
public class CockpitInventoryTrend {

    @TableId("id")
    private String id;

    @TableField("stat_date")
    private LocalDate statDate;

    @TableField("workshop_index")
    private Integer workshopIndex;

    @TableField("brand")
    private String brand;

    @TableField("quantity")
    private Double quantity;
    @TableField("tenant_id")
    private Integer tenantId;

    @TableField("del_flag")
    private Integer delFlag;

    @TableField("create_by")
    private String createBy;

    @TableField("create_time")
    private LocalDateTime createTime;
}