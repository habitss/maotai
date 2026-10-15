package com.keytech.modules.pi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description:  pi报表分配
 * @Author: jeecg-boot
 * @Date:   2024-11-266
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
@TableName("mes3_pi_allocation_table")
@Schema(description="pi报表分配")
public class PiAllocationTable implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description="id")
    private String id;

	/**租户id**/
	@Schema(description="租户id")
	private Integer tenantId;

	/**工厂*/
	@Excel(name = "工厂", width = 15 , dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@Schema(description="工厂")
	@Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@NotBlank(message = "工厂Code为空")
    private String factoryCode;
	/**部门*/
	@Schema(description="部门")
	@Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@Excel(name = "部门", width = 15 , dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@NotBlank(message = "部门Code为空")
    private String deptCode;
	/**产线*/
	@Schema(description="产线")
	@Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@Excel(name = "产线Code", width = 15 , dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@NotBlank(message = "产线Code为空")
	private String lineCode;


	/**报表名称*/
	@Excel(name = "报表名称", width = 15)
	@Schema(description="报表名称")
    private String reportName;
	/**绑定表*/
	@Excel(name = "绑定表", width = 15)
	@Dict(dictTable ="mes3_pi_customize_table",dicText = "name",dicCode = "id")
	@Schema(description="绑定表")
    private String tableId;
	/**表名*/
	@Excel(name = "表名", width = 15)
	@Schema(description="表名")
    private String tableName;
	/**修改字段模式*/
	@Excel(name = "修改字段模式", width = 15)
	@Schema(description="修改字段模式")
    private String modifiedMode;

	/**报表类型*/
	@Excel(name = "报表类型", width = 15)
	@Schema(description="报表类型")
	private String type;

	/**条件json*/
	@Excel(name = "条件json", width = 15)
	@Schema(description="条件json")
	private String conditionJson;

	/**是否分配多表(0否/1是)*/
	@Excel(name = "是否分配多表", width = 15)
	@Schema(description="是否分配多表")
	private String multipleTables;

	/**排序*/
	//@Excel(name = "排序", width = 15)
	@Schema(description="排序")
	private Integer sort;
	/**删除状态(0未删,1已删)*/
	//@Excel(name = "删除状态", width = 15)
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

	/**视图名称*/
	@Schema(description="视图名称")
	private String viewName;
	/**是否列表显示(0否/1是)*/
	@TableField(exist = false)
	private Integer isList;

}
