package com.keytech.modules.exception.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description: 异常处理报告
 * @Author: jeecg-boot
 * @Date:   2025-03-26
 * @Version: V1.0
 */
@Data
@TableName("mes3_exception_report")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="异常处理报告")
public class ExceptionReport implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂Code*/
	@Excel(name = "工厂Code", width = 15)
    @Schema(description = "工厂Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String factoryCode;

	/**部门Code*/
	@Excel(name = "部门Code", width = 15)
    @Schema(description = "部门Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

	/**产线Code*/
	@Excel(name = "产线Code", width = 15)
    @Schema(description = "产线Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

	/**工段*/
	@Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    private String section;

	/**异常报告编号*/
	@Excel(name = "异常报告编号", width = 15)
    @Schema(description = "异常报告编号")
    private String reportCode;

	/**异常触发条件*/
	@Excel(name = "异常触发条件", width = 15)
    @Schema(description = "异常触发条件")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_EXCEPTION_TRRIGER_CONDITION'", dicText = "item_key", dicCode = "item_val")
    private String triggerConditions;

	/**异常时间*/
	@Excel(name = "异常时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "异常时间")
    private Date exceptionTime;

	/**报告*/
	@Excel(name = "报告", width = 15)
    @Schema(description = "报告")
    private String reportFile;

	/**异常报告名称*/
	@Excel(name = "异常报告名称", width = 15)
    @Schema(description = "异常报告名称")
    private String reportName;

	/**问题类型*/
	@Excel(name = "问题类型", width = 15)
    @Schema(description = "问题类型")
    private Integer exceptionType;

	/**异常描述*/
	@Excel(name = "异常描述", width = 15)
    @Schema(description = "异常描述")
    private String exception;

	/**影响到什么产品*/
	@Excel(name = "影响到什么产品", width = 15)
    @Schema(description = "影响到什么产品")
    private String effectProduct;

	/**损失*/
	@Excel(name = "损失", width = 15)
    @Schema(description = "损失")
    private String loss;

	/**停机时间*/
	@Excel(name = "停机时间", width = 15)
    @Schema(description = "停机时间")
    private Integer stopTime;

	/**被替代的设备*/
	@Excel(name = "被替代的设备", width = 15)
    @Schema(description = "被替代的设备")
    private String replaceDevice;

	/**文件类型*/
	@Excel(name = "文件类型", width = 15)
    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**0不删除,1为删除*/
	@Excel(name = "0不删除,1为删除", width = 15)
    @Schema(description = "0不删除,1为删除")
    @TableLogic
    private Integer delFlag;

	/**数据创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "数据创建时间")
    private Date createTime;

	/**数据更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "数据更新时间")
    private Date updateTime;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

    @TableField(exist = false)
    private String times;

    @TableField(exist = false)
    private String raiseDate;

    @TableField(exist = false)
    private String problemDescription;

    @TableField(exist = false)
    private List<ActionPlanTrack> actionPlanTrackList;


    private static final long serialVersionUID = 1L;
}
