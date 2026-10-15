package com.keytech.modules.kpi.dto;

import lombok.Data;

import java.util.List;

/**
 * @Description: TODO
 * @Author: sjw
 * @Date: 2025/3/9 16:29
 */
@Data
public class KpiDiffAnalysisDto {
    //kpi
    private String kpiCode;
    private Boolean showMore5W = false;
    String month;
    String reportId;
    String recordId;
}
