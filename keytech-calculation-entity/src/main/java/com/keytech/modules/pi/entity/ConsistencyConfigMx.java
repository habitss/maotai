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
 * @Description: 一次性指数配置明细表
 * @Author: jeecg-boot
 * @Date:   2025-01-13
 * @Version: V1.0
 */
@Data
@TableName("mes3_consistency_config_mx")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="一次性指数配置明细表")
public class ConsistencyConfigMx implements Serializable {


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

	/**工段*/
	@Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    private String section;


    /**PI名称*/
    @Excel(name = "配置主表ID", width = 15)
    @Schema(description = "配置主表ID")
    private String zbId;

	/**PI名称*/
	@Excel(name = "PI名称", width = 15)
    @Schema(description = "PI名称")
    private String piName;

	/**pi编码（一级指数存id，二级指数存field编码）*/
	@Excel(name = "pi编码（一级指数存id，二级指数为空）", width = 15)
    @Dict(dictTable ="mes3_consistency_config_zb", dicText = "consistency_name", dicCode = "id")
    @Schema(description = "pi编码（一级指数存id，二级指数为空）")
    private String piCode;

    /**已配置pi数*/
    @Excel(name = "已配置pi数", width = 15)
    @Schema(description = "已配置pi数")
    private Integer piTotal;

	/**单位*/
	@Excel(name = "单位", width = 15)
    @Schema(description = "单位")
    private String unit;

	/**指数权重（%）*/
	@Excel(name = "指数权重（%）", width = 15)
    @Schema(description = "指数权重（%）")
    private Double weight;

	/**sort*/
	@Excel(name = "sort", width = 15)
    @Schema(description = "sort")
    private Double sort;

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

    @Schema(description = "绿区数")
    @TableField(exist = false)
    private double green;

    @Schema(description = "总批次")
    @TableField(exist = false)
    private double totle;

    @Schema(description = "绿区绿")
    @TableField(exist = false)
    private double greenProportion;

    @Schema(description = "一致性指数得分")
    @TableField(exist = false)
    private double greenProportionConsistency;

    private static final long serialVersionUID = 1L;
}
