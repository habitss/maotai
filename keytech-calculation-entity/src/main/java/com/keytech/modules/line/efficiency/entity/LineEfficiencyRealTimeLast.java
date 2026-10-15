package com.keytech.modules.line.efficiency.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.annotation.*;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 实时计算逻辑-最新记录表
 * @Author: jeecg-boot
 * @Date:   2025-09-05
 * @Version: V1.0
 */
@Data
@TableName("mes3_line_efficiency_real_time_last")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="实时计算逻辑-最新记录表")
public class LineEfficiencyRealTimeLast implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

	/**产线代码*/
	@Excel(name = "产线代码", width = 15)
    @Schema(description = "产线代码")
    private String lineCode;

	/**产线名称*/
	@Excel(name = "产线名称", width = 15)
    @Schema(description = "产线名称")
    private String lineName;

	/**CIP状态 0 1*/
	@Excel(name = "CIP状态 0 1", width = 15)
    @Schema(description = "CIP状态 0 1")
    private String stateCode;

	/**开始时间*/
	@Excel(name = "开始时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private Date startTime;

	/**结束时间*/
	@Excel(name = "结束时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private Date endTime;

	/**跳变运行时长*/
	@Excel(name = "跳变运行时长", width = 15)
    @Schema(description = "跳变运行时长")
    private Double runningTime;

	/**分类0实时酒损*/
	@Excel(name = "分类0实时酒损", width = 15)
    @Schema(description = "分类0实时酒损")
    private Integer type;

	/**最新时间（用于计算）*/
	@Excel(name = "最新时间（用于计算）", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最新时间（用于计算）")
    private Date lastTime;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**删除标记*/
	@Excel(name = "删除标记", width = 15)
    @Schema(description = "删除标记")
    @TableLogic
    private Integer delFlag = 0;

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

    private static final long serialVersionUID = 1L;

    @Schema(description="触发时间")
    @TableField(exist = false)
    private Date triggerTime;

    @Schema(description="SKU配置信息")
    @TableField(exist = false)
    private List<LineEfficiencySku> handleList;

    @Schema(description="产量配置信息")
    @TableField(exist = false)
    private LineEfficiencyHandle productionHandle;

    @Schema(description="历史记录信息")
    @TableField(exist = false)
    private List<LineEfficiencyFilerlossRecord> listRecord;
}
