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
 * 驾驶舱-月度/年度调拨(mes3_maotai_cockpit_transfer_plan)
 *
 * @author cockpit
 * @since 2026-09-10
 */
@Data
@TableName("mes3_maotai_cockpit_transfer_plan")
@Accessors(chain = true)
public class CockpitTransferPlan {

    @TableId("id")
    private String id;

    @TableField("stat_year")
    private Integer statYear;

    @TableField("stat_month")
    private Integer statMonth;

    @TableField("workshop_index")
    private Integer workshopIndex;

    @TableField("brand")
    private String brand;

    @TableField("planned")
    private Double planned;

    @TableField("actual")
    private Double actual;
    @TableField("tenant_id")
    private Integer tenantId;

    @TableField("del_flag")
    private Integer delFlag;

    @TableField("create_by")
    private String createBy;

    @TableField("create_time")
    private LocalDateTime createTime;
}