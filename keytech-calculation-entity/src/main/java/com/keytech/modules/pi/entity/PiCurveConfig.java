package com.keytech.modules.pi.entity;

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
import java.util.Date;

/**
 * @Description: PI曲线配置
 * @Author: jeecg-boot
 * @Date:   2025-12-09
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_curve_config")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="PI曲线配置")
public class PiCurveConfig implements Serializable {


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

    /**区域*/
    @Excel(name = "区域", width = 15)
    @Schema(description = "区域")
    private String areaNo;

    /**设备*/
    @Excel(name = "设备", width = 15)
    @Schema(description = "设备")
    @Dict(dictTable ="mes3_mdm_device",dicText = "device_name",dicCode = "device_code")
    private String deviceNo;

    /**PI报表id*/
    @Excel(name = "PI报表Id", width = 15)
    @Schema(description = "PI报表Id")
    @Dict(dictTable ="mes3_pi_customize_table",dicText = "name",dicCode = "id")
    private String tableId;
	/**PI报表名称*/
	@Excel(name = "PI报表名称", width = 15)
    @Schema(description = "PI报表名称")
    private String tableName;

	/**曲线类型(1单曲线/2多批次曲线)*/
	@Excel(name = "曲线类型(1单曲线/2多批次曲线)", width = 15)
    @Schema(description = "曲线类型(1单曲线/2多批次曲线)")
    private Integer curveType;

	/**时间(触发时间)*/
	@Excel(name = "时间(触发时间)", width = 15)
    @Schema(description = "时间(触发时间)")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Dict(dictTable ="mes3_pi_customize_table_field",dicText = "name",dicCode = "id")
    private String eventTime;

	/**开始时间*/
	@Excel(name = "开始时间", width = 15)
    @Schema(description = "开始时间")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Dict(dictTable ="mes3_pi_customize_table_field",dicText = "name",dicCode = "id")
    private String startTime;

	/**结束时间*/
	@Excel(name = "结束时间", width = 15)
    @Schema(description = "结束时间")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Dict(dictTable ="mes3_pi_customize_table_field",dicText = "name",dicCode = "id")
    private String endTime;

	/**设备编码*/
	@Excel(name = "设备编码", width = 15)
    @Schema(description = "设备编码")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Dict(dictTable ="mes3_pi_customize_table_field",dicText = "name",dicCode = "id")
    private String unitNo;

	/**设备名称*/
	@Excel(name = "设备名称", width = 15)
    @Schema(description = "设备名称")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Dict(dictTable ="mes3_pi_customize_table_field",dicText = "name",dicCode = "id")
    private String unitName;

	/**配方*/
	@Excel(name = "配方", width = 15)
    @Schema(description = "配方")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Dict(dictTable ="mes3_pi_customize_table_field",dicText = "name",dicCode = "id")
    private String recipe;

	/**订单号*/
	@Excel(name = "订单号", width = 15)
    @Schema(description = "订单号")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Dict(dictTable ="mes3_pi_customize_table_field",dicText = "name",dicCode = "id")
    private String orderNo;

	/**批次*/
	@Excel(name = "批次", width = 15)
    @Schema(description = "批次")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Dict(dictTable ="mes3_pi_customize_table_field",dicText = "name",dicCode = "id")
    private String batchNo;

	/**步骤名称*/
	@Excel(name = "步骤名称", width = 15)
    @Schema(description = "步骤名称")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Dict(dictTable ="mes3_pi_customize_table_field",dicText = "name",dicCode = "id")
    private String eopName;

    /**步骤号*/
    @Excel(name = "步骤号", width = 15)
    @Schema(description = "步骤号")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Dict(dictTable ="mes3_pi_customize_table_field",dicText = "name",dicCode = "id")
    private String eopNo;

	/**配方类型*/
	@Excel(name = "配方类型", width = 15)
    @Schema(description = "配方类型")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
//    @Dict(dictTable ="mes3_pi_customize_table_field",dicText = "name",dicCode = "id")
    private String recipeType;

    /**配方号*/
    @Excel(name = "配方号", width = 15)
    @Schema(description = "配方号")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Dict(dictTable ="mes3_pi_customize_table_field",dicText = "name",dicCode = "id")
    private String recipeNo;


    @Schema(description = "时间-对应PI")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String eventTimePi;
    @Schema(description = "开始时间-对应PI")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String startTimePi;
    @Schema(description = "结束时间-对应PI")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String endTimePi;
    @Schema(description = "设备编码-对应PI")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String unitNoPi;
    @Schema(description = "设备名称-对应PI")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String unitNamePi;
    @Schema(description = "配方-对应PI")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String recipePi;
    @Schema(description = "订单号-对应PI")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String orderNoPi;
    @Schema(description = "批次-对应PI")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String batchNoPi;
    @Schema(description = "步骤名称-对应PI")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String eopNamePi;
    @Schema(description = "步骤编码-对应PI")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String eopNoPi;
    @Schema(description = "配方类型-对应PI")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String recipeTypePi;
    @Schema(description = "配方号-对应PI")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String recipeNoPi;


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

    /**区域名称*/
    @TableField(exist = false)
    private String areaName;
}
