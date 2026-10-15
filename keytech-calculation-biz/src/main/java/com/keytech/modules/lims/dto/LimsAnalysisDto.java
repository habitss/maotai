package com.keytech.modules.lims.dto;

import lombok.Data;

/**
 * LIMS
 * 车间分析排查-推送给lims
 */
@Data
public class LimsAnalysisDto {
     //行动计划ID：LIMS系统自动生成唯一标识
     private String LIMSFLOWID;
     //根本原因：PI系统返回，推送空值即可
     private String REASON;
     //车间部门经理审批时间
     private String MGRAPPROVALTIME;
     //分析时间
     private String ANALYSISTIME;
}
