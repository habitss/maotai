package com.keytech.modules.kpi.vo;

import com.keytech.modules.kpi.entity.KpiPlanData;
import lombok.Data;

import java.util.List;

/**
 * KPI树统计
 */
@Data
public class ControlConfigurationVo {
    //部门编号
    private String deptCode;
    private String perentId;
    private String parentName;
    private String kpiCode;
    private String kpiName;
    private String platoOneId;
    private String platoOneName;
    private String platoTwoId;
    private String platoTwoName;
    //三级柏拉图主键
    private String platoThreeKey;
    private String platoThreeId;
    //控制活动
    private String controlActivity;
    private String piCode;
    private String platoThreeName;
    //标准
    private String standard;
    //单位
    private String unit;
    //年差累值
    private String yearValue;
    private Object piControlValues;
    //排班数据
    private List<KpiPlanData> stringListMap;
    //年月
    private List<YearDataVo> yearDataVos;

}
