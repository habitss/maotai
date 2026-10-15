package com.keytech.modules.project.management.entity;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 项目管理
 * @Author: jeecg-boot
 * @Date:   2025-03-28
 * @Version: V1.0
 */
@Data
@TableName("mes3_project_management")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="项目管理")
public class ProjectManagement implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**租户ID*/
	@Excel(name = "租户ID", width = 15)
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂*/
	@Excel(name = "工厂", width = 15)
    @Schema(description = "工厂")
    private String factoryCode;

	/**部门*/
	@Excel(name = "部门", width = 15)
    @Schema(description = "部门")
    private String deptCode;

	/**产线*/
	@Excel(name = "产线", width = 15)
    @Schema(description = "产线")
    private String lineCode;

	/**工段*/
	@Excel(name = "工段", width = 15)
    @Schema(description = "工段")
    private String section;

	/**项目编号*/
	@Excel(name = "项目编号", width = 15)
    @Schema(description = "项目编号")
    private String projectCode;

	/**项目名称*/
	@Excel(name = "项目名称", width = 15)
    @Schema(description = "项目名称")
    private String projectName;

	/**项目等级*/
	@Excel(name = "项目等级", width = 15)
    @Schema(description = "项目等级")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_PROJECT_LEVEL'", dicText = "item_key", dicCode = "item_val")
    private String projectLevel;

	/**项目类型*/
	@Excel(name = "项目类型", width = 15)
    @Schema(description = "项目类型")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_PROJECT_TYPE' and del_flag = 0", dicText = "item_key", dicCode = "item_val")
    private String projectType;

    /**项目来源*/
    @Excel(name = "项目来源", width = 15)
    @Schema(description = "项目来源")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_PROJECT_SOURCE' and del_flag = 0", dicText = "item_key", dicCode = "item_val")
    private String projectSource;

	/**部门id*/
	@Excel(name = "部门", width = 15)
    @Schema(description = "部门id")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    private String sysOrgId;

	/**项目总监*/
	@Excel(name = "项目总监", width = 15)
    @Schema(description = "项目总监")
    private String projectBossName;

	/**项目总监用户id*/
	@Excel(name = "项目总监用户id", width = 15)
    @Schema(description = "项目总监用户id")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String projectBossId;

	/**项目经理*/
	@Excel(name = "项目经理", width = 15)
    @Schema(description = "项目经理")
    private String projectManageName;

	/**项目经理id*/
	@Excel(name = "项目经理id", width = 15)
    @Schema(description = "项目经理id")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String projectManageId;

    /**立项报告文件地址*/
    @Excel(name = "立项报告文件地址", width = 15)
    @Schema(description = "立项报告文件地址")
    private String projectDocument;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**是否删除*/
	@Excel(name = "是否删除", width = 15)
    @Schema(description = "是否删除")
    @TableLogic
    private Integer delFlag;

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

    @TableField(exist = false)
    private String year;

    @TableField(exist = false)
    private String months;

    @TableField(exist = false)
    private List<String> monthList = new ArrayList<>();

    @TableField(exist = false)
    private List<String> projectLevelList = new ArrayList<>();

    @TableField(exist = false)
    private List<String> projectTypelList = new ArrayList<>();

    @TableField(exist = false)
    private List<String> projectSourceList = new ArrayList<>();

    @TableField(exist = false)
    private ProjectManagementActionSummary summary;

    @TableField(exist = false)
    private JSONObject monthSummary;

    @TableField(exist = false)
    private JSONObject weekSummary;

    @TableField(exist = false)
    @Schema(description = "负责人ID")
    private String userId;

    @TableField(exist = false)
    @Schema(description = "负责部门")
    private String leaderDeptIds;

    @TableField(exist = false)
    @Schema(description = "是否查询已完成节点")
    private String isFinishNode;

    private static final long serialVersionUID = 1L;
}
