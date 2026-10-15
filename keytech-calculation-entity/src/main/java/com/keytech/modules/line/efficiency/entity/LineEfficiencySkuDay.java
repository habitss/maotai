package com.keytech.modules.line.efficiency.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

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
 * @Description: 产线-SKU天记录表/拆分
 * @Author: jeecg-boot
 * @Date:   2025-08-20
 * @Version: V1.0
 */
@Data
@TableName("mes3_line_efficiency_sku_day")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="产线-SKU天记录表/拆分")
public class LineEfficiencySkuDay implements Serializable {


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

	/**日期*/
	@Excel(name = "日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "日期")
    private Date dateDay;

	/**班次描述*/
	@Excel(name = "班次描述", width = 15)
    @Schema(description = "班次描述")
    private String classDes;

	/**sku编码*/
	@Excel(name = "sku编码", width = 15)
    @Schema(description = "sku编码")
    private String skuCode;

	/**sku名称-汉化*/
	@Excel(name = "sku名称-汉化", width = 15)
    @Schema(description = "sku名称-汉化")
    private String skuName;

	/**sku-速度*/
	@Excel(name = "sku-速度", width = 15)
    @Schema(description = "sku-速度")
    private Double skuSpeed;

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

	/**运行时长*/
	@Excel(name = "运行时长", width = 15)
    @Schema(description = "运行时长")
    private Double runningTime;

	/**产量*/
	@Excel(name = "产量", width = 15)
    @Schema(description = "产量")
    private Double outputValue;

	/**op*/
	@Excel(name = "op", width = 15)
    @Schema(description = "op")
    private Double speedOp;

    /**产量点位编码*/
    @Excel(name = "产量点位编码", width = 15)
    @Schema(description = "产量点位编码")
    private String outputCode;

	/**数据分类：1SKU数据，2酒机数据*/
	@Excel(name = "数据分类：1SKU数据，2酒机数据", width = 15)
    @Schema(description = "数据分类：1SKU数据，2酒机数据")
    private Integer type;

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

    /**原数据ID*/
    @TableField(exist = false)
    private String mainId;
}
