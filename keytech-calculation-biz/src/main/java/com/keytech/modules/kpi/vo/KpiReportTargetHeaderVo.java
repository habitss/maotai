package com.keytech.modules.kpi.vo;

import lombok.Data;

@Data
public class KpiReportTargetHeaderVo {

    private String id;
    private String kpiCode;
    private String reportId;
    private String kpiName;
    private String lastYear = "";
    private String yearTarget = "";
    private String janValue = "";
    private String febValue = "";
    private String marValue = "";
    private String aprValue = "";
    private String mayValue = "";
    private String junValue = "";
    private String julValue = "";
    private String augValue = "";
    private String sepValue = "";
    private String octValue = "";
    private String novValue = "";
    private String decValue = "";
    private String possessBy;
    /**
     * 提供人
     */
    //@ApiModelProperty(value = "提供人")
    private String provideBy;
    //@ApiModelProperty(value = "截止时间")
    private String deadline;
    private String remark;
}
