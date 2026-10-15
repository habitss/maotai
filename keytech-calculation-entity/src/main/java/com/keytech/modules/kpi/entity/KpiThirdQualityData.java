package com.keytech.modules.kpi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * @Description: KPI第三方接口-质量KPI
 * @Author: HQ
 * @Date:   2025-08-18
 * @Version: V1.0
 */
@Data
@TableName("mes3_kpi_third_quality_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="KPI第三方接口-质量KPI")
public class KpiThirdQualityData implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private Integer id;

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

	/**组织层级：Group代表集团级、Factory代表工厂级*/
	@Excel(name = "组织层级：Group代表集团级、Factory代表工厂级", width = 15)
    @Schema(description = "组织层级：Group代表集团级、Factory代表工厂级")
    @JsonProperty(value = "orglevel")
    private String orgLevel;

	/**组织代码：YJ代表燕京集团、1001代表北京工厂*/
	@Excel(name = "组织代码：YJ代表燕京集团、1001代表北京工厂", width = 15)
    @Schema(description = "组织代码：YJ代表燕京集团、1001代表北京工厂")
    @JsonProperty(value = "orgid")
    private String orgId;

	/**时间维度：year代表年、month代表月、week代表周*/
	@Excel(name = "时间维度：year代表年、month代表月、week代表周", width = 15)
    @Schema(description = "时间维度：year代表年、month代表月、week代表周")
    @JsonProperty(value = "timedimension")
    private String timeDimension;

	/**年*/
	@Excel(name = "年", width = 15)
    @Schema(description = "年")
    private String year;

	/**月*/
	@Excel(name = "月", width = 15)
    @Schema(description = "月")
    private String month;

	/**周*/
	@Excel(name = "周", width = 15)
    @Schema(description = "周")
    private String week;

	/**父级指数编码*/
	@Excel(name = "父级指数编码", width = 15)
    @Schema(description = "父级指数编码")
    @JsonProperty(value = "parentindexcode")
    private String parentIndexCode;

	/**指数编码：1代表理化指数、101代表酿造理化指数*/
	@Excel(name = "指数编码：1代表理化指数、101代表酿造理化指数", width = 15)
    @Schema(description = "指数编码：1代表理化指数、101代表酿造理化指数")
    @JsonProperty(value = "indexcode")
    private String indexCode;

	/**指数值*/
	@Excel(name = "指数值", width = 15)
    @Schema(description = "指数值")
    @JsonProperty(value = "indexnum")
    private Double indexNum;

	/**指数单位*/
	@Excel(name = "指数单位", width = 15)
    @Schema(description = "指数单位")
    private String unit;

	/**报表显示指数值：指数值+单位*/
	@Excel(name = "报表显示指数值：指数值+单位", width = 15)
    @Schema(description = "报表显示指数值：指数值+单位")
    @JsonProperty(value = "indexnumshow")
    private String indexNumShow;

	/**目标值*/
	@Excel(name = "目标值", width = 15)
    @Schema(description = "目标值")
    @JsonProperty(value = "targetvalue")
    private Double targetValue;

	/**报表显示目标值：目标值+单位*/
	@Excel(name = "报表显示目标值：目标值+单位", width = 15)
    @Schema(description = "报表显示目标值：目标值+单位")
    @JsonProperty(value = "targetvalueshow")
    private String targetValueShow;

	/**是否达标：1代表达标*/
	@Excel(name = "是否达标：1代表达标", width = 15)
    @Schema(description = "是否达标：1代表达标")
    @JsonProperty(value = "reachflag")
    private String reachFlag;

	/**废弃(无用)*/
	@Excel(name = "废弃", width = 15)
    @Schema(description = "废弃")
    private String colorFlag;

	/**废弃(无用)*/
	@Excel(name = "废弃", width = 15)
    @Schema(description = "废弃")
    @JsonProperty(value = "logindexnum")
    private Double logIndexNum;

	/**权重：指数计算权重*/
	@Excel(name = "权重：指数计算权重", width = 15)
    @Schema(description = "权重：指数计算权重")
    private Double weight;

	/**最后更新时间：数据最后更新时间，到秒*/
	@Excel(name = "最后更新时间：数据最后更新时间，到秒", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后更新时间：数据最后更新时间，到秒")
    @JsonProperty(value = "lastupdatetime")
    private Date lastUpdateTime;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

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


    /**KPI信息*/
    @TableField(exist = false)
    private KpiInfo kpiInfo;
}
