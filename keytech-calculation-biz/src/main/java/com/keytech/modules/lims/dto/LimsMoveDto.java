package com.keytech.modules.lims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * LIMS
 * 行动计划--->推送给lims
 */
@Data
public class LimsMoveDto {
    //行动计划ID：LIMS系统自动生成唯一标识
     private String ID;
     //任务编号
     private String ACTIONNO;
     //任务状态
     private String STATUS;
     //任务来源类型
     private String SOURCETYPE;
     //任务来源编号
     private String SOURCENO;
     //任务来源标题
     private String SOURCETITLE;
     //任务内容
     private String CONTENT;
     //计划完成时间
     private String PLANFINISHTIME;
     //执行部门
     private String CHARGEORG;
     //执行部门ID
     private String CHARGEORGID;
     //负责人<-执行人
     private String CHARGEPERSON;
     //负责人ID <-执行人ID
     private String CHARGEPERSONID;
     //任务类型(1：预防，2：纠正)
     private String PLANTYPE;
     //任务完成情况
     private String PLANFINISHDETAIL;
     //实际完成时间
     private String FINISHTIME;
     //完成情况验证
     private String DETAILCHECK;
     //验证部门
     private String CHECKORG;
     //验证部门ID
     private String CHECKORGID;
     //验证人
     private String CHECKPERSON;
     //验证人ID
     private String CHECKPERSONID;
     //验证时间
     @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
     private Date CHECKTIME;
     //备注
     private String REMARK;
     //制单人编码
     private String CREATEDBYID;
     //制单人名称
     private String CREATEDBYNAME;
     //制单时间
     private String CREATEDTIME;
     //制单人单位编码
     private String CREATEDBYORGID;
     //制单人单位名称
     private String CREATEDBYORGNAME;
     //来源ID
     private String SOURCEID;
     //任务来源编码
     private String SOURCETYPECODE;
     //来源
     private String ACTIONSOURCE;
     //问题分级
     private String PROBLEMLEVEL;
     //任务/问题描述
     private String PROBLEMDESC;
     //责任部门ID
     private String DIRECTORORGID;
     //责任部门
     private String DIRECTORORGNAME;
     //责任人ID
     private String DIRECTORID;
     //责任人
     private String DIRECTORNAME;
     //第一次延期备注
     private String FIRSTDELAYREMARK;
     //第一次延期期限
     private String FIRSTDELAYPERIOD;
     //第二次延期备注
     private String SECONDDELAYREMARK;
     //第二次延期期限
     private String SECONDDELAYPERIOD;
     //第三次延期备注
     private String THIRDDELAYREMARK;
     //第三次延期期限
     private String THIRDDELAYPERIOD;
     //任务下发时间
     @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
     private Date ISSUEDTIME;
     //任务下发人ID
     private String ISSUEDUSERID;
     //任务下发人
     private String ISSUEDUSERNAME;
     //执行提交时间
     @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
     private Date EXECUTIONSUBMITTIME;
     //验证提交时间
     @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
     private Date VERIFYSUBMITTIME;
     //取消时间
     @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
     private Date CANCELTIME;
     //取消操作人ID
     private String CANCELUSERID;
     //取消操作人
     private String CANCELUSERNAME;
     //重新激活时间
     @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
     private Date REACTIVATEDTIME;
     //重新激活操作人ID
     private String REACTIVATEDUSERID;
     //重新激活操作人
     private String REACTIVATEDUSERNAME;
     //驳回次数
     private String REJECTNUM;
     //提出人ID
     private String FOUNDERID;
     //提出人
     private String FOUNDERNAME;
     //提出时间
     private String INITIALTIME;

}
