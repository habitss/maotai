package com.keytech.modules.plan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 计划工单-下发任务结果
 * @Author: jeecg-boot
 * @Date:   2025-06-12
 * @Version: V1.0
 */
@Data
@TableName("mes3_plan_order_result")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="计划工单-下发任务结果")
public class PlanOrderResult implements Serializable {
	/**工单结果表*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "工单结果表")
    private String id;

	/**租户id*/
	@Excel(name = "租户id", width = 15)
    @Schema(description = "租户id")
    private Integer tenantId;

    /**工厂code*/
    @Excel(name = "工厂code", width = 15)
    @Schema(description = "工厂code")
    private String factoryCode;

    /**部门code*/
    @Excel(name = "部门code", width = 15)
    @Schema(description = "部门code")
    private String deptCode;

    /**产线code*/
    @Excel(name = "产线code", width = 15)
    @Schema(description = "产线code")
    private String lineCode;

	/**工单id*/
	@Excel(name = "工单id", width = 15)
    @Schema(description = "工单id")
    private String orderId;

	/**任务id*/
	@Excel(name = "任务id", width = 15)
    @Schema(description = "任务id")
    private String taskId;

    /**条目id*/
    @Excel(name = "条目id", width = 15)
    @Schema(description = "条目id")
    private Integer itemId;

    /**结果*/
    @Excel(name = "结果", width = 15)
    @Schema(description = "结果")
    private String result;

    /**现场照片*/
    @Excel(name = "现场照片", width = 15)
    @Schema(description = "现场照片")
    private String resultImg;

    /**检查时间*/
    @Excel(name = "检查时间", width = 15)
    @Schema(description = "检查时间")
    private String checkTime;

    /**确认人id*/
    @Excel(name = "确认人id", width = 15)
    @Schema(description = "确认人id")
    private String confirmUserId;

    /**合格描述*/
    @Excel(name = "合格描述", width = 15)
    @Schema(description = "合格描述")
    private String qualifiedDesc;

    /**是否合格*/
    @Excel(name = "是否合格", width = 15)
    @Schema(description = "是否合格")
    private Integer isQualified;

    /**异常描述*/
    @Excel(name = "异常描述", width = 15)
    @Schema(description = "异常描述")
    private String errorDesc;

    /**基础点位编码*/
    @Excel(name = "基础点位编码", width = 15)
    @Schema(description = "基础点位编码")
    private String baseInfoCode;

    /**sort*/
    @Excel(name = "sort", width = 15)
    @Schema(description = "sort")
    private Double sort;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

    /**是否删除*/
    @Excel(name = "是否删除", width = 15)
    @Schema(description = "是否删除")
    @TableLogic
    private Integer delFlag;

    /**创建时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

    /**创建人*/
    @Schema(description = "创建人")
    private String createBy;

    /**更新时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

    /**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

    /**岗位id*/
    @Schema(description = "岗位id")
    private String postId;

    /**设备id*/
    @Schema(description = "设备id")
    private String deviceId;

    /**班组id*/
    @Schema(description = "班组id")
    private String shiftGroupId;

    /**班次id*/
    @Schema(description = "班次id")
    private String shiftId;

    /**na标识，1：NA*/
    @Schema(description = "班次id")
    private String naFlag;

    private static final long serialVersionUID = 1L;
}
