package com.keytech.modules.pi.entity;

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
import java.util.Date;

/**
 * @Description: PI-字段合并
 * @Author: jeecg-boot
 * @Date:   2026-01-05
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_field_merge")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="PI-字段合并")
public class PiFieldMerge implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private Integer id;

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

	/**原表-表名*/
	@Excel(name = "原表-表名", width = 15)
    @Schema(description = "原表-表名")
    private String sourceTableName;

	/**原表-字段*/
	@Excel(name = "原表-字段", width = 15)
    @Schema(description = "原表-字段")
    private String sourceFieldName;

    /**原表-字段名称(中文)*/
    @Excel(name = "原表-字段名称", width = 15)
    @Schema(description = "原表-字段名称")
    private String sourceFieldCn;

	/**目标-表名*/
	@Excel(name = "目标-表名", width = 15)
    @Schema(description = "目标-表名")
    private String targetTableName;

	/**目标-字段*/
	@Excel(name = "目标-字段", width = 15)
    @Schema(description = "目标-字段")
    private String targetFieldName;

	/**目标-视图名称*/
	@Excel(name = "目标-视图名称", width = 15)
    @Schema(description = "目标-视图名称")
    private String targetView;

    /**状态0未完成, 1完成*/
    @Excel(name = "状态", width = 15)
    @Schema(description = "状态")
    private Integer status;

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
