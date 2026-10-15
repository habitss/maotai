package com.keytech.modules.kpi.entity;

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
 * @Description: kpi报表实际值周表
 * @Author: jeecg-boot
 * @Date:   2025-02-24
 * @Version: V1.0
 */
@Data
@TableName("mes3_kpi_report_actual_week_value")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="kpi报表实际值周表")
public class KpiReportActualWeekValue implements Serializable {


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
    private String factoryCode;

	/**部门Code*/
	@Excel(name = "部门Code", width = 15)
    @Schema(description = "部门Code")
    private String deptCode;

	/**产线Code*/
	@Excel(name = "产线Code", width = 15)
    @Schema(description = "产线Code")
    private String lineCode;

	/**Kpi 报表id*/
	@Excel(name = "Kpi 报表id", width = 15)
    @Schema(description = "Kpi 报表id")
    private String reportId;

	/**kpi编码*/
	@Excel(name = "kpi编码", width = 15)
    @Schema(description = "kpi编码")
    private String kpiCode;

	/**统计指标 1:周累 */
	@Excel(name = "统计指标 1:周累 ", width = 15)
    @Schema(description = "统计指标 1:周累 ")
    private Integer indicator;

	/**值*/
	@Excel(name = "值", width = 15)
    @Schema(description = "值")
    private String value;

	/**红黄绿 结果 1 green 2:red 3:yellow*/
	@Excel(name = "红黄绿 结果 1 green 2:red 3:yellow", width = 15)
    @Schema(description = "红黄绿 结果 1 green 2:red 3:yellow")
    private Integer colour;

	/**统计开始时间*/
	@Excel(name = "统计开始时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "统计开始时间")
    private Date startTime;

	/**统计结束时间*/
	@Excel(name = "统计结束时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "统计结束时间")
    private Date endTime;

	/**月份*/
	@Excel(name = "月份", width = 15)
    @Schema(description = "月份")
    private Integer month;

	/**年份*/
	@Excel(name = "年份", width = 15)
    @Schema(description = "年份")
    private Integer year;

    /**周数(第几周)*/
    @Excel(name = "周数", width = 15)
    @Schema(description = "周数")
    private Integer weekNumber;

	/**扩展配置*/
	@Excel(name = "扩展配置", width = 15)
    @Schema(description = "扩展配置")
    private String extJson;

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

    private static final long serialVersionUID = 1L;
}
