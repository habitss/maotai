package com.keytech.modules.kpi.service;

import com.keytech.modules.kpi.entity.KpiPlanData;
import com.keytech.base.service.BaseService;
import com.keytech.modules.kpi.vo.ControlConfigurationVo;
import com.keytech.modules.kpi.vo.YearDataVo;

import java.util.List;
import java.util.Map;

/**
 * @Description: KPI树-基础数据
 * @Author: jeecg-boot
 * @Date:   2025-03-07
 * @Version: V1.0
 */
public interface IKpiPlanDataService extends BaseService<KpiPlanData> {


    /**
     * 根据年份和三级ID查询年度数据
     * @param year
     * @param threeId
     * @return
     */
    List<YearDataVo> yearData(String year, String threeId);


    /**
     * 获取KPI 柏拉图
     * @param month
     * @param kpiCode
     * @return
     */
    List<?> treeData(String month, String kpiCode);

    /**
     * KPI树基础数据-存储
     * @param controlConfigurationVos
     * @return
     */
    List<KpiPlanData> batchAddOrEdit(List<ControlConfigurationVo> controlConfigurationVos);
}
