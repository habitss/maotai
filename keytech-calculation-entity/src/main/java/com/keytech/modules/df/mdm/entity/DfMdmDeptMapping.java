package com.keytech.modules.df.mdm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @Description: MDM部门与系统部门映射关系表
 * @Author: jeecg-boot
 * @Date:   2025-12-25
 * @Version: V1.0
 */
@Data
@TableName("mes3_mdm_df_dept_mapping")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="MDM部门与系统部门映射关系表")
public class DfMdmDeptMapping implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private Integer id;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Integer sort;

	/**0不删除,1为删除*/
	@Excel(name = "0不删除,1为删除", width = 15)
    @Schema(description = "0不删除,1为删除")
    @TableLogic
    private Integer delFlag;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

	/**数据创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "数据创建时间")
    private Date createTime;

	/**数据更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "数据更新时间")
    private Date updateTime;

	/**mdm部门编码*/
	@Excel(name = "mdm部门编码", width = 15)
    @Schema(description = "mdm部门编码")
    private String mdmDeptCode;

	/**mdm部门名称*/
	@Excel(name = "mdm部门名称", width = 15)
    @Schema(description = "mdm部门名称")
    private String mdmDeptName;

	/**mdm部门全称：工厂/部门/产线*/
	@Excel(name = "mdm部门全称：工厂/部门/产线", width = 15)
    @Schema(description = "mdm部门全称：工厂/部门/产线")
    private String mdmFullName;

	/**mdm部门拼接编码：工厂_部门_产线*/
	@Excel(name = "mdm部门拼接编码：工厂_部门_产线", width = 15)
    @Schema(description = "mdm部门拼接编码：工厂_部门_产线")
    private String mdmFullCode;

    /**系统部门编码*/
    @Excel(name = "系统部门编码", width = 15)
    @Schema(description = "系统部门编码")
    private String sysDeptCode;

    /**系统部门名称*/
    @Excel(name = "系统部门名称", width = 15)
    @Schema(description = "系统部门名称")
    private String sysDeptName;

    /**部门类型：工厂：10、部门：20、产线：30*/
    @Excel(name = "部门类型：工厂：10、部门：20、产线：30", width = 15)
    @Schema(description = "部门类型：工厂：10、部门：20、产线：30")
    private String deptType;

    @Schema(description = "是否映射")
    @TableField(exist = false)
    private Integer isMapping;

    @Serial
    private static final long serialVersionUID = 1L;
}
