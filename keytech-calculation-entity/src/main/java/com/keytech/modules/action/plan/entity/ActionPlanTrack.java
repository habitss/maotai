package com.keytech.modules.action.plan.entity;

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
 * @Description: 行动计划跟踪
 * @Author: jeecg-boot
 * @Date:   2025-01-08
 * @Version: V1.0
 */
@Data
@TableName("mes3_action_plan_track")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="行动计划跟踪")
public class ActionPlanTrack implements Serializable {


	/**ID*/
	@TableId(type = IdType.AUTO)
    @Excel(name = "ID", width = 15, orderNum = "0")
    @Schema(description = "ID")
    private Integer id;

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

    @Schema(description="产线Code")
    @TableField("line_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @NotBlank(message = "产线Code为空")
    private String lineCode;

	/**工段*/
    @Schema(description = "工段")
    private String section;

	/**提出日期*/
	@Excel(name = "提出日期(必填)", width = 15, format = "yyyy-MM-dd", orderNum = "1")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "提出日期")
    private Date raiseDate;

    /**不合格kpi指标*/
    @Excel(name = "不合格kpi指标", width = 15,dictTable ="mes3_kpi_info",dicText = "kpi_name",dicCode = "kpi_code", orderNum = "6")
    @Dict(dictTable ="mes3_kpi_info",dicText = "kpi_name",dicCode = "kpi_code")
    @Schema(description = "不合格kpi指标")
    private String pkiInfo;

