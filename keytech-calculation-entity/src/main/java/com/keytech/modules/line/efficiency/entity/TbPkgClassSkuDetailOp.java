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
 * @Description: OPI拆分
 * @Author: jeecg-boot
 * @Date:   2025-08-03
 * @Version: V1.0
 */
@Data
@TableName("tb_pkg_class_sku_detail_op")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="OPI拆分")
public class TbPkgClassSkuDetailOp implements Serializable {


	/**主键*/
	@TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;

	/**产线*/
	@Excel(name = "产线", width = 15)
    @Schema(description = "产线")
    private String line;

	/**日期*/
	@Excel(name = "日期", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "日期")
    private Date date;

	/**班次*/
	@Excel(name = "班次", width = 15)
    @Schema(description = "班次")
    private String classtime;

	/**SKU*/
	@Excel(name = "SKU", width = 15)
    @Schema(description = "SKU")
    private String sku;

	/**开始时间*/
	@Excel(name = "开始时间", width = 15)
    @Schema(description = "开始时间")
    private String st;

	/**结束时间*/
	@Excel(name = "结束时间", width = 15)
    @Schema(description = "结束时间")
    private String et;

	/**产量*/
	@Excel(name = "产量", width = 15)
    @Schema(description = "产量")
    private String detail;

	/**速度*/
	@Excel(name = "速度", width = 15)
    @Schema(description = "速度")
    private String speed;

	/**手动录入速度*/
	@Excel(name = "手动录入速度", width = 15)
    @Schema(description = "手动录入速度")
    private String speedOp;

    private static final long serialVersionUID = 1L;
    /**原数据ID*/
    @TableField(exist = false)
    private String mainId;
}
