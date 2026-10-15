package com.keytech.modules.kpi.service;

import cn.hutool.json.JSONObject;
import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.dto.PlaceEmphasisVo;
import com.keytech.modules.kpi.entity.KpiReportActualWeekValue;
import com.keytech.base.service.BaseService;
import com.keytech.modules.kpi.vo.KpiReportCellVo;

import java.util.List;
import java.util.Map;

/**
 * @Description: kpi报表实际值周表
 * @Author: jeecg-boot
 * @Date:   2025-02-24
 * @Version: V1.0
 */
public interface IKpiReportActualWeekValueService extends BaseService<KpiReportActualWeekValue> {

    /**
     * 同步周月
     * @param bean
     * @param k
     * @param v
     */
    void addWeekAndMonth(Map<String, Object> bean, String k, Object v);

    /**
     * 获取KPI周月报表头
     * @param dto
     * @return
     */
    List<?> queryKpiReportHeader(KpiReportValueDto dto);

    /**
     * 获取KPI周月报数据
     * @param dto
     * @return
     */
    List<?> listKpiReportData(KpiReportValueDto dto);

    /**
     * KPI颜色重算
     * @param dto
     * @return
     */
    void retryReportData(KpiReportValueDto dto);

    /**
     * 颜色计算
     * @param weekMonthDataList
     * @param year
     */
    void updateWeekAndMonth(List<JSONObject> weekMonthDataList, String year);


    /**
     * 重点关注KPI-首页
     * @param dto
     * @return
     */
    List<PlaceEmphasisVo> factoryActionStatisticsC(KpiReportValueDto dto);

    /**
     * KPI趋势表头
     * @param dto
     * @return
     */
    List<KpiReportCellVo> trendCurveHeader(KpiReportValueDto dto);

    /**
     * KPI趋势数据
     * @param dto
     * @return
     */
    List<JSONObject> trendCurveData(KpiReportValueDto dto);
}
