package com.keytech.modules.kpi.service;

import com.keytech.modules.kpi.entity.KpiThirdQualityData;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: KPI第三方接口-质量KPI
 * @Author: jeecg-boot
 * @Date:   2025-08-18
 * @Version: V1.0
 */
public interface IKpiThirdQualityDataService extends BaseService<KpiThirdQualityData> {

    void qualityData(List<KpiThirdQualityData> kpiThirdQualityDataList);
}
