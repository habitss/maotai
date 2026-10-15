package com.keytech.modules.kpi.service;

import cn.hutool.json.JSONObject;
import com.keytech.base.service.BaseService;
import com.keytech.modules.kpi.dto.KpiDiffAnalysisDto;
import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.entity.KpiReportTargetValue;
import com.keytech.modules.kpi.vo.KpiReportCellVo;

import java.util.List;
import java.util.Map;

/**
 * @Description: kpi报表目标值
 * @Author: jeecg-boot
 * @Date:   2025-02-24
 * @Version: V1.0
 */
public interface IKpiReportTargetValueService extends BaseService<KpiReportTargetValue> {


    /**获取表头*/
    List<KpiReportCellVo> queryHeader(KpiReportTargetValue kpiReportTargetValue);

    /**获取数据*/
    List<Map<String, Object>> queryData(KpiReportValueDto dto);


    /**数据保存*/
    List<KpiReportTargetValue> saveData(KpiReportValueDto kpiReportValueDto);

    /**
     * 获取目标值
     * @param reportId
     * @param year
     * @param kpiCode
     * @param factoryCode
     * @param deptCode
     * @return
     */
    KpiReportTargetValue queryTargetValueBy(String reportId, Integer year, String kpiCode, String factoryCode, String deptCode);

    /***/
    Map<String, String> monthMap(boolean b);


    /**
     * 获取颜色
     * @param targetObject
     * @param actualValue
     * @param targetValue
     * @param compareValue
     * @return
     */
    int getColour(JSONObject targetObject,String actualValue, String targetValue, String compareValue);


    /**
     * 获取目标值
     * @param reportId
     * @param year
     * @param factory
     * @param dept
     * @return
     */
    Map<String, KpiReportTargetValue> queryTargetValue(String reportId, Integer year, String factory, String dept);

    /**
     * 更新目标值
     * @param kpiCode
     * @param year
     * @param key
     * @param value
     */
    Boolean updateTargetValue(String kpiCode, String year, String key, String value);

    /**
     * 目标值调整-查询
     * @param dto
     * @return
     */
    Object monthTracking(KpiDiffAnalysisDto dto);

    /**
     * 目标值调整-保存
     * @param json
     * @return
     */
    void monthTrackingUpdate(JSONObject json);

    /**
     * 获取目标值指定年份
     * @param reportId
     * @param years
     * @param kpiCode
     * @param factory
     * @param deptCode
     * @return
     */
    KpiReportTargetValue queryTargetValueByYear(String reportId, List<Integer> years, String kpiCode, String factory, String deptCode);
}
