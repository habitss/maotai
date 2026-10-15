package com.keytech.modules.post.check.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 岗位检查记录配置主表
 * @Author: jeecg-boot
 * @Date:   2025-01-02
 * @Version: V1.0
 */
@Data
@TableName("mes3_post_check_record_config_main")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="岗位检查记录配置主表")
public class PostCheckRecordConfigMain implements Serializable {


	/**主键*/
    @Excel(name = "ID", width = 15)
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

	/**租户ID*/
    @Schema(description = "租户ID")
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

    @Schema(description="工段")
    private String section;

	/**配置名称*/
	@Excel(name = "配置名称", width = 15)
    @Schema(description = "配置名称")
    private String reportName;

	/**APP岗位名称*/
    @Schema(description = "APP岗位名称")
    private String appName;

    /**报表类型：1、通用岗位记录；2、岗位检查清单*/
    @Excel(name = "配置类型", width = 15, replace = { "通用岗位记录_1", "岗位检查清单_2" })
    @Schema(description = "配置类型")
    private String reportType;

	/**是否启用，0有效，1无效，设置默认值为0*/
	@Excel(name = "是否启用，0有效，1无效，设置默认值为0", width = 15, replace = { "是_1", "否_0" })
    @Schema(description = "是否启用，0有效，1无效，设置默认值为0")
    private String ifEffective;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**备注说明*/
    @Schema(description = "备注说明")
    private String remark;

	/**删除标识*/
    @Schema(description = "删除标识")
    @TableLogic
    private Integer delFlag;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
