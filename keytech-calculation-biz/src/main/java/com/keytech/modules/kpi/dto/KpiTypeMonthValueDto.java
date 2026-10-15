package com.keytech.modules.kpi.dto;


import lombok.Data;

/**
 * 月目标值
 */
@Data
public class KpiTypeMonthValueDto {

    private String janValue;
    private String febValue;
    private String marValue;
    private String aprValue;
    private String mayValue;
    private String junValue;
    private String julValue;
    private String augValue;
    private String sepValue;
    private String octValue;
    private String novValue;
    private String decValue;
    private String kpiName;
    private String lastYear;
    private String yearTarget;
    private String yearBatch;
    //当前年目标
    private String currentYearTarget;
}
