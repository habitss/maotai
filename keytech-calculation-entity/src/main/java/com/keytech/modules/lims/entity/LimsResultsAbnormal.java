package com.keytech.modules.lims.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
import com.keytech.modules.common.DataBase;
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
 * @Description: 超标不合格台账(LIMS)
 * @Author: HQ
 * @Date:   2025-03-17
 * @Version: V1.0
 */
@Data
@TableName("mes3_lims_results_abnormal")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="超标不合格台账(LIMS)")
public class LimsResultsAbnormal extends DataBase implements Serializable {


	/**LIMS流程ID：超标不合格记录唯一标识*/
	@Excel(name = "LIMS流程ID：超标不合格记录唯一标识", width = 15)
    @Schema(description = "LIMS流程ID：超标不合格记录唯一标识")
    private String limsFlowId;

	/**LIMS工厂代码*/
	@Excel(name = "LIMS工厂代码", width = 15)
    @Schema(description = "LIMS工厂代码")
    private String limsFactoryCode;

	/**LIMS工厂名称*/
	@Excel(name = "LIMS工厂名称", width = 15)
    @Schema(description = "LIMS工厂名称")
    private String limsFactoryName;

	/**LIMS责任部门(取样品来源采样点所属部门名称)*/
	@Excel(name = "LIMS责任部门(名称)", width = 15)
    @Schema(description = "LIMS责任部门(名称)")
    private String limsDept;

