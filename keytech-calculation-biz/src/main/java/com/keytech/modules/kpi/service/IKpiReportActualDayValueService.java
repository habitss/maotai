package com.keytech.modules.kpi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.entity.KpiReportActualDayValue;
import com.keytech.base.service.BaseService;
import com.keytech.modules.kpi.entity.KpiReportTargetValue;
import com.keytech.modules.kpi.vo.KpiReportCellVo;

import java.util.List;
import java.util.Map;

/**
 * @Description: KPI实际值
 * @Author: jeecg-boot
 * @Date:   2025-02-24
 * @Version: V1.0
 */
public interface IKpiReportActualDayValueService extends BaseService<KpiReportActualDayValue> {


    /**
     * 获取表头
     * @param kpiReportActualDayValue
     * @return
     */
    List<KpiReportCellVo> queryHeader(KpiReportActualDayValue kpiReportActualDayValue);

    /**
     * 获取数据
     * @param dto
     * @return
     */
    List<?> queryData(KpiReportValueDto dto);

    /**
     * 保存
     * @param mapList
     * @return
     */
    List<?> saveData(List<Map<String, Object>> mapList);

    /**
     * 修改实际值
     * @param kpiCode
     * @param year
     * @param key
     * @param value
     * @return
     */
    Boolean updateActualValue(String kpiCode, String year, String key, String value);

    /**
     * KPI实际值查询(录入查询)
     * @param k
     * @return
     */
    List<Map<String, Object>> actualDataList(KpiReportValueDto k);


    /**
     * 更新数据
     * @param actualDataList
     * @param s
     */
    void updateByImport(List<Map<String, Object>> actualDataList, String s);

    /**
     * 递归获取子节点
     */
    void getChildrenRecursion(Map<String, String> keyIndexMap, List<KpiReportCellVo> listHeader);
}
