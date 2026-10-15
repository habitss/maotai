package com.keytech.modules.maotai.workorder.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工单操作日志(仅记录工单的人工操作:创建/编辑/状态流转)
 *
 * @author workorder
 * @since 2026-09-15
 */
@Data
@TableName("mes3_maotai_work_order_log")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "工单操作日志")
public class WorkOrderLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId("id")
    @Schema(description = "主键")
    private String id;

    @TableField("order_no")
    @Schema(description = "工单号")
    private String orderNo;

    @TableField("operator")
    @Schema(description = "操作人")
    private String operator;

    @TableField("action")
    @Schema(description = "操作类型 创建工单/编辑工单/状态变更/工单废弃")
    private String action;

    @TableField("content")
    @Schema(description = "操作内容")
    private String content;

    @TableField("order_status")
    @Schema(description = "操作后工单状态")
    private String orderStatus;

    @TableField("operate_time")
    @Schema(description = "操作时间")
    private LocalDateTime operateTime;

    @TableField("tenant_id")
    private Integer tenantId;

    @TableField("del_flag")
    private Integer delFlag;

    @TableField("create_time")
    private LocalDateTime createTime;
}
