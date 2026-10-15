package com.keytech.modules.pi.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * @Description: 曲线局部标准配置
 * @Author: jeecg-boot
 * @Date:   2026-04-16
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_curve_partial_standard")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="曲线局部标准配置")
public class PiCurvePartialStandard implements Serializable {


	/**id*/
    @Excel(name = "id", width = 15)
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂Code*/
    @Schema(description = "工厂Code")
    private String factoryCode;

	/**部门Code*/
    @Schema(description = "部门Code")
    private String deptCode;

	/**产线Code*/
    @Schema(description = "产线Code")
    private String lineCode;

    /**数据源 1adapter/2ra*/
    //@Excel(name = "数据源 1adapter/2ra", width = 15)
    @Schema(description = "数据源 1adapter/2ra")
    private Integer dataSource;

	/**系统产线/罐区*/
    @Excel(name = "罐区号", width = 15)
    @Schema(description = "系统产线/罐区")
    private String reccatNo;

	/**区域pcu*/
    //@Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_MANUAL_DEVICE_REGION'", dicText = "item_key", dicCode = "item_val")
    //@Excel(name = "区域", width = 15,  dictTable ="mes3_mdm_dict where group_code = 'MES3_MANUAL_DEVICE_REGION'", dicText = "item_key", dicCode = "item_val")
    @Excel(name = "区域号", width = 15)
    @Schema(description = "区域pcu")
    private String pcuNo;

	/**区域名称*/
    @Excel(name = "区域名称", width = 15)
    @Schema(description = "区域名称")
    private String pcuName;

	/**配方号*/
    @Excel(name = "配方号", width = 15)
    @Schema(description = "配方号")
    private String recipeNo;

	/**配方名称*/
	@Excel(name = "配方名称", width = 15)
    @Schema(description = "配方名称")
    private String recipeName;

	/**设备号*/
    @Excel(name = "设备号", width = 15)
    @Schema(description = "设备号")
    private String unitNo;

	/**设备名称*/
	@Excel(name = "设备名称", width = 15)
    @Schema(description = "设备名称")
    private String unitName;

	/**步骤号*/
    @Excel(name = "步骤号", width = 15)
    @Schema(description = "步骤号")
    private String eopNo;

	/**步骤名称*/
	@Excel(name = "步骤名称", width = 15)
    @Schema(description = "步骤名称")
    private String eopName;

    /**ropId*/
    @Excel(name = "ropId", width = 15)
    @Schema(description = "ropId")
    private String ropId;

	/**起始值*/
	@Excel(name = "起始值", width = 15)
    @Schema(description = "起始值")
    private String initialValue;

	/**结束值*/
	@Excel(name = "结束值", width = 15)
    @Schema(description = "结束值")
    private String endValue;

	/**排序*/
    @Schema(description = "排序")
    private Double sort;

	/**0不删除,1为删除*/
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

	/**区域名称(导入用)*/
	//@Excel(name = "区域名称", width = 15)
	@TableField(exist = false)
	@Schema(description = "区域名称(导入用)")
	private String areaName;

    private static final long serialVersionUID = 1L;
}
