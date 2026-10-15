package com.keytech.modules.kpi.vo;

import lombok.Data;

/**
 * KPI异常清单-统计
 * @date 2023/05/05/19:51
 */
@Data
public class ExcPercentVo {

    //总数
    private int ExcSum;
    //已分析
    private int ExcCount;
    //统计
    private double percent;
}
