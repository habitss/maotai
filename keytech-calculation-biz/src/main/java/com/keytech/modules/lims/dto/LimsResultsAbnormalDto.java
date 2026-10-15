package com.keytech.modules.lims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.keytech.modules.lims.deserializer.CustomDateDeserializer;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * LIMS与系统对照
 */
@Data
public class LimsResultsAbnormalDto {

    /**LIMS流程ID：超标不合格记录唯一标识*/
    @JsonProperty(value = "LIMSFLOWID")
    private String limsFlowId;
    /**LIMS工厂代码*/
    @JsonProperty(value = "FACTORYCODE")
    private String limsFactoryCode;
    /**LIMS工厂名称*/
    @JsonProperty(value = "FACTORYNAME")
    private String limsFactoryName;
    /**责任部门(取样品来源采样点所属部门名称)*/
    @JsonProperty(value = "DEPT")
    private String limsDept;
    /**报告日期：超标不合格信息推送日期 [改成推送时间]*/
    @JsonProperty(value = "REPORTDATE")
//    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date reportDate;
    /**批号：样品批号*/
    @JsonProperty(value = "BATCHNO")
    private String batchNo;
    /**检验类别*/
    @JsonProperty(value = "CHECKCATEGORY")
    private String checkCategory;
    /**品种：样品名称*/
    @JsonProperty(value = "MTLNAME")
    private String mtlName;
    /**数据来源：质量管理部*/
    @JsonProperty(value = "DATASOURCE")
    private String dataSource;
    /**供应商*/
    @JsonProperty(value = "VENDORNAME")
    private String vendorName;
    /**反馈人：检验结果提交人*/
    @JsonProperty(value = "FEEDPERSON")
    private String feedPerson;
    /**KPI类别*/
    @JsonProperty(value = "KPICATEGORY")
    private String kpiCategory;
    /**RCI/CI/SCI*/
    @JsonProperty(value = "ICATEGORY")
    private String icategory;
    /**红区/黄区：OOS-A代表红区、OOS-B代表黄区*/
    @JsonProperty(value = "ZONE")
    private String zone;
    /**超标及不合格项目：指标名称*/
    @JsonProperty(value = "TESTNAME")
    private String testName;
    /**检测结果*/
    @JsonProperty(value = "REPORTRESULT")
    private String reportResult;
    /**红区下限*/
    @JsonProperty(value = "LOWLIMITA")
    private String lowLimita;
    /**黄区下限*/
    @JsonProperty(value = "LOWLIMITB")
    private String lowLimitb;
    /**目标值*/
    @JsonProperty(value = "TARGETVALUE")
    private String targetValue;
    /**黄区上限*/
    @JsonProperty(value = "HIGHLIMITB")
    private String highLimitb;
    /**红区上限*/
    @JsonProperty(value = "HIGHLIMITA")
    private String highLimita;
    /**字符标准*/
    @JsonProperty(value = "CHARSTANDARD")
    private String charsTandard;
    /**化验室/车间数据确认：异常排查数据拼接*/
    @JsonProperty(value = "CONFIRM")
    private String confirm;
    /**是否隔离：RCI指标到红区为是，其他为否*/
    @JsonProperty(value = "QUARANTINE")
    private String quarantine;
    /**根本原因：PI系统返回，推送空值即可*/
    @JsonProperty(value = "REASON")
    private String reason;
    /**触发级别：提交超标不合格检验结果时手动选择*/
    @JsonProperty(value = "TRIGGERLEVEL")
    private String triggerLevel;
    /**是否为质量事件：提交超标不合格检验结果时手工选择*/
    @JsonProperty(value = "QUALITYEVENTFLAG")
    private String qualityEventFlag;
    /**车间部门经理审批时间*/
    @JsonProperty(value = "MGRAPPROVALTIME")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date mgrapprovalTime;

    /**责任生产线*/
    @JsonProperty(value = "DEVICENAME")
    private String deviceName;

    /**取样点*/
    @JsonProperty(value = "SAMPLEDOTNAME")
    private String sampledotName;

    /**分析时间*/
    @JsonProperty(value = "ANALYSISTIME")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date analysisTime;

    /**是否作废(初始时null，作废是1，取消作废0)*/
    @JsonProperty(value = "CANCELFLAG")
    private String cancelFlag;

    /** 作废原因*/
    @JsonProperty(value = "CANCELREASON")
    private String cancelReason;

}
