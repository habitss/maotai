package com.keytech.modules.line.efficiency.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 线效状态记录表-最新记录
 * @Author: jeecg-boot
 * @Date:   2025-06-05
 * @Version: V1.0
 */
@Data
@TableName("mes3_line_efficiency_state_last")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="线效状态记录表-最新记录")
public class LineEfficiencyStateLast implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /**产线code*/
    @Excel(name = "产线code", width = 15)
    @Schema(description = "产线code")
    private String lineCode;

    /**产线名称*/
    @Excel(name = "产线名称", width = 15)
    @Schema(description = "产线名称")
    private String lineName;

	/**设备id*/
	@Excel(name = "设备id", width = 15)
    @Schema(description = "设备id")
    private String sbId;

	/**设备名称*/
	@Excel(name = "设备名称", width = 15)
    @Schema(description = "设备名称")
    private String sbName;

	/**运行状态 0未运行 1运行*/
	@Excel(name = "运行状态 0未运行 1运行", width = 15)
    @Schema(description = "运行状态 0未运行 1运行")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String stateCode;

	/**开始时间*/
	@Excel(name = "开始时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date startTime;

	/**结束时间*/
	@Excel(name = "结束时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date endTime;

	/**运行时长（秒）*/
	@Excel(name = "运行时长（秒）", width = 15)
    @Schema(description = "运行时长（秒）")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Double runningTime;

	/**分类0线效状态、1设备运行状态*/
	@Excel(name = "分类0线效状态、1设备运行状态", width = 15)
    @Schema(description = "分类0线效状态、1设备运行状态、2设备报警、3实时线效、4非运行能源、5运行能源、6点位故障状态")
    private Integer type;

	/**最新时间（用于计算）*/
	@Excel(name = "最新时间（用于计算）", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最新时间（用于计算）")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date lastTime;

	/**最新产量（用于计算）*/
	@Excel(name = "最新产量（用于计算）", width = 15)
    @Schema(description = "最新产量（用于计算）")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Double lastProduction;

    /**额定产量*/
    @Excel(name = "额定产量", width = 15)
    @Schema(description = "额定产量")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Double ratedProduction;

    /**报警名称*/
    @Excel(name = "报警名称", width = 15)
    @Schema(description = "报警名称")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String alarmName;


	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**删除标记*/
	@Excel(name = "删除标记", width = 15)
    @Schema(description = "删除标记")
    @TableLogic
    private Integer delFlag;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description="触发时间")
    @TableField(exist = false)
    private Date triggerTime;

    @Schema(description="配置信息")
    @TableField(exist = false)
    private List<LineEfficiencyHandle> lineEfficiencyHandleList;

    @Schema(description="需要存储的记录（仅用于设备运行时长和报警记录）")
    @TableField(exist = false)
    List<LineEfficiencyStateRecord> listRecord;


    /**sku更新时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "sku更新时间")
    private Date skuLastTime;

    /**线效产量最近清零时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "线效产量最近清零时间")
    private Date zeroClearingTime;

    private static final long serialVersionUID = 1L;
}
