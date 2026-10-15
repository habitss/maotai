package com.keytech.modules.pi.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 异常点位配置
 * @Author: jeecg-boot
 * @Date:   2025-03-04
 * @Version: V1.0
 */
@Data
@TableName("mes3_pi_abnormal_analysis_point")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="异常点位配置")
public class PiAbnormalAnalysisPoint implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    @Excel(name = "主键", width = 15)
    private String id;

	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;

    @Schema(description="工厂")
    @TableField("factory_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "工厂", width = 15, dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @NotBlank(message = "工厂Code为空")
    private String factoryCode;

    @Schema(description="部门")
    @TableField("dept_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "部门", width = 15, dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @NotBlank(message = "部门Code为空")
    private String deptCode;

    @Schema(description="产线")
    @TableField("line_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Excel(name = "产线", width = 15, dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @NotBlank(message = "产线Code为空")
    private String lineCode;

	/**工段*/
    @Schema(description = "工段")
    private String section;

    /**pi报表code*/
    @Excel(name = "pi报表编码", width = 15)
    @Schema(description = "pi报表code")
    private String tableCode;

    /**pi报表名称*/
    @Excel(name = "pi报表名称", width = 15)
    @Schema(description = "pi报表名称")
    private String tableName;

    /**pi列名code*/
    @Excel(name = "pi编码", width = 15)
    @Schema(description = "pi列名code")
    private String fieldCode;

    /**pi列名*/
    @Excel(name = "pi名称", width = 15)
    @Schema(description = "pi列名")
    private String fieldName;

    /**异常排查点名称*/
    @Excel(name = "异常排查点名称", width = 15)
    @Schema(description = "异常排查点名称")
    private String pointName;

    /**标准值*/
    @Excel(name = "标准值", width = 15)
    @Schema(description = "标准值")
    private String standardValue;

    /**是否有子节点*/
    @Schema(description = "是否有子节点")
    private String hasChild;

	/**填写范例*/
    @Schema(description = "填写范例")
    private String fillInExample;

	/**排查项说明*/
    @Schema(description = "排查项说明")
    private String illustrate;

    /**编号*/
    @Excel(name = "编号", width = 15)
    @Schema(description = "编号")
    private String serialNumber;

    /**父级编号*/
    @Schema(description = "父级编号编号")
    @TableField(exist = false)
    private String parentSerialNumber;

    /**父级主键*/
    @Schema(description = "父级主键（如有父级，父级编号和父级主键填写一个即可）")
    private String parentId;

	/**排序*/
    @Schema(description = "排序")
    private Double sort;

	/**备注*/
    @Schema(description = "备注")
    private String remark;

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
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "username")
    private String createBy;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

    private static final long serialVersionUID = 1L;
}
