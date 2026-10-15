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
 * @Description: PI曲线参数配置
 * @Author: jeecg-boot
 * @Date:   2026-01-19
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_curve_param")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="PI曲线参数配置")
public class PiCurveParam implements Serializable {


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

    /**参数 1工艺浓度/2冷麦汁产量*/
    @Excel(name = "参数", width = 15)
    @Schema(description = "参数")
    private Integer param;

	/**PI报表id*/
	@Excel(name = "PI报表id", width = 15)
    @Schema(description = "PI报表id")
    @Dict(dictTable ="mes3_pi_customize_table",dicText = "name",dicCode = "id")
    private String tableId;
    /**PI报表名称*/
    @Excel(name = "PI报表名称", width = 15)
    @Schema(description = "PI报表名称")
    private String tableName;

	/**时间*/
	@Excel(name = "时间", width = 15)
    @Schema(description = "时间")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String dateTime;

	/**订单号*/
	@Excel(name = "订单号", width = 15)
    @Schema(description = "订单号")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String orderNo;

	/**批次号*/
	@Excel(name = "批次号", width = 15)
    @Schema(description = "批次号")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String batchNo;

    /**参数值*/
    @Excel(name = "参数值", width = 15)
    @Schema(description = "参数值")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String paramVal;

    /**产线/区域*/
    @Excel(name = "产线/区域", width = 15)
    @Schema(description = "产线/区域")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_PI_CURVE_AREA'", dicText = "item_key", dicCode = "item_val")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String areaNo;

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



    /**时间*/
    @TableField(exist = false)
    private String dateTimeName;

    /**订单号*/
    @TableField(exist = false)
    private String orderName;

    /**批次号*/
    @TableField(exist = false)
    private String batchName;

    /**参数值*/
    @TableField(exist = false)
    private String paramValName;

    /**产线/区域名称*/
    @TableField(exist = false)
    private String areaName;
}