	/**重要性评价 - 数据字典(MES3_ACTION_PLAN_IMPORTANCE_EVALUATION)*/
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_IMPORTANCE_EVALUATION'", dicText = "item_key", dicCode = "item_val")
    @Excel(name = "重要性评价", width = 15, orderNum = "8", dictTable ="mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_IMPORTANCE_EVALUATION'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "重要性评价 - 数据字典(MES3_ACTION_PLAN_IMPORTANCE_EVALUATION)")
    private String importanceEvaluation;

	/**问题分类*/
    @Dict(dictTable ="mes3_action_plan_problem_type", dicText = "problem_name", dicCode = "id")
	@Excel(name = "问题分类(必填)", width = 15, orderNum = "2", dictTable ="mes3_action_plan_problem_type", dicText = "problem_name", dicCode = "id")
    @Schema(description = "问题分类")
    private String problemTypeId;

	/**任务/问题描述*/
	@Excel(name = "任务/问题描述(必填)", width = 15, orderNum = "7")
    @Schema(description = "任务/问题描述")
    private String problemDescription;

	/**行动*/
	@Excel(name = "行动(必填)", width = 15, orderNum = "9")
    @Schema(description = "行动")
    private String actionTitle;

	/**提出人id*/
    @Schema(description = "提出人id")
    @Excel(name = "提出人(必填)", width = 15, orderNum = "3", dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String putUserId;

	/**提出人姓名*/
    @Schema(description = "提出人姓名")
    private String putUserName;

	/**提出部门编码*/
    @Dict(dictTable ="sys_depart", dicText = "depart_name", dicCode = "id")
	@Excel(name = "提出部门(必填)", width = 15, orderNum = "4", dictTable ="sys_depart", dicText = "depart_name", dicCode = "id")
    @Schema(description = "提出部门编码")
    private String putDeptId;

	/**提出人激励*/
	@Excel(name = "提出人激励", width = 15, orderNum = "5")
    @Schema(description = "提出人激励")
    private String putKind;

	/**责任人id*/
    @Schema(description = "责任人id")
    @Excel(name = "责任人(必填)", width = 15, orderNum = "10", dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String dutyUserId;

	/**责任人姓名*/
    @Schema(description = "责任人姓名")
    private String dutyUserName;

	/**责任部门编码*/
    @Dict(dictTable ="sys_depart", dicText = "depart_name", dicCode = "id")
    @Excel(name = "责任部门(必填)", width = 15, orderNum = "11", dictTable ="sys_depart", dicText = "depart_name", dicCode = "id")
    @Schema(description = "责任部门编码")
    private String dutyDeptId;

	/**责任人激励*/
	@Excel(name = "责任人激励", width = 15, orderNum = "12")
    @Schema(description = "责任人激励")
    private String dutyKind;

	/**执行人id*/
    @Excel(name = "执行人", width = 15, orderNum = "13", dictTable ="sys_user",dicText = "realname",dicCode = "id")
    @Schema(description = "执行人id")
    private String executeUserId;

	/**执行人姓名*/
    @Schema(description = "执行人姓名")
    private String executeUserName;

	/**执行部门编码*/
    @Dict(dictTable ="sys_depart", dicText = "depart_name", dicCode = "id")
    @Excel(name = "执行部门", width = 15, orderNum = "14", dictTable ="sys_depart", dicText = "depart_name", dicCode = "id")
    @Schema(description = "执行部门编码")
    private String executeDeptId;

	/**执行人激励*/
    @Schema(description = "执行人激励")
    private String executeKind;

	/**确认人id*/
    @Excel(name = "确认人(必填)", width = 15, orderNum = "20", dictTable ="sys_user",dicText = "realname",dicCode = "id")
    @Schema(description = "确认人id")
    private String verifyUserId;

	/**确认人姓名*/
    @Schema(description = "确认人姓名")
    private String verifyUserName;

	/**确认部门编码*/
    @Dict(dictTable ="sys_depart", dicText = "depart_name", dicCode = "id")
    @Excel(name = "确认部门(必填)", width = 15, orderNum = "21", dictTable ="sys_depart", dicText = "depart_name", dicCode = "id")
    @Schema(description = "确认部门编码")
    private String verifyDeptId;

	/**确认人激励*/
    @Schema(description = "确认人激励")
    private String verifyKind;

	/**计划完成日期*/
	@Excel(name = "计划完成日期(必填)", width = 15, orderNum = "17", format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "计划完成日期")
    private Date planFinishDate;

	/**实际完成日期*/
	@Excel(name = "实际完成日期", width = 15, orderNum = "18", format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "实际完成日期")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date practicalFinishDate;

    /**行动级别*/
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_LEVEL'", dicText = "item_key", dicCode = "item_val")
    @Excel(name = "行动级别(必填)", width = 15, orderNum = "15", dictTable ="mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_LEVEL'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "行动级别")
    private String actionLevel;

    /**行动措施*/
    @Excel(name = "行动措施", width = 15, orderNum = "16", replace = { "预防_1", "纠正_2", "_" })
    @Schema(description = "行动措施")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String actionMeasures;

	/**进入会议-字典（MES3_ACTION_PLAN_MEETING）*/
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_MEETING'", dicText = "item_key", dicCode = "item_val")
	@Excel(name = "是否进入会议(必填)", width = 15, orderNum = "16", dictTable ="mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_MEETING'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "进入会议-字典（MES3_ACTION_PLAN_MEETING）")
    private String enterMeeting;

	/**完成情况*/
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_FINISH'", dicText = "item_key", dicCode = "item_val")
	@Excel(name = "完成情况", width = 15, orderNum = "22", dictTable ="mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_FINISH'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "完成情况")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String finishState;

	/**完成情况确认*/
	@Excel(name = "完成情况确认", width = 15, orderNum = "23", replace = { "待确认_1", "合格_2", "不合格_3", "_" })
    @Schema(description = "完成情况确认")
    private String finishVerifyState;

    /**文件对象*/
    @Schema(description = "整改前图片")
    private String fileObjBefore;

    /**文件对象*/
    @Schema(description = "整改后图片")
    private String fileObj;

	/**文件路径*/
    @Schema(description = "文件上传&下载")
    private String pathText;

	/**文件名称*/
    @Schema(description = "文件名称")
    private String pathTitle;

	/**延期次数*/
    @Schema(description = "延期次数")
    private Integer delayCount;

	/**第一次延期备注*/
	@Excel(name = "第一次延期备注", width = 15, orderNum = "28")
    @Schema(description = "第一次延期备注")
    private String oneText;

	/**第一次延期期限*/
	@Excel(name = "第一次延期期限", width = 15, format = "yyyy-MM-dd", orderNum = "29")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "第一次延期期限")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date oneTime;

	/**第二次延期备注*/
	@Excel(name = "第二次延期备注", width = 15, orderNum = "30")
    @Schema(description = "第二次延期备注")
    private String twoText;

	/**第二次延期期限*/
	@Excel(name = "第二次延期期限", width = 15, format = "yyyy-MM-dd", orderNum = "31")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "第二次延期期限")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date twoTime;

	/**第三次延期备注*/
	@Excel(name = "第三次延期备注", width = 15, orderNum = "32")
    @Schema(description = "第三次延期备注")
    private String threeText;

	/**第三次延期期限*/
	@Excel(name = "第三次延期期限", width = 15, format = "yyyy-MM-dd", orderNum = "33")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "第三次延期期限")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date threeTime;

	/**剩余天数*/
	@Excel(name = "剩余天数", width = 15, orderNum = "19")
    @Schema(description = "剩余天数")
    private String surplusDay;

	/**措施类别*/
    @Schema(description = "措施类别")
    private String measureCategory;

	/**考核得分*/
    @Schema(description = "考核得分")
    private String score;

	/**1:有效 0:无效*/
    @Schema(description = "1:有效 0:无效")
    private String status;

    /**5W的数据id*/
    @Schema(description = "dataId")
    private String dataId;

    /**5W的措施id*/
    @Schema(description = "5W的措施id")
    private String fiveWhyMeasuresId;

	/**排序*/
    @Schema(description = "排序")
    private Double sort;

	/**备注说明*/
	@Excel(name = "备注", width = 15, orderNum = "24")
    @Schema(description = "备注")
    private String remark;

	/**删除标识*/
    @Schema(description = "删除标识")
    @TableLogic
    private Integer delFlag;

	/**创建人*/
    @Schema(description = "创建人")
    @Dict(dictTable ="sys_user", dicText = "realname", dicCode = "username")
    @Excel(name = "创建人", width = 15, orderNum = "37", dictTable ="sys_user",dicText = "realname",dicCode = "username")
    private String createBy;

	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
    private Date createTime;

	/**创建人部门*/
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "创建人部门")
    private String createDept;

	/**更新人*/
    @Excel(name = "最后更新人", width = 15, orderNum = "35", dictTable ="sys_user",dicText = "realname",dicCode = "username")
    @Schema(description = "更新人")
    @Dict(dictTable ="sys_user", dicText = "realname", dicCode = "username")
    private String updateBy;

	/**更新日期*/
    @Excel(name = "最后更新时间", width = 15, format = "yyyy-MM-dd HH:mm:ss", orderNum = "36")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
    private Date updateTime;

    /**修改人部门*/
    @Schema(description = "修改人部门")
    private String updateDept;

    /**相关部门查询条件*/
    @TableField(exist = false)
    private String deptIds;
    /**相关人查询条件*/
    @TableField(exist = false)
    private String userIds;

    private static final long serialVersionUID = 1L;
}
