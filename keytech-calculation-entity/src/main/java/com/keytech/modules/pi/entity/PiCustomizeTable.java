package com.keytech.modules.pi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
//import io.swagger.annotations.ApiModel;
//import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Null;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: pi自定义报表主表
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */

@Getter
@Setter
@Accessors(chain = true)
@TableName("mes3_pi_customize_table")
@Schema(description="pi自定义报表主表")
public class PiCustomizeTable implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description="id")
    private String id;

	/**租户id**/
	@Schema(description="租户id")
	private Integer tenantId;

	/**工厂*/
	@Excel(name = "工厂", width = 15)
	@Schema(description="工厂")
	@Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private java.lang.String factoryCode;
	/**部门*/
	@Excel(name = "部门", width = 15)
	@Schema(description="部门")
	@Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private java.lang.String deptCode;
	/**产线*/
	@Excel(name = "产线", width = 15)
	@Schema(description="产线")
	@Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private java.lang.String lineCode;
	/**工段*/
	@Excel(name = "工段", width = 15)
	@Schema(description="工段")
	@Dict(dictTable ="mdm_dict where group_code = 'ORG_TYPE'",dicText = "item_key",dicCode = "item_val")
    private java.lang.String section;
	/**报表名称*/
	@Excel(name = "报表名称", width = 15)
	@Schema(description="报表名称")
    private java.lang.String name;
	/**表名*/
	@Excel(name = "表名", width = 15)
	@Schema(description="表名")
    private java.lang.String tableName;
	/**备注说明*/
	@Excel(name = "备注说明", width = 15)
	@Schema(description="备注说明")
    private java.lang.String notes;

	@TableField(updateStrategy = FieldStrategy.IGNORED)
	@Excel(name = "数据保留天数", width = 15)
	@Schema(description="数据保留天数")
	private Integer days;


	/**排序*/
	@Excel(name = "排序", width = 15)
	@Schema(description="排序")
	private Double sort;
	/**删除状态(0未删,1已删)*/
	@Excel(name = "删除状态", width = 15)
	@Schema(description="删除状态(0未删,1已删)")
	@TableLogic
	private Integer delFlag;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@Schema(description="创建时间")
	private Date createTime;
	/**修改时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@Schema(description="修改时间")
	private Date updateTime;
	/**创建人*/
	@Schema(description="创建人")
	private String createBy;
	/**修改人*/
	@Schema(description="修改人")
	private String updateBy;
	/**是否自动报表 1自动报表 0手动报表*/
	@Schema(description="是否自动报表")
	private String automatic;
	/**视图名称*/
	@Schema(description="视图名称")
	private String viewName;
	/**子表(扩展表)集合 mes3_basedata_001,mes3_basedata_001_1,mes3_basedata_001_2 */
	@Schema(description="子表(扩展表)集合")
	private String subtable;


	/**配置字段数*/
	@TableField(exist = false)
	private Integer configureField;
	/**配置算法数*/
	@TableField(exist = false)
	private Integer configureAlgorithm;
	/**未配置算法数*/
	@TableField(exist = false)
	private Integer noConfigureAlgorithm;
	/**子表ID字段*/
	@TableField(exist = false)
	private String field;

	@TableField(updateStrategy = FieldStrategy.IGNORED)
	@Excel(name = "数据重算时间对照PI", width = 15)
	@Schema(description="数据重算时间对照PI")
	private String recalculateTimePi;

	/**是否数据处理 1处理,其他不处理*/
	@TableField(exist = false)
	private String isDataHandle;
}
