package com.keytech.modules.manage.check.entity;

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
import java.util.Date;
import java.util.List;

/**
 * @Description: 分层检查-检查清单录入主表
 * @Author: jeecg-boot
 * @Date:   2025-04-10
 * @Version: V1.0
 */
@Data
@TableName("mes3_manager_checklist_edit_main")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="分层检查-检查清单录入主表")
public class ManagerChecklistEditMain implements Serializable {


	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

	/**租户id*/
	@Excel(name = "租户id", width = 15)
    @Schema(description = "租户id")
    private Integer tenantId;

	/**工厂*/
	@Excel(name = "工厂", width = 15)
    @Schema(description = "工厂")
    private String factoryCode;

	/**部门*/
	@Excel(name = "部门", width = 15)
    @Schema(description = "部门")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

	/**产线编码*/
	@Excel(name = "产线编码", width = 15)
    @Schema(description = "产线编码")
    private String lineCode;

	/**推送周*/
	@Excel(name = "推送周", width = 15)
    @Schema(description = "推送周")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_WEEK'", dicText = "item_key", dicCode = "item_val")
    private String pushWeek;

	/**记录日期*/
	@Excel(name = "记录日期", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "记录日期")
    private Date recordDate;

	/**记录周*/
	@Excel(name = "记录周", width = 15)
    @Schema(description = "记录周")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_WEEK'", dicText = "item_key", dicCode = "item_val")
    private String recordWeek;

	/**检查人id*/
	@Excel(name = "检查人id", width = 15)
    @Schema(description = "检查人id")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String inspectedBy;

	/**确认人id*/
	@Excel(name = "确认人id", width = 15)
    @Schema(description = "确认人id")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String confirmBy;

	/**确认结果*/
	@Excel(name = "确认结果", width = 15)
    @Schema(description = "确认结果")
    private Integer confirmResult;

	/**结果确认人*/
	@Excel(name = "结果确认人", width = 15)
    @Schema(description = "结果确认人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String resultConfirmBy;

	/**结果确认时间*/
	@Excel(name = "结果确认时间", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结果确认时间")
    private Date resultConfirmTime;

	/**是否录入0未录入 1已录入*/
	@Excel(name = "是否录入0未录入 1已录入", width = 15)
    @Schema(description = "是否录入0未录入 1已录入")
    private String isEdit;

	/**是否停产(停产无需检查) */
	@Excel(name = "是否停产(停产无需检查) ", width = 15)
    @Schema(description = "是否停产(停产无需检查) ")
    private String stopProduction;

	/**岗位id*/
	@Excel(name = "岗位id", width = 15)
    @Schema(description = "岗位id")
    @Dict(dictTable ="mes3_mdm_post",dicText = "post_name",dicCode = "id")
    private String postId;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Double sort;

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

	/**删除标识*/
	@Excel(name = "删除标识", width = 15)
    @Schema(description = "删除标识")
    @TableLogic
    private Integer delFlag;

    /**填报明细**/
    @TableField(exist = false)
    @Schema(description = "填报明细")
    private List<ManagerChecklistEditRecord> managerChecklistEditRecordList;

    private static final long serialVersionUID = 1L;
}
