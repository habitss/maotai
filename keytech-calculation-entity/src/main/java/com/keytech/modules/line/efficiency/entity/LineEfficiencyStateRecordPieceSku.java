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
 * @Description: 线效清零分段-按照sku记录
 * @Author: jeecg-boot
 * @Date:   2025-08-25
 * @Version: V1.0
 */
@Data
@TableName("mes3_line_efficiency_state_record_piece_sku")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="线效清零分段-按照sku记录")
public class LineEfficiencyStateRecordPieceSku implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

	/**线效记录表ID*/
	//@Excel(name = "线效记录表ID", width = 15)
    @Schema(description = "线效记录表ID")
    private String recordId;

	/**线效清零分段表ID*/
	//@Excel(name = "线效清零分段表ID", width = 15)
    @Schema(description = "线效清零分段表ID")
    private String pieceId;

	/**产线code*/
	//@Excel(name = "产线code", width = 15)
    @Schema(description = "产线code")
    private String lineCode;

	/**产线名称*/
	@Excel(name = "产线", width = 15)
    @Schema(description = "产线名称")
    private String lineName;

	/**sku编码*/
	//@Excel(name = "sku编码", width = 15)
    @Schema(description = "sku编码")
    private String skuCode;

	/**sku名称-汉化*/
	@Excel(name = "SKU", width = 15)
    @Schema(description = "sku名称-汉化")
    private String skuName;



	/**瓶型描述*/
	//@Excel(name = "瓶型描述", width = 15)
    @Schema(description = "瓶型描述")
    private String bottleShape;

	/**瓶型值*/
	//@Excel(name = "瓶型值", width = 15)
    @Schema(description = "瓶型值")
    private Double bottleShapeValue;

	/**sku-hostid*/
	//@Excel(name = "sku-hostid", width = 15)
    @Schema(description = "sku-hostid")
    private String pointCode;

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

    /**sku-速度*/
    @Excel(name = "速度", width = 15)
    @Schema(description = "sku-速度")
    private Double skuSpeed;

	/**换算成ml*/
	//@Excel(name = "换算成ml", width = 15)
    @Schema(description = "换算成ml")
    private String shape;

	/**产量点位code*/
	//@Excel(name = "产量点位code", width = 15)
    @Schema(description = "产量点位code")
    private String outputCode;

	/**排序*/
	//@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**删除标记*/
	//@Excel(name = "删除标记", width = 15)
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

    //查询标识
    @TableField(exist = false)
    private String queryType;
    //OPI
    @TableField(exist = false)
    @Excel(name = "OPI", width = 15)
    private String opi;
    //批次id
    @TableField(exist = false)
    private String batchId;
    //批次开始时间
    @TableField(exist = false)
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date batchStartTime;
    //批次结束时间
    @TableField(exist = false)
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date batchEndTime;
    //批次运行时长
    @TableField(exist = false)
    private Double batchRunningTime;
}
