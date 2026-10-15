package com.keytech.modules.line.efficiency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 地磅数据
 * @Author: jeecg-boot
 * @Date:   2025-10-29
 * @Version: V1.0
 */
@Data
@TableName("mes3_db_scaleweight")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="地磅数据")
public class DbScaleweight implements Serializable {


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

	/**ticketid*/
	@Excel(name = "ticketid", width = 15)
    @Schema(description = "ticketid")
    private String ticketid;

	/**intime*/
	@Excel(name = "intime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "intime")
    private Date intime;

	/**outime*/
	@Excel(name = "outime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "outime")
    private Date outime;

	/**cchh*/
	@Excel(name = "cchh", width = 15)
    @Schema(description = "cchh")
    private String cchh;

	/**ppmm*/
	@Excel(name = "ppmm", width = 15)
    @Schema(description = "ppmm")
    private String ppmm;

	/**ddww*/
	@Excel(name = "ddww", width = 15)
    @Schema(description = "ddww")
    private String ddww;

	/**ddhh*/
	@Excel(name = "ddhh", width = 15)
    @Schema(description = "ddhh")
    private String ddhh;

	/**gggg*/
	@Excel(name = "gggg", width = 15)
    @Schema(description = "gggg")
    private String gggg;

	/**llxx*/
	@Excel(name = "llxx", width = 15)
    @Schema(description = "llxx")
    private String llxx;

	/**bbzz*/
	@Excel(name = "bbzz", width = 15)
    @Schema(description = "bbzz")
    private String bbzz;

	/**gross*/
	@Excel(name = "gross", width = 15)
    @Schema(description = "gross")
    private BigDecimal gross;

	/**tare*/
	@Excel(name = "tare", width = 15)
    @Schema(description = "tare")
    private BigDecimal tare;

	/**net*/
	@Excel(name = "net", width = 15)
    @Schema(description = "net")
    private BigDecimal net;

	/**kzbl*/
	@Excel(name = "kzbl", width = 15)
    @Schema(description = "kzbl")
    private BigDecimal kzbl;

	/**kkzz*/
	@Excel(name = "kkzz", width = 15)
    @Schema(description = "kkzz")
    private BigDecimal kkzz;

	/**jjss*/
	@Excel(name = "jjss", width = 15)
    @Schema(description = "jjss")
    private BigDecimal jjss;

	/**operator*/
	@Excel(name = "operator", width = 15)
    @Schema(description = "operator")
    private String operator;

	/**wtname*/
	@Excel(name = "wtname", width = 15)
    @Schema(description = "wtname")
    private String wtname;

	/**gwtime*/
	@Excel(name = "gwtime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "gwtime")
    private Date gwtime;

	/**gwtoper*/
	@Excel(name = "gwtoper", width = 15)
    @Schema(description = "gwtoper")
    private String gwtoper;

	/**taretime*/
	@Excel(name = "taretime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "taretime")
    private Date taretime;

	/**tareoper*/
	@Excel(name = "tareoper", width = 15)
    @Schema(description = "tareoper")
    private String tareoper;

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
}
