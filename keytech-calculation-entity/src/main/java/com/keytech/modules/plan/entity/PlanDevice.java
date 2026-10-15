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
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 工单管理-机台计划
 * @Author: jeecg-boot
 * @Date:   2025-06-06
 * @Version: V1.0
 */
@Data
@TableName("mes3_plan_device")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="工单管理-机台计划")
public class PlanDevice implements Serializable {


	/**机台计划*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "机台计划")
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

	/**主计划id*/
	@Excel(name = "主计划id", width = 15)
    @Schema(description = "主计划id")
    private String planId;

	/**skuid*/
	@Excel(name = "skuid", width = 15)
    @Schema(description = "skuid")
    private Integer skuId;

	/**产量*/
	@Excel(name = "产量", width = 15)
    @Schema(description = "产量")
    private Double yield;

	/**当前产量*/
	@Excel(name = "当前产量", width = 15)
    @Schema(description = "当前产量")
    private Double currentYield;

	/**计划开始时间*/
	@Excel(name = "计划开始时间", width = 20, format = "yyyy-MM-dd HH:mm")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm")
    @Schema(description = "计划开始时间")
    private Date planStartTime;

	/**计划结束时间*/
	@Excel(name = "计划结束时间", width = 20, format = "yyyy-MM-dd HH:mm")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm")
    @Schema(description = "计划结束时间")
    private Date planEndTime;

	/**实际开始时间*/
	@Excel(name = "实际开始时间", width = 20, format = "yyyy-MM-dd HH:mm")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "实际开始时间")
    private Date realStartTime;

	/**实际结束时间*/
	@Excel(name = "实际结束时间", width = 20, format = "yyyy-MM-dd HH:mm")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm")
    @Schema(description = "实际结束时间")
    private Date realEndTime;

    @Schema(description = "恢复时间")
    private Date recoveryTime;

	/**计划状态*/
	@Excel(name = "计划状态", width = 15)
    @Schema(description = "计划状态")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_PLAN_STATUS'",dicText = "item_key",dicCode = "item_val")
    private Integer planStatus;

	/**设备id*/
	@Excel(name = "设备id", width = 15)
    @Schema(description = "设备id")
    @Dict(dictTable ="mes3_mdm_device",dicText = "device_name",dicCode = "id")
    private Integer deviceId;

	/**主设备id*/
	@Excel(name = "主设备id", width = 15)
    @Schema(description = "主设备id")
    @Dict(dictTable ="mes3_mdm_device",dicText = "device_name",dicCode = "id")
    private Integer mainDeviceId;

	/**岗位id*/
	@Excel(name = "岗位id", width = 15)
    @Schema(description = "岗位id")
    private Integer postId;

	/**生产类型*/
	@Excel(name = "生产类型", width = 15)
    @Schema(description = "生产类型")
    private Integer productionStatus;

	/**计划消耗时长*/
	@Excel(name = "计划消耗时长", width = 15)
    @Schema(description = "计划消耗时长")
    private Integer timeConsume;

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

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**状态*/
	@Excel(name = "状态", width = 15)
    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "延迟关闭时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date delayCloseTime;

    private static final long serialVersionUID = 1L;
}
