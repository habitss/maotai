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
 * 驾驶舱-今日调拨(mes3_maotai_cockpit_transfer_daily)
 *
 * @author cockpit
 * @since 2026-09-10
 */
@Data
@TableName("mes3_maotai_cockpit_transfer_daily")
@Accessors(chain = true)
public class CockpitTransferDaily {

    @TableId("id")
    private String id;

    @TableField("stat_date")
    private LocalDate statDate;

    @TableField("workshop_index")
    private Integer workshopIndex;

    @TableField("group_no")
    private Integer groupNo;

    @TableField("inbound")
    private Double inbound;

    @TableField("outbound")
    private Double outbound;

    @TableField("inbound_plan")
    private Double inboundPlan;

    @TableField("outbound_plan")
    private Double outboundPlan;
    @TableField("tenant_id")
    private Integer tenantId;

    @TableField("del_flag")
    private Integer delFlag;

    @TableField("create_by")
    private String createBy;

    @TableField("create_time")
    private LocalDateTime createTime;
}