	/**报告日期：超标不合格信息推送日期[改成推送时间]*/
	@Excel(name = "报告日期：超标不合格信息推送日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "报告日期：超标不合格信息推送日期")
    private Date reportDate;

	/**批号：样品批号*/
	@Excel(name = "批号：样品批号", width = 15)
    @Schema(description = "批号：样品批号")
    private String batchNo;

	/**检验类别*/
	@Excel(name = "检验类别", width = 15)
    @Schema(description = "检验类别")
    private String checkCategory;

	/**品种：样品名称*/
	@Excel(name = "品种：样品名称", width = 15)
    @Schema(description = "品种：样品名称")
    private String mtlName;

	/**数据来源：质量管理部*/
	@Excel(name = "数据来源：质量管理部", width = 15)
    @Schema(description = "数据来源：质量管理部")
    private String dataSource;

	/**供应商*/
	@Excel(name = "供应商", width = 15)
    @Schema(description = "供应商")
    private String vendorName;

	/**反馈人：检验结果提交人*/
	@Excel(name = "反馈人：检验结果提交人", width = 15)
    @Schema(description = "反馈人：检验结果提交人")
    private String feedPerson;

	/**KPI类别*/
	@Excel(name = "KPI类别", width = 15)
    @Schema(description = "KPI类别")
    private String kpiCategory;

	/**RCI/CI/SCI*/
	@Excel(name = "RCI/CI/SCI", width = 15)
    @Schema(description = "RCI/CI/SCI")
    private String icategory;

	/**红区/黄区：OOS-A代表红区、OOS-B代表黄区*/
	@Excel(name = "红区/黄区：OOS-A代表红区、OOS-B代表黄区", width = 15)
    @Schema(description = "红区/黄区：OOS-A代表红区、OOS-B代表黄区")
    private String zone;

	/**超标及不合格项目：指标名称*/
	@Excel(name = "超标及不合格项目：指标名称", width = 15)
    @Schema(description = "超标及不合格项目：指标名称")
    private String testName;

	/**检测结果*/
	@Excel(name = "检测结果", width = 15)
    @Schema(description = "检测结果")
    private String reportResult;

	/**红区下限*/
	@Excel(name = "红区下限", width = 15)
    @Schema(description = "红区下限")
    private String lowLimita;

	/**黄区下限*/
	@Excel(name = "黄区下限", width = 15)
    @Schema(description = "黄区下限")
    private String lowLimitb;

	/**目标值*/
	@Excel(name = "目标值", width = 15)
    @Schema(description = "目标值")
    private String targetValue;

	/**黄区上限*/
	@Excel(name = "黄区上限", width = 15)
    @Schema(description = "黄区上限")
    private String highLimitb;

	/**红区上限*/
	@Excel(name = "红区上限", width = 15)
    @Schema(description = "红区上限")
    private String highLimita;

	/**字符标准*/
	@Excel(name = "字符标准", width = 15)
    @Schema(description = "字符标准")
    private String charsTandard;

	/**化验室/车间数据确认：异常排查数据拼接*/
	@Excel(name = "化验室/车间数据确认：异常排查数据拼接", width = 15)
    @Schema(description = "化验室/车间数据确认：异常排查数据拼接")
    private String confirm;

	/**是否隔离：RCI指标到红区为是，其他为否*/
	@Excel(name = "是否隔离：RCI指标到红区为是，其他为否", width = 15)
    @Schema(description = "是否隔离：RCI指标到红区为是，其他为否")
    private String quarantine;

	/**根本原因：PI系统返回，推送空值即可*/
	@Excel(name = "根本原因：PI系统返回，推送空值即可", width = 15)
    @Schema(description = "根本原因：PI系统返回，推送空值即可")
    private String reason;

    /**根本原因状态：1通过  0待审批  2不通过*/
    @Excel(name = "根本原因状态", width = 15)
    @Schema(description = "根本原因状态")
    private String reasonStatus;

	/**触发级别：提交超标不合格检验结果时手动选择*/
	@Excel(name = "触发级别：提交超标不合格检验结果时手动选择", width = 15)
    @Schema(description = "触发级别：提交超标不合格检验结果时手动选择")
    private String triggerLevel;

	/**是否为质量事件：提交超标不合格检验结果时手工选择*/
	@Excel(name = "是否为质量事件：提交超标不合格检验结果时手工选择", width = 15)
    @Schema(description = "是否为质量事件：提交超标不合格检验结果时手工选择")
    private String qualityEventFlag;

	/**车间部门经理审批时间*/
	@Excel(name = "车间部门经理审批时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "车间部门经理审批时间")
    private Date mgrapprovalTime;

	/**是否5why分析1已分析  0未分析 */
	@Excel(name = "是否5why分析1已分析  0未分析 ", width = 15)
    @Schema(description = "是否5why分析1已分析  0未分析 ")
    private String fiveWhyStatus;

	/**是否进入行动1已记录  0未记录*/
	@Excel(name = "是否进入行动1已记录  0未记录", width = 15)
    @Schema(description = "是否进入行动1已记录  0未记录")
    private String actionStatus;

	/**审批状态 1通过  0 待审批 2不通过*/
	@Excel(name = "审批状态 1通过  0 待审批", width = 15)
    @Schema(description = "审批状态 1通过  0 待审批")
    private Integer examineStatus;

	/**行动分析*/
	@Excel(name = "行动分析", width = 15)
    @Schema(description = "行动分析")
    private String downData;

	/**5why分析*/
	@Excel(name = "5why分析", width = 15)
    @Schema(description = "5why分析")
    private String reasonWhy;

	/**分析结果ID(5why ID)*/
	@Excel(name = "分析结果ID", width = 15)
    @Schema(description = "分析结果ID")
    private String analysisId;

	/**审批人code*/
	@Excel(name = "审批人code", width = 15)
    @Schema(description = "审批人code")
    @Dict(dictTable = "sys_user", dicCode = "id", dicText = "realname")
    private String approvalCode;

	/**责任生产线*/
	@Excel(name = "责任生产线", width = 15)
    @Schema(description = "责任生产线")
    private String deviceName;

	/**取样点*/
	@Excel(name = "取样点", width = 15)
    @Schema(description = "取样点")
    private String sampledotName;

	/**完成状态*/
	@Excel(name = "完成状态", width = 15)
    @Schema(description = "完成状态")
    private String completeStatus;

	/**完成进度*/
	@Excel(name = "完成进度", width = 15)
    @Schema(description = "完成进度")
    private String completeProgress;

	/**最后异常推送状态(0未推送1已推送) */
	@Excel(name = "最后异常推送状态(0未推送1已推送) ", width = 15)
    @Schema(description = "最后异常推送状态(0未推送1已推送) ")
    private String lastStatus;

	/**推送异常日志*/
	@Excel(name = "推送异常日志", width = 15)
    @Schema(description = "推送异常日志")
    private String sendLog;

	/**分析时间*/
	@Excel(name = "分析时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "分析时间")
    private Date analysisTime;

	/**是否作废(初始时null，作废是1，取消作废0) */
	@Excel(name = "是否作废", width = 15)
    @Schema(description = "是否作废")
    private String cancelFlag;

	/**分析时间*/
	@Excel(name = "分析时间", width = 15)
    @Schema(description = "分析时间")
    private String cancelReason;

    private static final long serialVersionUID = 1L;

    /**分析时效符合度(24h)[分析时间-推送时间]*/
    @TableField(exist = false)
    private String analyzeTimeliness;
    /**审核时效符合度(72h)[分析时间-审核时间]*/
    @TableField(exist = false)
    private String auditTimeliness;

    //时间起
    @TableField(exist = false)
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date checkDateBegin;

    //时间止
    @TableField(exist = false)
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date checkDateEnd;

    //审批人名称
    @TableField(exist = false)
    private String approvalName;
}
