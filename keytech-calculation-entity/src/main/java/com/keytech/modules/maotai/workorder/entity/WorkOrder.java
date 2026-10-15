package com.keytech.modules.maotai.workorder.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工单任务(工艺工单:卸车/装车/倒罐/灌装)
 * <p>
 * 区别于装卸车工单(mes3_maotai_loading_order,E+H 对接):本表为页面人工创建的工艺工单,
 * 状态机 pending→running→completed / abandoned 由页面流转,操作全程记 work_order_log。
 *
 * @author workorder
 * @since 2026-09-15
 */
@Data
@TableName("mes3_maotai_work_order")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "工单任务")
public class WorkOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_PAUSED = "paused";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_ABANDONED = "abandoned";

    /** 槽车工艺(显示车牌/驾驶员/联系电话/酒精度) */
    public static final String PROCESS_UNLOAD = "卸车工艺";
    public static final String PROCESS_LOAD = "装车工艺";

    @TableId("id")
    @Schema(description = "主键")
    private String id;

    @TableField("order_no")
    @Schema(description = "工单号 WO-yyyy-nnnn")
    private String orderNo;

    @TableField("process_name")
    @Schema(description = "工艺名称(卸车/装车/倒罐/灌装工艺)")
    private String processName;

    @TableField("workshop_code")
    @Schema(description = "所属车间(MES3_WORKSHOP 字典 ZZG/XLG/CPG/GWG)")
    private String workshopCode;

    @TableField("status")
    @Schema(description = "状态 pending待执行/running执行中/paused已暂停/completed已完成/abandoned已废弃")
    private String status;

    @TableField("source_tank")
    @Schema(description = "源罐(卸车工艺无)")
    private String sourceTank;

    @TableField("source_brand")
    @Schema(description = "源品牌")
    private String sourceBrand;

    @TableField("target_tank")
    @Schema(description = "目标罐(装车工艺无)")
    private String targetTank;

    @TableField("target_brand")
    @Schema(description = "目标品牌")
    private String targetBrand;

    @TableField("transfer_path")
    @Schema(description = "转酒路径编码(mes3_maotai_transfer_route.route_code)")
    private String transferPath;

    @TableField("transfer_path_name")
    @Schema(description = "转酒路径名称(选中配置带出)")
    private String transferPathName;

    @TableField("skid_code")
    @Schema(description = "卸车撬/鹤位编码(选中路径带出,卸/装车工艺)")
    private String skidCode;

    @TableField("amount")
    @Schema(description = "输送量(吨)")
    private Double amount;

    @TableField("empty_source")
    @Schema(description = "是否打空源罐 0否 1是(倒罐工艺)")
    private Integer emptySource;

    @TableField("batch_no")
    @Schema(description = "批次号(批次追溯用,选填)")
    private String batchNo;

    @TableField("plan_start_time")
    @Schema(description = "计划开始 HH:mm")
    private String planStartTime;

    @TableField("plan_end_time")
    @Schema(description = "计划完成 HH:mm")
    private String planEndTime;

    @TableField("start_time")
    @Schema(description = "实际开始时间")
    private LocalDateTime startTime;

    @TableField("end_time")
    @Schema(description = "实际结束时间")
    private LocalDateTime endTime;

    @TableField("plan_date")
    @Schema(description = "工单日期")
    private LocalDate planDate;

    @TableField("operator")
    @Schema(description = "操作人(启动操作的验证账号)")
    private String operator;

    @TableField("plate_no")
    @Schema(description = "车牌号(槽车工艺)")
    private String plateNo;

    @TableField("driver_name")
    @Schema(description = "驾驶员姓名")
    private String driverName;

    @TableField("contact_phone")
    @Schema(description = "联系电话")
    private String contactPhone;

    @TableField("alcohol")
    @Schema(description = "酒精度 %vol")
    private Double alcohol;

    @TableField("current_vehicle_qty")
    @Schema(description = "当前车载酒数量(倒罐工艺,API05 必填)")
    private String currentVehicleQty;

    @TableField("remark")
    @Schema(description = "备注说明")
    private String remark;

    @TableField("abandon_type")
    @Schema(description = "废弃类型 停止/删除")
    private String abandonType;

    @TableField("abandon_user")
    @Schema(description = "废弃操作人")
    private String abandonUser;

    @TableField("abandon_time")
    @Schema(description = "废弃时间")
    private String abandonTime;

    @TableField("tenant_id")
    private Integer tenantId;

    @TableField("del_flag")
    private Integer delFlag;

    @TableField("create_by")
    private String createBy;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_by")
    private String updateBy;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
