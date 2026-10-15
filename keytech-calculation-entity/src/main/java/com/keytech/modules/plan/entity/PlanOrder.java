package com.keytech.modules.plan.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Description: 计划工单-下发任务
 * @Author: jeecg-boot
 * @Date:   2025-06-12
 * @Version: V1.0
 */
@Data
@TableName("mes3_plan_order")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="计划工单-下发任务")
public class PlanOrder implements Serializable {
	/**工单表*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "工单表")
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

	/**岗位id*/
	@Excel(name = "岗位id", width = 15)
    @Schema(description = "岗位id")
    @Dict(dictTable ="mes3_mdm_post",dicText = "post_name",dicCode = "id")
    private String postId;

	/**设备id*/
	@Excel(name = "设备id", width = 15)
    @Schema(description = "设备id")
    @Dict(dictTable ="mes3_mdm_device",dicText = "device_name",dicCode = "id")
    private String deviceId;

	/**任务id*/
	@Excel(name = "任务id", width = 15)
    @Schema(description = "任务id")
    @Dict(dictTable ="mes3_mdm_task",dicText = "task_name",dicCode = "id")
    private String taskId;

	/**计划id*/
	@Excel(name = "计划id", width = 15)
    @Schema(description = "计划id")
    @Dict(dictTable ="mes3_plan",dicText = "plan_name",dicCode = "id")
    private String planId;

	/**机台计划id*/
	@Excel(name = "机台计划id", width = 15)
    @Schema(description = "机台计划id")
   private String planDeviceId;

	/**生产状态*/
	@Excel(name = "生产状态", width = 15)
    @Schema(description = "生产状态")
   private String productionStatus;

	/**任务类型*/
	@Excel(name = "任务类型", width = 15)
    @Schema(description = "任务类型")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_TASK_TYPE'", dicText = "item_key", dicCode = "item_val")
   private String taskTypeCode;

	/**任务种类*/
	@Excel(name = "任务种类", width = 15)
    @Schema(description = "任务种类")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_TASK_KIND'", dicText = "item_key", dicCode = "item_val")
   private String taskKindCode;

	/**执行状态*/
	@Excel(name = "执行状态", width = 15)
    @Schema(description = "执行状态")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_TASK_EXECUTE_STATUS'", dicText = "item_key", dicCode = "item_val")
   private Integer executeStatus;

