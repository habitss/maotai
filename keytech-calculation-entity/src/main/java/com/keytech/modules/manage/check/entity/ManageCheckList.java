package com.keytech.modules.manage.check.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 管理者检查清单-分层检查清单
 * @Author: jeecg-boot
 * @Date:   2025-03-31
 * @Version: V1.0
 */
@Data
@TableName("mes3_manage_check_list")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="管理者检查清单-分层检查清单")
public class ManageCheckList implements Serializable {
	/**id*/
    @Excel(name = "主键", width = 15)
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**租户id*/
    @Schema(description = "租户id")
    private Integer tenantId;

	/**工厂编码*/
    @Schema(description = "工厂编码")
    private String factoryCode;

	/**部门编码*/
    @Schema(description = "部门编码")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

	/**产线编码*/
    @Schema(description = "产线编码")
    private String lineCode;

	/**检查时间*/
	@Excel(name = "日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "检查时间")
    private Date checkDate;

	/**所属周*/
    @Schema(description = "所属周")
    private Integer week;

	/**检查层级*/
	@Excel(name = "检查层级", width = 15)
    @Schema(description = "检查层级")
    private String checkScope;

	/**检查类型： 1 管理者检查清单 2 其他*/
	@Excel(name = "检查类型： 1 管理者检查清单 2 其他", width = 15)
    @Schema(description = "检查类型： 1 管理者检查清单 2 其他")
    @Dict(dictTable ="mes3_mdm_post", dicText = "post_name", dicCode = "id")
    private String checkType;

	/**检查人*/
	@Excel(name = "检查人", width = 15,dictTable ="sys_user",dicText = "realname",dicCode = "id")
    @Schema(description = "检查人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String checkUser;

	/**检查人部门*/
	@Excel(name = "检查人部门", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    @Schema(description = "检查人部门")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    private String checkUserDept;

	/**检查人岗位*/
	@Excel(name = "检查人岗位", width = 15, dictTable ="mes3_mdm_post", dicText = "post_name", dicCode = "id")
    @Schema(description = "检查人岗位")
    @Dict(dictTable ="mes3_mdm_post", dicText = "post_name", dicCode = "id")
    private String checkUserPost;

	/**责任部门*/
	@Excel(name = "责任部门", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    @Schema(description = "责任部门")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    private String checkOwnerDept;

	/**责任岗位*/
	@Excel(name = "责任岗位", width = 15, dictTable ="mes3_mdm_post", dicText = "post_name", dicCode = "id")
    @Schema(description = "责任岗位")
    @Dict(dictTable ="mes3_mdm_post", dicText = "post_name", dicCode = "id")
    private String checkOwnerPost;

	/**责任班组*/
	@Excel(name = "责任班组", width = 15, dictTable ="mes3_mdm_shift_group",dicText = "group_name",dicCode = "id")
    @Schema(description = "责任班组")
    @Dict(dictTable ="mes3_mdm_shift_group",dicText = "group_name",dicCode = "id")
    private String checkOwnerClassGroup;

	/**直接责任人*/
	@Excel(name = "责任人", width = 15,dictTable ="sys_user",dicText = "realname",dicCode = "id")
    @Schema(description = "直接责任人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String checkOwner;

	/**责任管理人*/
	@Excel(name = "责任管理人", width = 15, dictTable ="sys_user",dicText = "realname",dicCode = "id")
    @Schema(description = "责任管理人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String checkOwnerAdmin;

	/**问题图片*/
	@Excel(name = "问题图片", width = 15)
    @Schema(description = "问题图片")
    private String problemImg;

	/**问题描述*/
	@Excel(name = "问题描述", width = 15)
    @Schema(description = "问题描述")
    private String problemDesc;

	/**问题分类*/
	@Excel(name = "问题分类", width = 15, dictTable ="mes3_manage_check_category", dicText = "category_name", dicCode = "code")
    @Schema(description = "问题分类")
    @Dict(dictTable ="mes3_manage_check_category", dicText = "category_name", dicCode = "code")
    private String problemType;

	/**分类子项*/
	@Excel(name = "分类子项", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_SUB_CLASS'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "分类子项")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_SUB_CLASS'", dicText = "item_key", dicCode = "item_val")
    private String problemSubclass;

	/**行动/其他*/
	@Excel(name = "行动/其他", width = 15)
    @Schema(description = "行动/其他")
    private String extJson;

	/**5s检查*/
	@Excel(name = "5s检查点", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_5S_POINT'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "5s检查")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_5S_POINT'", dicText = "item_key", dicCode = "item_val")
    private String fivesCheck;

	/**整改图片*/
    @Schema(description = "整改图片")
    private String optimizeImg;

	/**整改后说明*/
	@Excel(name = "整改后说明", width = 15)
    @Schema(description = "整改后说明")
    private String optimizeDesc;

	/**检查人岗位区域*/
	@Excel(name = "检查人岗位区域", width = 15)
    @Schema(description = "检查人岗位区域")
    private String checkUserPostRegion;

	/**责任人岗位区域*/
	@Excel(name = "责任人岗位区域", width = 15)
    @Schema(description = "责任人岗位区域")
    private String ownerPostRegion;

	/**创建人*/
    @Schema(description = "创建人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "username")
    private String createBy;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;

	/**修改时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "修改时间")
    private Date updateTime;

	/**删除状态(0未删,1已删)*/
    @Schema(description = "删除状态(0未删,1已删)")
    @TableLogic
    private String delFlag;

    @TableField(exist = false)
    private ActionPlanTrack actionPlanTrack;
    @TableField(exist = false)
    private String checkDate_begin;
    @TableField(exist = false)
    private String checkDate_end;
    @TableField(exist = false)
    private String createTime_begin;
    @TableField(exist = false)
    private String createTime_end;
    /**用于替代id*/
    @TableField(exist = false)
    private String rowId;
    @TableField(exist = false)
    @Excel(name = "是否进入车间行动计划", width = 15)
    private String enterAction;
    @TableField(exist = false)
    private String[] problemTypeArr;
    //是否查询部门子级
    @TableField(exist = false)
    private Integer queryDeptChild;
    //是否去重
    @TableField(exist = false)
    private Integer removeRepeat=0;

    private static final long serialVersionUID = 1L;
}
