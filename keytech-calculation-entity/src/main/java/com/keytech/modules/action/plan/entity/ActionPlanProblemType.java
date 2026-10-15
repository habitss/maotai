package com.keytech.modules.action.plan.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @Description: 行动计划-问题分类
 * @Author: jeecg-boot
 * @Date:   2025-01-07
 * @Version: V1.0
 */
@Data
@TableName("mes3_action_plan_problem_type")
@Schema(description="行动计划-问题分类")
public class ActionPlanProblemType implements Serializable {
    private static final long serialVersionUID = 1L;

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

	/**工段*/
    @Schema(description = "工段")
    private String section;
	/**问题名称*/
	@Excel(name = "问题名称", width = 15)
    @Schema(description = "问题名称")
    private String problemName;
	/**父级节点*/
	@Excel(name = "父级节点", width = 15)
    @Schema(description = "父级节点")
    private String parentId;
	/**是否有子节点*/
    @Excel(name = "是否有子节点", width = 15, replace = { "是_1", "否_0" })
    @Schema(description = "是否有子节点")
    private String hasChild;
    /**是否禁用1：是、0：否*/
    @Schema(description = "是否禁用1：是、0：否")
    private Integer disabled;
	/**排序*/
    @Schema(description = "排序")
    private String sort;
	/**备注*/
    @Schema(description = "备注")
    private String remark;
	/**删除标识*/
    @Schema(description = "删除标识")
    @TableLogic
    private String delFlag;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
     private Date createTime;
	/**创建人*/
    @Schema(description = "创建人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "username")
    private String createBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
     private Date updateTime;
    /**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

    @TableField(exist = false)
    private List<ActionPlanProblemType> childList = new ArrayList<>();

    /**
     * 是否排除disabled字段，只要传值就排除
     */
    @TableField(exist = false)
    private String excludeDisabled;
}
