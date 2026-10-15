package com.keytech.modules.custom.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 自定义报表主表
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */

@Getter
@Setter
@Accessors(chain = true)
@TableName("mes3_custom_report")
@Schema(description="pi自定义报表主表")
public class CustomReport implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description="id")
    private String id;

	/**租户id**/
	@Schema(description="租户id")
	private Integer tenantId;

	@Schema(description="工厂Code")
	@TableField("factory_code")
	@Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@Excel(name = "工厂", width = 15, dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
	@NotBlank(message = "工厂Code为空")
	private String factoryCode;

	@Schema(description="部门Code")
	@TableField("dept_code")
	@Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@Excel(name = "部门", width = 15, dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
	@NotBlank(message = "部门Code为空")
	private String deptCode;

	@Schema(description="产线Code")
	@TableField("line_code")
	@Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
	@Excel(name = "产线", width = 15, dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
	@NotBlank(message = "产线Code为空")
	private String lineCode;
	/**工段*/
	@Excel(name = "工段", width = 15)
	@Schema(description="工段")
    private String section;
	/**报表名称*/
	@Excel(name = "报表名称", width = 15)
	@Schema(description="报表名称")
    private String name;
	/**表名*/
	@Excel(name = "表名", width = 15)
	@Schema(description="表名")
    private String tableName;
	/**备注说明*/
	@Excel(name = "备注说明", width = 15)
	@Schema(description="备注说明")
    private String notes;


	/**排序*/
	@Excel(name = "排序", width = 15)
	@Schema(description="排序")
	private Integer sort;
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
}
