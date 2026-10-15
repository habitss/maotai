package com.keytech.modules.kpi.service;

import com.keytech.modules.kpi.entity.KpiThirdPlayData;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: 第三方接口-维护KPI
 * @Author: jeecg-boot
 * @Date:   2025-03-13
 * @Version: V1.0
 */
public interface IKpiThirdPlayDataService extends BaseService<KpiThirdPlayData> {

    /**
     * 维护KPI数据抽取
     */
    void synchronizeMonthWeekValueJob();

    /**
     * 不达标KPI推送
     * @param list
     */
    void kpiNotStandard(List<Long> list);

    /**
     * 不达标KPI5why分析推送
     */
    void notStandardKpiFwhyJob();
}
