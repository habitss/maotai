package com.keytech.modules.pi.dto;

import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;

import java.util.Map;


/**
 * PI周月累统计对象
 */
@Data
public class PiMonWeekSummaryDto {

    /**产线编码*/
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "org_code")
    private String lineCode;

    /**报表名称-编码**/
    private String tableCode;

    /**报表名称-中文*/
    private String tableName;

    /**PI名称-编码*/
    private String fieldCode;

    /**PI名称-中文*/
    private String fieldName;

    /**绿区数*/
    private String green;

    /**黄区数*/
    private String yellow;

    /**红区数*/
    private String red;

    /**剔除数*/
    private String grey;

    /**合计数*/
    private String all;

    /**绿区率目标值*/
    private String greenRateTarget;

    /**绿区率*/
    private String greenProportion;

    /**黄区率*/
    private String yellowProportion;

    /**红区率*/
    private String redProportion;

    /**异常总数*/
    private Integer anomalyTotal;

    /**异常类型数量*/
    private Map<String, Integer> anomalyTypeMap;

    /**重复异常数*/
    private Integer anomalyRepeatTotal;

    /**重复异常率*/
    private String anomalyRepeatRate;

    /**班组评价OK数*/
    private Integer teamEvaluationTotal;

    /**5w分析数*/
    private Integer fiveWhyTotal;

    /**5w分析率（修正）*/
    private String fiveWhyAmendRate = "0.00%";

    /**5w分析率（实际）*/
    private String fiveWhyActualRate = "0.00%";

    /**车间5w评审数*/
    private String fiveWhyWorkshopEvaluationTotal = "0";

    /**车间5w评价OK数*/
    private String fiveWhyWorkshopEvaluationOkTotal = "0";

    /**车间5w自查合格数*/
    private String fiveWhySelfCheckOkTotal = "0";

    /**偏差分位值-5分位值*/
    private String fivePercentile;

    /**偏差分位值-10分位值*/
    private String tenPercentile;

    /**偏差分位值-50分位值*/
    private String fiftyPercentile;

    /**偏差分位值-90分位值*/
    private String ninetyPercentile;

    /**偏差分位值-95分位值*/
    private String ninetyFivePercentile;
}
