package com.keytech.modules.project.management.entity;

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
 * @Description: 项目行动计划
 * @Author: jeecg-boot
 * @Date:   2025-03-28
 * @Version: V1.0
 */
@Data
@TableName("mes3_project_management_action")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="项目行动计划")
public class ProjectManagementAction implements Serializable {


	/**id*/
    @Excel(name = "行动主键", width = 20, orderNum = "3")
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;

    @Schema(description="工厂Code")
    @TableField("factory_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @NotBlank(message = "工厂Code为空")
    private String factoryCode;

    @Schema(description="部门Code")
    @TableField("dept_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @NotBlank(message = "部门Code为空")
    private String deptCode;

	/**产线*/
    @Schema(description = "产线")
    private String lineCode;

	/**工段*/
    @Schema(description = "工段")
    private String section;

	/**项目id*/
    @Schema(description = "项目id")
    @Excel(name = "项目主键", orderNum = "13")
    private String projectId;

	/**节点id*/
	@Excel(name = "项目关键节点主键", width = 15)
    @Schema(description = "节点id")
    @Dict(dictTable ="mes3_project_management_node",dicText = "node_name",dicCode = "id")
    private String nodeId;

	/**负责人*/
    @Schema(description = "负责人")
    private String leaderUserName;

	/**负责人ID*/
	@Excel(name = "负责人", width = 15, dictTable ="sys_user", dicText = "realname", dicCode = "id", orderNum = "7")
    @Schema(description = "负责人ID")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String leaderUserId;

	/**负责部门Id*/
	@Excel(name = "负责部门", width = 15, dictTable ="sys_depart", dicText = "depart_name", dicCode = "id", orderNum = "6")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    @Schema(description = "负责部门Id")
    private String leaderDeptId;

	/**行动计划名称*/
	@Excel(name = "行动计划名称", width = 15, orderNum = "5")
    @Schema(description = "行动计划名称")
    private String actionName;

	/**提交成果*/
	@Excel(name = "提交成果", width = 15, orderNum = "8")
    @Schema(description = "提交成果")
    private String submitResult;

	/**计划完成时间*/
	@Excel(name = "计划完成时间", width = 20, format = "yyyy-MM-dd", orderNum = "9")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "计划完成时间")
    private Date planCompletionTime;

	/**实际完成时间*/
	@Excel(name = "实际完成时间", width = 20, format = "yyyy-MM-dd", orderNum = "10")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "实际完成时间")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date realCompletionTime;

	/**完成状态*/
	@Excel(name = "完成状态", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_FINISH'", dicText = "item_key", dicCode = "item_val", orderNum = "11")
    @Schema(description = "状态：进行中	已完成	延期；	延期完成")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_FINISH'", dicText = "item_key", dicCode = "item_val")
    private Integer actionState;

	/**激励  分数*/
	@Excel(name = "激励", width = 15, orderNum = "12")
    @Schema(description = "激励")
    private Double reward;

	/**排序*/
	@Excel(name = "行动计划排序", width = 15, orderNum = "4")
    @Schema(description = "排序")
    private Double sort;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
    private Date createTime;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
    private Date updateTime;

    /**是否删除（1是0否）*/
    @Schema(description = "是否删除（1是0否）")
    @TableLogic
    private Integer delFlag;

    /**备注*/
    @Schema(description = "备注")
    private String remark;

    @TableField(exist = false)
    @Schema(description = "node排序")
    @Excel(name = "项目关键节点排序", width = 15, orderNum = "1")
    private Double nodeSort;

    @TableField(exist = false)
    @Schema(description = "node名称")
    @Excel(name = "项目关键节点", width = 15, orderNum = "2")
    private String nodeName;

    private static final long serialVersionUID = 1L;
}
