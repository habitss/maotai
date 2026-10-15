package com.keytech.modules.kpi.service;

import com.keytech.modules.kpi.entity.KpiConsistencyConfig;
import com.keytech.base.service.BaseService;

import java.util.Map;

/**
 * @Description: 一致KPI数据配置
 * @Author: jeecg-boot
 * @Date:   2026-03-24
 * @Version: V1.0
 */
public interface IKpiConsistencyConfigService extends BaseService<KpiConsistencyConfig> {

    /**
     * -致KPI数据计算定时任务
     */
    void extractData(Map map);
}
