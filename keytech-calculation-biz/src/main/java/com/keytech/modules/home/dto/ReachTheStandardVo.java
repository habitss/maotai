package com.keytech.modules.home.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * KPI达标率统计
 * @author hq
 * @date 2025年4月9日
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReachTheStandardVo {
    private String reportId;
    private Integer color;
    private String kpiCode;
    private String kpiName;
    private String value;
}
