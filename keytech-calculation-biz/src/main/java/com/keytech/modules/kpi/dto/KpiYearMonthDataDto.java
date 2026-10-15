package com.keytech.modules.kpi.dto;

import lombok.Data;

/**
 * @FileName KpiYearMonthDataDto
 * @Description KPI年月数据DTO
 * @Author HQ
 * @date 2026-03-26
 **/
@Data
public class KpiYearMonthDataDto {

    private String kpiCode;

    /** 月累 */
    private String monthCumulative;
    /** 月年累 */
    private String yearMonthCumulative;
    /** 周累 */
    private String weekCumulative;

}
