package com.keytech.modules.sop.entity;

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

/**
 * @Description: SOP管理回顾
 * @Author: jeecg-boot
 * @Date:   2025-04-14
 * @Version: V1.0
 */
@Data
@TableName("mes3_sop_review_plan")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="SOP管理回顾")
public class SopReviewPlan implements Serializable {

    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Excel(name = "主键（勿删）", width = 15)
    @Schema(description = "id")
    private String id;

	/**租户ID*/
    @Schema(description = "租户ID")
    private Integer tenantId;

	/**工厂Code*/
    @Schema(description = "工厂Code")
    private String factoryCode;

	/**部门Code*/
	@Excel(name = "部门", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "部门Code")
    @TableField("dept_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

	/**产线Code*/
	@Excel(name = "产线", width = 15, dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    @Schema(description = "产线Code")
    @TableField("line_code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

    /**SOP编号*/
    @TableField(exist = false)
    @Excel(name = "SOP编号", width = 15)
    @Schema(description = "SOP编号")
    private String sopNumber;

    /**SOP名称*/
    @TableField(exist = false)
    @Excel(name = "SOP名称", width = 15)
    @Schema(description = "SOP名称")
    private String sopDesc;

    /**关键SOP*/
    @Excel(name = "关键SOP", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_SOP_CRUX'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "关键SOP")
    private String sopState;

    /**回顾频次*/
    @Excel(name = "回顾频次", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_SOP_FREQUENCY'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "回顾频次")
    private String frequency;

    /**回顾间隔*/
    @Excel(name = "回顾间隔", width = 15, dictTable ="mes3_mdm_dict where group_code = 'MES3_SOP_FREQUENCY'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "回顾间隔")
    private String reviewInterval;

    /**回顾负责人*/
    @Excel(name = "回顾负责人", width = 15 ,dictTable ="sys_user", dicText = "realname", dicCode = "id")
    @Schema(description = "回顾负责人")
    private String headUserId;

    /**回顾责任人*/
    @Excel(name = "回顾责任人", width = 15 ,dictTable ="sys_user", dicText = "realname", dicCode = "id")
    @Schema(description = "回顾责任人")
    private String responsibleUserId;

    /**回顾咨询人*/
    @Excel(name = "回顾咨询人", width = 15 ,dictTable ="sys_user", dicText = "realname", dicCode = "id")
    @Schema(description = "回顾咨询人")
    private String seekUserId;

    /**回顾通知人*/
    @Excel(name = "回顾通知人", width = 15)
    @Schema(description = "回顾通知人")
    private String noticeUserId;



    /**是否为关键SOP*/
    //@Excel(name = "是否为关键SOP", width = 15)
    @Schema(description = "是否为关键SOP")
    private String isSopState;

	/**sop首页关联ID*/
    @Schema(description = "SOP首页关联ID")
    private String sopId;

	/**上一年回顾年份*/
    @Schema(description = "上一年回顾年份")
    private String lastYear;

	/**结果数据*/
    @Schema(description = "结果数据")
    private String resultData;

	/**year*/
	@Excel(name = "所属年份", width = 15)
    @Schema(description = "year")
    private Integer year;

	/**最新版本号*/
    @Schema(description = "最新版本号")
    private String latestVersionNumber;

	/**实施时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "实施时间")
    private Date operateTime;

	/**排序*/
    @Schema(description = "排序")
    private Double sort;

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
    private String createBy;

	/**更新人*/
    @Schema(description = "更新人")
    private String updateBy;


    /**部门集合-查询条件*/
    @TableField(exist = false)
    @Schema(description = "部门集合")
    private String depts;

    /**排序标识*/
    @TableField(exist = false)
    @Schema(description = "排序标识")
    private Boolean isDescending;



    @TableField(exist = false)
    @Schema(description = "SOP活动")
    private String sopActivity;

    @TableField(exist = false)
    @Schema(description = "SOP类型")
    @Dict(dictTable ="mes3_mdm_dict where group_code = 'MES3_SOP_TYPE'", dicText = "item_key", dicCode = "item_val")
    private String sopType;

    @TableField(exist = false)
    @Schema(description = "SOP类型")
    private String sopTypeName;

    @TableField(exist = false)
    @Schema(description = "岗位")
    private String sopPost;


    /**回顾频次-name*/
    @TableField(exist = false)
    @Schema(description = "回顾频次")
    private String frequencyName;

    /**部门-查询*/
    @TableField(exist = false)
    @Schema(description = "部门-查询条件")
    private String deptQuery;
}
