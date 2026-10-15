package com.keytech.modules.serviceAgreement.entity;

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
 * @Description: 服务协议登记表
 * @Author: aolei
 * @Date:   2025-03-12
 * @Version: V1.0
 */
@Data
@TableName("mes3_service_agreement_input")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="服务协议登记表")
public class ServiceAgreementInput implements Serializable {


	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**租户ID*/
    @Schema(description = "租户ID")
    private String tenantId;

	/**工厂Code*/
	@Excel(name = "工厂", width = 15,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "工厂Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String factoryCode;

	/**部门Code*/
	@Excel(name = "部门(*)", width = 15,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "部门")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String deptCode;

	/**产线Code*/
	@Excel(name = "产线", width = 15 ,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "产线Code")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

	/**提供方部门*/
	@Excel(name = "提供方部门(*)", width = 15,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "提供方部门")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String provideDept;

	/**产品/服务*/
	@Excel(name = "产品/服务(*)", width = 15)
    @Schema(description = "产品/服务")
    private String product;

	/**活动*/
	@Excel(name = "活动(*)", width = 15)
    @Schema(description = "活动")
    private String activity;

	/**接收方部门*/
	@Excel(name = "接收方部门(*)", width = 15,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "接收方部门")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String receiveDept;

	/**服务度量指标*/
	@Excel(name = "服务度量指标", width = 15)
    @Schema(description = "服务度量指标")
    private String serviceIndicator;

	/**单位*/
	@Excel(name = "单位", width = 15)
    @Schema(description = "单位")
    private String unit;

	/**目标值*/
	@Excel(name = "目标值", width = 15)
    @Schema(description = "目标值")
    private String targetValue;

	/**提供人*/
	@Excel(name = "提供人(*)", width = 15,  dictTable ="sys_user", dicText = "realname", dicCode = "id")
    @Schema(description = "提供人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String provideBy;

	/**接收人*/
	@Excel(name = "接收人(*)", width = 15,  dictTable ="sys_user", dicText = "realname", dicCode = "id")
    @Schema(description = "接收人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String receiveBy;

	/**提供方责任人*/
	@Excel(name = "提供方责任人", width = 15,  dictTable ="sys_user", dicText = "realname", dicCode = "id")
    @Schema(description = "提供方责任人")
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "id")
    private String provideDutyBy;

	/**确认状态： 0否 1是*/
	@Excel(name = "确认状态： 0否 1是", width = 15)
    @Schema(description = "确认状态： 0否 1是")
    private Integer confirmStatus;

	/**使用状态： 0否 1是*/
	@Excel(name = "使用状态： 0否 1是", width = 15)
    @Schema(description = "使用状态： 0否 1是")
    private Integer useStatus;

	/**月目标值*/
    @Schema(description = "月目标值")
    private String monthTargetValue;

    /**创建时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "启用时间")
    private Date startDate;

    /**修改时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "停用时间")
    private Date endDate;

	/**扩展配置*/
    @Schema(description = "扩展配置")
    private String extJson;

	/**备注*/
	@Excel(name = "备注", width = 15)
    @Schema(description = "备注")
    private String remark;

	/**排序*/
	@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Integer sort;

	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

	/**修改时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "修改时间")
    private Date updateTime;

	/**创建人*/
    @Schema(description = "创建人")
    private String createBy;

	/**修改人*/
    @Schema(description = "修改人")
    private String updateBy;

	/**删除状态(0未删,1已删)*/
    @Schema(description = "删除状态(0未删,1已删)")
    @TableLogic
    private Integer delFlag;

    @TableField(exist = false)
    private String title;
    @TableField(exist = false)
    private Integer year;
    @TableField(exist = false)
    private int jan ;
    @TableField(exist = false)
    private int feb ;
    @TableField(exist = false)
    private int mar ;
    @TableField(exist = false)
    private int apr ;
    @TableField(exist = false)
    private int may ;
    @TableField(exist = false)
    private int jun ;
    @TableField(exist = false)
    private int jul ;
    @TableField(exist = false)
    private int aug ;
    @TableField(exist = false)
    private int sep ;
    @TableField(exist = false)
    private int oct ;
    @TableField(exist = false)
    private int nov ;
    @TableField(exist = false)
    private int dec ;

    @TableField(exist = false)
    private String provideDept_text;

    @TableField(exist = false)
    private String receiveDept_text;

    private static final long serialVersionUID = 1L;
}
