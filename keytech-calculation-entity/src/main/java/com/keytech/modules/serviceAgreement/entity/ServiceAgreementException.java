package com.keytech.modules.serviceAgreement.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 服务协议异常记录表
 * @Author: jeecg-boot
 * @Date:   2025-03-12
 * @Version: V1.0
 */
@Data
@TableName("mes3_service_agreement_exception")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="服务协议异常记录表")
public class ServiceAgreementException implements Serializable {


	/**租户ID*/
    @Schema(description = "租户ID")
    private String tenantId;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;

	/**工厂Code*/
	@Excel(name = "工厂Code", width = 15,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "工厂Code")
    private String factoryCode;

	/**部门Code*/
	@Excel(name = "部门Code", width = 15,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "部门Code")
    private String deptCode;

	/**产线Code*/
	@Excel(name = "产线Code", width = 15,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "产线Code")
    private String lineCode;

	/**协议id*/
    @Schema(description = "协议id")
    @Dict(dictTable ="mes3_service_agreement_input where del_flag=0 ",dicText = "product",dicCode = "id")
    private String serviceAgreementId;

    /**提供方部门*/
    @Excel(name = "提供方部门", width = 15,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "提供方部门")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String provideDept;

    /**产品/服务*/
    @Excel(name = "产品/服务", width = 15)
    @Schema(description = "产品/服务")
    private String product;

    /**活动*/
    @Excel(name = "活动", width = 15)
    @Schema(description = "活动")
    private String activity;

    /**接收方部门*/
    @Excel(name = "接收方部门", width = 15,dictTable ="sys_depart", dicText = "depart_name", dicCode = "org_code")
    @Schema(description = "接收方部门")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String receiveDept;

	/**异常日期*/
	@Excel(name = "异常日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "异常日期")
    private Date exceptionDate;

	/**异常描述*/
	@Excel(name = "异常描述", width = 15)
    @Schema(description = "异常描述")
    private String exceptionDesc;

	/**异常文件*/
	@Excel(name = "异常文件", width = 15)
    @Schema(description = "异常文件")
    private String exceptionFile;

	/**影响的KPI*/
	@Excel(name = "影响的KPI", width = 15,dictTable ="mes3_kpi_info",dicText = "kpi_name",dicCode = "kpi_code")
    @Schema(description = "影响的KPI")
    @Dict(dictTable ="mes3_kpi_info",dicText = "kpi_name",dicCode = "kpi_code")
    private String affectKpiCode;

	/**影响的PI*/
    @Schema(description = "影响的PI")
    private String affectPiTable;

	/**影响的PI*/
	@Excel(name = "影响的PI", width = 15,dictTable ="mes3_pi_customize_table_field",dicText = "name",dicCode = "id")
    @Schema(description = "影响的PI")
    @Dict(dictTable ="mes3_pi_customize_table_field",dicText = "name",dicCode = "id")
    private String affectPiCode;

	/**是否5why分析： 0否 1是*/
	@Excel(name = "是否5why分析： 0否 1是", width = 15)
    @Schema(description = "是否5why分析： 0否 1是")
    private Integer fiveWhyStatus;

	/**是否进入行动： 0否 1是*/
	@Excel(name = "是否进入行动： 0否 1是", width = 15)
    @Schema(description = "是否进入行动： 0否 1是")
    private Integer actionStatus;

	/**审批状态： 1 待审批 1 通过*/
	@Excel(name = "审批状态： 0 待审批 1 通过", width = 15)
    @Schema(description = "审批状态： 0 待审批 1 通过")
    private Integer examineStatus;

	/**确认状态： 0 未确认 1 已确认*/
	@Excel(name = "确认状态： 0 未确认 1 已确认", width = 15)
    @Schema(description = "确认状态： 0 未确认 1 已确认")
    private Integer confirmStatus;

	/**确认备注*/
	@Excel(name = "确认备注", width = 15)
    @Schema(description = "确认备注")
    private String remark;

	/**确认附件*/
	@Excel(name = "确认附件", width = 15)
    @Schema(description = "确认附件")
    private String confirmFile;

    @Schema(description = "5why分析id")
    private String analysisId;

	/**排序*/
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
    @Dict(dictTable ="sys_user", dicText = "realname", dicCode = "username")
    private String createBy;

	/**修改人*/
    @Schema(description = "修改人")
    private String updateBy;

	/**删除状态(0未删,1已删)*/
    @Schema(description = "删除状态(0未删,1已删)")
    @TableLogic
    private Integer delFlag;

    @TableField(exist = false)
    private Integer type;

    private static final long serialVersionUID = 1L;
}
