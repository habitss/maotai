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
 * @Description: PI数据同步配置
 * @Author: jeecg-boot
 * @Date:   2026-03-31
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_sync_config")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="PI数据同步配置")
public class PiSyncConfig implements Serializable {


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

	/**源表名称*/
	@Excel(name = "源表名称", width = 15)
    @Schema(description = "源表名称")
    private String sourceTable;

	/**源字段*/
	@Excel(name = "源字段", width = 15)
    @Schema(description = "源字段")
    private String sourceField;

    /**源字段名称*/
    @Excel(name = "源字段名称", width = 15)
    @Schema(description = "源字段名称")
    private String sourceFieldName;

	/**源条件字段*/
//	@Excel(name = "源条件字段", width = 15)
//    @Schema(description = "源条件字段")
//    private String sourceCondition;

    /**源排序字段*/
	@Excel(name = "源排序字段", width = 15)
    @Schema(description = "源排序字段")
    private String sourceSort;

	/**目标表名称*/
	@Excel(name = "目标表名称", width = 15)
    @Schema(description = "目标表名称")
    private String targetTable;

	/**目标字段*/
	@Excel(name = "目标字段", width = 15)
    @Schema(description = "目标字段")
    private String targetField;

    /**目标字段名称*/
    @Excel(name = "目标字段名称", width = 15)
    @Schema(description = "目标字段名称")
    private String targetFieldName;

    /**目标/源表条件字段*/
	@Excel(name = "条件字段", width = 15)
    @Schema(description = "条件字段")
    private String targetCondition;

	/**分组字段  1交叉进罐*/
	@Excel(name = "分组字段", width = 15)
    @Schema(description = "分组字段")
    private String groupKey;

	/**是否启用（1是0否）*/
	@Excel(name = "是否启用（1是0否）", width = 15)
    @Schema(description = "是否启用（1是0否）")
    private Integer enabled;

	/**关联字段集合（JSON格式，如[{"target":"field_0001", "source":"field_0001"}]）*/
	@Excel(name = "关联字段集合", width = 15)
    @Schema(description = "关联字段集合")
    private String fieldMapping;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**计算类型（SUM求和/AVG平均/MAX最大/MIN最小/NONE不计算）*/
	@Excel(name = "计算类型", width = 15)
    @Schema(description = "计算类型（SUM求和/AVG平均/MAX最大/MIN最小/NONE不计算）")
    private String calcType;

	/**计算分组标识（同一分组的字段参与同一计算）*/
	@Excel(name = "计算分组标识", width = 15)
    @Schema(description = "计算分组标识")
    private String calcGroup;

	/**计算结果写入的目标字段*/
	@Excel(name = "计算结果字段", width = 15)
    @Schema(description = "计算结果字段")
    private String calcResultField;

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