	/**执行人*/
	@Excel(name = "执行人", width = 15)
    @Schema(description = "执行人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
   private String executorId;

	/**计划开始时间*/
	@Excel(name = "计划开始时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "计划开始时间")
    private Date planStartTime;

	/**计划结束时间*/
	@Excel(name = "计划结束时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "计划结束时间")
    private Date planEndTime;

	/**实际开始时间*/
	@Excel(name = "实际开始时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "实际开始时间")
    private Date realStartTime;

	/**实际结束时间*/
	@Excel(name = "实际结束时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "实际结束时间")
    private Date realEndTime;

	/**审核状态*/
	@Excel(name = "审核状态", width = 15)
    @Schema(description = "审核状态")
   private Integer reviewStatus;

	/**审核人*/
	@Excel(name = "审核人", width = 15)
    @Schema(description = "审核人")
   private String reviewerId;

	/**审核时间*/
	@Excel(name = "审核时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "审核时间")
    private Date reviewTime;

	/**是否为临时工单*/
	@Excel(name = "是否为临时工单", width = 15)
    @Schema(description = "是否为临时工单")
    private Integer isTemp;

	/**是否停机*/
	@Excel(name = "是否停机", width = 15)
    @Schema(description = "是否停机")
    private Integer stopFlag;

	/**班组*/
	@Excel(name = "班组", width = 15)
    @Schema(description = "班组")
    @Dict(dictTable ="mes3_mdm_shift_group",dicText = "group_name",dicCode = "id")
    private String team;

	/**班长*/
	@Excel(name = "班长", width = 15)
    @Schema(description = "班长")
    private String monitor;

	/**是否补录*/
	@Excel(name = "是否补录", width = 15)
    @Schema(description = "是否补录")
    private Integer adRecord;

	/**班次*/
	@Excel(name = "班次", width = 15)
    @Schema(description = "班次")
    @Dict(dictTable ="mes3_mdm_shift_manage",dicText = "shift_name",dicCode = "id")
    private String shiftNum;

	/**补录提交时间*/
	@Excel(name = "补录提交时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "补录提交时间")
    private Date recordTime;

	/**补录人*/
	@Excel(name = "补录人", width = 15)
    @Schema(description = "补录人")
    private String recordBy;

	/**补录备注*/
	@Excel(name = "补录备注", width = 15)
    @Schema(description = "补录备注")
    private String adRemark;

	/**异常事件id*/
	@Excel(name = "异常事件id", width = 15)
    @Schema(description = "异常事件id")
    private Integer pushWorkId;

    /**状态0：正常，1：审核，2：取消*/
    @Excel(name = "状态0：正常，1：审核，2：通过", width = 15)
    @Schema(description = "状态0：正常，1：审核，2：通过")
    private String activeStatus;

    /**取消提出人*/
    @Schema(description = "取消提出人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String activeUser;

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
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "username")
    private String updateBy;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**任务名称*/
	@Excel(name = "任务名称", width = 15)
    @Schema(description = "任务名称")
    private String taskName;

	/**任务关闭时间*/
	@Excel(name = "任务关闭时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "任务关闭时间")
    private Date closeTime;

	/**异常任务*/
	@Excel(name = "异常任务", width = 15)
    @Schema(description = "异常任务")
    private Integer isException;

	/**检验瓶数*/
	@Excel(name = "检验瓶数", width = 15)
    @Schema(description = "检验瓶数")
    private Integer inspectNum;

	/**不合格瓶数*/
	@Excel(name = "不合格瓶数", width = 15)
    @Schema(description = "不合格瓶数")
    private Integer inspectNoNum;

	/**是否准予入库*/
	@Excel(name = "是否准予入库", width = 15)
    @Schema(description = "是否准予入库")
    private Integer inspectIsPass;

	/**批次号*/
	@Excel(name = "批次号", width = 15)
    @Schema(description = "批次号")
    private String inspectBatchNum;

	/**特殊触发*/
	@Excel(name = "特殊触发", width = 15)
    @Schema(description = "特殊触发")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_TASK_SPECIAL_TYPE'", dicText = "item_key", dicCode = "item_val")
    private String specialType;

	/**父任务id*/
	@Excel(name = "父任务id", width = 15)
    @Schema(description = "父任务id")
    private Integer parentOrderId;

	/**维修确认状态*/
	@Excel(name = "维修确认状态", width = 15)
    @Schema(description = "维修确认状态")
    private Integer mainConfirmStatus;

    /**有子任务*/
    @Excel(name = "有子任务", width = 15)
    @Schema(description = "有子任务")
    private Integer hadChildOrder;

    /**结果历史配置id*/
    @Schema(description = "结果历史配置id")
    private String resultConfigId;

    /**触发类型*/
    @Excel(name = "触发类型", width = 15)
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "触发类型")
    private String recipeTypeCode;

    /**触发细类*/
    @Excel(name = "触发细类", width = 15)
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE_SUB'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "触发细类")
    private String recipeSubTypeCode;

    /**自动触发任务-与自动化数据比较后的触发时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "自动触发任务-与自动化数据比较后的触发时间")
    private LocalDateTime autoTriggerCalcTime;

    /**自动触发频率类型*/
    @Schema(description = "自动触发频率类型")
    @TableField(exist = false)
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_TASK_AUTO_TRIGGER_TYPE'", dicText = "item_key", dicCode = "item_val")
    private Integer autoTriggerFrequencyType;

    /**自动触发频率值，单位：小时*/
    @Schema(description = "自动触发频率值，单位：小时")
    @TableField(exist = false)
    private Integer autoTriggerFrequencyValue;

    @Schema(description = "App任务类型搜索字段")
    @TableField(exist = false)
    private String appSearchType;

    //检查项名称
    @TableField(exist = false)
    private String taskItemName;

    //是否按照岗位关联的设备id查询
    @TableField(exist = false)
    private Boolean postDevice;

    /**批量取消时的工单id列表，逗号分隔*/
    @Schema(description = "批量取消时的工单id列表，逗号分隔")
    @TableField(exist = false)
    private String ids;

    private static final long serialVersionUID = 1L;
}
