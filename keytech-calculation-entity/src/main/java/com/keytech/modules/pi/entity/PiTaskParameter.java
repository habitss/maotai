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
 * @Description: pi任务参数配置
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
@TableName("mes3_pi_task_parameter")
@Schema(description="pi任务参数配置")
public class PiTaskParameter implements Serializable {
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

	/**自定义表id*/
	@Excel(name = "自定义表id", width = 15)
	@Schema(description="自定义表id")
	@Dict(dictTable ="mes3_pi_customize_table",dicText = "name",dicCode = "id")
	private String tableId;

	/**间隔*/
	@Excel(name = "间隔", width = 15)
	@Schema(description="间隔")
	private String difference;

	/**字段集合*/
	@Excel(name = "字段集合", width = 15)
	@Schema(description="字段集合")
	private String fields;

	/**任务id*/
	@Excel(name = "任务id", width = 15)
	@Schema(description="任务id")
	private String taskId;

	/**备注*/
	@Excel(name = "备注", width = 15)
	@Schema(description="备注")
	private String remark;

	/**任务名称*/
	@Excel(name = "任务名称", width = 15)
	@Schema(description="任务名称")
	private String name;

	/**自定义表表名*/
	@Excel(name = "自定义表表名", width = 15)
	@Schema(description="自定义表表名")
	private String tableName;

	/**排序*/
	//@Excel(name = "排序", width = 15)
	@Schema(description="排序")
	private Double sort;
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

//	/**查询*/
//	@TableField(exist = false)
//	private String queryDept;

	/**任务名称*/
	@TableField(exist = false)
	private String taskName;
}
