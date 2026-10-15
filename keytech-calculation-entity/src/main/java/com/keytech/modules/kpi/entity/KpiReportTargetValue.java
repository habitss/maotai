package com.keytech.modules.kpi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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
 * @Description: kpi报表目标值
 * @Author: jeecg-boot
 * @Date:   2025-02-24
 * @Version: V1.0
 */
@Data
@TableName("mes3_kpi_report_target_value")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="kpi报表目标值")
public class KpiReportTargetValue implements Serializable {


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

	/**月目标值配置*/
	@Excel(name = "月目标值配置", width = 15)
    @Schema(description = "月目标值配置")
    private String kpiTypeMonthValue;

	/**周目标值配置*/
	@Excel(name = "周目标值配置", width = 15)
    @Schema(description = "周目标值配置")
    private String kpiTypeWeekValue;

	/**年份*/
	@Excel(name = "年份", width = 15)
    @Schema(description = "年份")
    private Integer year;

	/**负责人A*/
	@Excel(name = "负责人A", width = 15)
    @Schema(description = "负责人A")
    private String possessBy;

	/**执行人R*/
	@Excel(name = "执行人R", width = 15)
    @Schema(description = "执行人R")
    private String provideBy;

	/**咨询人C*/
	@Excel(name = "咨询人C", width = 15)
    @Schema(description = "咨询人C")
    private String consultedBy;

	/**告知人I*/
	@Excel(name = "告知人I", width = 15)
    @Schema(description = "告知人I")
    private String informedBy;



    /**KPI职责分工负责人A*/
    @Excel(name = "负责人A", width = 15)
    @Schema(description = "负责人A")
    private String dutyPossessBy;

    /**KPI职责分工执行人R*/
    @Excel(name = "执行人R", width = 15)
    @Schema(description = "执行人R")
    private String dutyProvideBy;

    /**KPI职责分工咨询人C*/
    @Excel(name = "咨询人C", width = 15)
    @Schema(description = "咨询人C")
    private String dutyConsultedBy;

    /**KPI职责分工告知人I*/
    @Excel(name = "告知人I", width = 15)
    @Schema(description = "告知人I")
    private String dutyInformedBy;


	/**截止时间*/
	@Excel(name = "截止时间", width = 15)
    @Schema(description = "截止时间")
    private String deadline;

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

    private static final long serialVersionUID = 1L;

    /**年月*/
    @TableField(exist = false)
    private String month;
    /**单位*/
    @TableField(exist = false)
    private String unit;
    /**KPI(中文名)*/
    @TableField(exist = false)
    private String kpiName;
    /**KPI分类id*/
    @TableField(exist = false)
    private String typeId;
    /**KPI录入排序*/
    @TableField(exist = false)
    private Double inputSort;

    /**同步年*/
    @TableField(exist = false)
    private Integer copyYearTarget;
    /**同步月*/
    @TableField(exist = false)
    private String copyMonthTarget;

    /**月集合*/
    @TableField(exist = false)
    private List<Integer> months;
    /**是否显示周累 1是 /0否*/
    @TableField(exist = false)
    private Boolean showWeek;
    /**是否显示KPI职责分工 1是 /0否*/
    @TableField(exist = false)
    private Boolean showKpiDuty;
}
