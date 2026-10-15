package com.keytech.modules.pi.entity;

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
 * @Description: 曲线标准配置
 * @Author: jeecg-boot
 * @Date:   2026-01-22
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_curve_standard")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="曲线标准配置")
public class PiCurveStandard implements Serializable {


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

	/**数据源 1adapter/2ra*/
	@Excel(name = "数据源 1adapter/2ra", width = 15)
    @Schema(description = "数据源 1adapter/2ra")
    private Integer dataSource;

	/**产线/区域*/
	@Excel(name = "产线/区域", width = 15)
    @Schema(description = "产线/区域")
    private String areaNo;

	/**设备*/
	@Excel(name = "设备", width = 15)
    @Schema(description = "设备")
    private String unitNo;

	/**设备名称*/
	@Excel(name = "设备名称", width = 15)
    @Schema(description = "设备名称")
    private String unitName;

	/**配方号*/
	@Excel(name = "配方号", width = 15)
    @Schema(description = "配方号")
    private String recipeNo;

	/**配方名称*/
	@Excel(name = "配方名称", width = 15)
    @Schema(description = "配方名称")
    private String recipeName;

	/**步骤名称*/
	@Excel(name = "步骤名称", width = 15)
    @Schema(description = "步骤名称")
    private String eopName;

	/**步骤号*/
	@Excel(name = "步骤号", width = 15)
    @Schema(description = "步骤号")
    private String eopNo;

	/**ROP编号*/
	@Excel(name = "ROP编号", width = 15)
    @Schema(description = "ROP编号")
    private String ropId;

	/**参数名称*/
	@Excel(name = "参数名称", width = 15)
    @Schema(description = "参数名称")
    private String param;

	/**标准起始值*/
	@Excel(name = "标准起始值", width = 15)
    @Schema(description = "标准起始值")
    private String initialValue;

	/**标准结束值*/
	@Excel(name = "标准结束值", width = 15)
    @Schema(description = "标准结束值")
    private String endValue;

	/**标准时长(小时/分钟)*/
	@Excel(name = "标准时长", width = 15)
    @Schema(description = "标准时长")
    private String duration;

	/**速率*/
	@Excel(name = "速率", width = 15)
    @Schema(description = "速率")
    private String rate;

	/**标准时长类型(1分钟/ 2小时)*/
	@Excel(name = "标准时长类型(1分钟/ 2小时)", width = 15)
    @Schema(description = "标准时长类型(1分钟/ 2小时)")
    private Integer durationType;

	/**单位*/
	@Excel(name = "单位", width = 15)
    @Schema(description = "单位")
    private String unit;

    /**点位编码*/
    @Excel(name = "点位编码", width = 15)
    @Schema(description = "点位编码")
    @Dict(dictTable ="mes3_pi_base_point where del_flag = 0 and data_attribute = 0", dicText = "point_name", dicCode = "point_code")
    private String pointCode;

    /**多设备配置id*/
    @Excel(name = "多设备配置id", width = 15)
    @Schema(description = "多设备配置id")
    private String configId;

	/**分组*/
	@Excel(name = "分组", width = 15)
    @Schema(description = "分组")
    private String groupName;

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

    /**
     * 区域名称
     */
    @TableField(exist = false)
    private String areaName;


    /**
     * 系统产线
     */
    @TableField(exist = false)
    private String reccatNo;
}
