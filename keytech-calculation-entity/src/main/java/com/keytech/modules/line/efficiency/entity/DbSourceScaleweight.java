package com.keytech.modules.line.efficiency.entity;

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
 * @Description: 地磅数据 原始数据 sqlServer
 * @Author: jeecg-boot
 * @Date:   2025-10-29
 * @Version: V1.0
 */
@Data
@TableName("scaleweight")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="地磅数据")
public class DbSourceScaleweight implements Serializable {


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

    private static final long serialVersionUID = 1L;
}
