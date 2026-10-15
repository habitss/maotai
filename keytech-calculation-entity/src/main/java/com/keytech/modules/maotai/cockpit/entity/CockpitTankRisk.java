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
 * 驾驶舱-储罐运行风险(mes3_maotai_cockpit_tank_risk)
 *
 * @author cockpit
 * @since 2026-09-10
 */
@Data
@TableName("mes3_maotai_cockpit_tank_risk")
@Accessors(chain = true)
public class CockpitTankRisk {

    @TableId("id")
    private String id;

    @TableField("stat_date")
    private LocalDate statDate;

    @TableField("workshop_index")
    private Integer workshopIndex;

    @TableField("group_no")
    private Integer groupNo;

    @TableField("running")
    private Integer running;

    @TableField("idle")
    private Integer idle;

    @TableField("cleaning")
    private Integer cleaning;

    @TableField("alarm")
    private Integer alarm;

    @TableField("loading_rate")
    private Double loadingRate;
    @TableField("tenant_id")
    private Integer tenantId;

    @TableField("del_flag")
    private Integer delFlag;

    @TableField("create_by")
    private String createBy;

    @TableField("create_time")
    private LocalDateTime createTime;
}