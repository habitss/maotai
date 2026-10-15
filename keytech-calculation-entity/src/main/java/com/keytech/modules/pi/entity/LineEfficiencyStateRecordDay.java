package com.keytech.modules.pi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
 * @Description: 线效状态天记录表
 * @Author: jeecg-boot
 * @Date:   2025-07-24
 * @Version: V1.0
 */
@Data
@TableName("mes3_line_efficiency_state_record_day")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="线效状态天记录表")
public class LineEfficiencyStateRecordDay implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

	/**日期*/
	@Excel(name = "日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "日期")
    private String dayDate;

	/**产线代码*/
	@Excel(name = "产线代码", width = 15)
    @Schema(description = "产线代码")
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

	/**运行时长（秒）*/
	@Excel(name = "运行时长（秒）", width = 15)
    @Schema(description = "运行时长（秒）")
    private Long runningTime;

	/**分类0线效状态、1设备运行状态*/
	@Excel(name = "分类0线效状态、1设备运行状态", width = 15)
    @Schema(description = "分类0线效状态、1设备运行状态")
    private Integer type;

	/**实际结束时间*/
	@Excel(name = "实际结束时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "实际结束时间")
    private Date endTimeActual;

	/**线效值*/
	@Excel(name = "线效值", width = 15)
    @Schema(description = "线效值")
    private String lineEfficiencyValue;

	/**产量*/
	@Excel(name = "产量", width = 15)
    @Schema(description = "产量")
    private String production;

	/**额定产量*/
	@Excel(name = "额定产量", width = 15)
    @Schema(description = "额定产量")
    private Double ratedProduction;

	/**报警名称*/
	@Excel(name = "报警名称", width = 15)
    @Schema(description = "报警名称")
    private String alarmName;

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
}
