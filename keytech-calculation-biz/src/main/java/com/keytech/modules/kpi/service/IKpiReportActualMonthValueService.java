package com.keytech.modules.kpi.service;

import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.entity.KpiReportActualMonthValue;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: kpi报表实际值月表
 * @Author: jeecg-boot
 * @Date:   2025-02-24
 * @Version: V1.0
 */
public interface IKpiReportActualMonthValueService extends BaseService<KpiReportActualMonthValue> {

    /**
     * 更新月表
     * @param monthValue
     * @param compareValue
     */
    void saveOrUpdateBy(KpiReportActualMonthValue monthValue, String compareValue);


    /**
     * 获取年月数据
     * @param dto
     * @param kpiCodeIds
     * @return
     */
    List<KpiReportActualMonthValue> listMonthDataBy(KpiReportValueDto dto, List<String> kpiCodeIds);

    /**
     * 获取年月数据 年份
     * @return
     */
    List<KpiReportActualMonthValue> listMonthDataByYear(KpiReportValueDto dto, List<String> kpiCodeList);
}
