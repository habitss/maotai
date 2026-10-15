package com.keytech.modules.kpi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * KPI分类-清单Dto
 */
@Data
public class KPiConfigDto {

    private String id;

    /**KPI分类-id*/
    private String typeId;
    /**KPI分类-分类名称*/
    private String kpiType;
    /**KPI分类-父级(顶层)*/
    private String typeParentId;

    /**KPI清单-id*/
    private String kpiId;
    /**KPI清单-KPICode*/
    private String kpiCode;
    /**KPI清单-KPI名称*/
    private String kpiName;
    /**KPI清单-分类id*/
    private String kpiPerentId;
    /**KPI清单-单位*/
    private String unit;
    /**KPI清单-父级(KPI清单上级)*/
    private String kpiParentId;
    /**KPI清单-保留小数位数*/
    private Integer decimalNum;

    /**KPI清单-录入排序*/
    private Double inputSort;
    /**KPI清单-日报排序*/
    private Double reportSort;

    /**KPI-录入权限*/
    private String inputPermission;
    /**KPI-月目标值配置*/
    private String kpiTypeMonthValue;
    /**KPI-周目标值配置*/
    private String kpiTypeWeekValue;

    /**目标R*/
    private String provideBy;
    /**目标A*/
    private String possessBy;
    /**目标C*/
    private String consultedBy;
    /**目标I*/
    private String informedBy;

    /**KPI职责分工R*/
    private String dutyProvideBy;
    /**KPI职责分工A*/
    private String dutyPossessBy;
    /**KPI职责分工C*/
    private String dutyConsultedBy;
    /**KPI职责分工I*/
    private String dutyInformedBy;

    /**截止时间描述*/
    private String deadline;

//    private String parentId;
//    private String perentId;
    private String reportId;
    private String extJson;
    private String orgCode;
    private String orgName;
    private Integer active;
    private String value;

}
