package com.keytech.modules.kpi.service;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.json.JSONObject;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.kpi.dto.KPiConfigDto;
import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.dto.KpiTypeDto;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.base.service.BaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description: KPI清单
 * @Author: jeecg-boot
 * @Date:   2025-02-07
 * @Version: V1.0
 */
public interface IKpiInfoService extends BaseService<KpiInfo> {

    //查询KPI树
    Object listTree(KpiTypeDto dto);

    //添加
    void kpiSave(KpiInfo kpiInfo);

    //单位部门树
    Object queryDeptTreeList(String isRoot, String factory);

    //KPI清单导出
    ModelAndView kpiInfoExportXls(HttpServletRequest request, KpiInfo kpiInfo);


    //KPI导入
    Result<?> kpiImportExcel(HttpServletRequest request, HttpServletResponse response,Class<KpiInfo> kpiInfoClass) throws IOException;

    List<KPiConfigDto> listKpi(KpiReportValueDto dto);

    /**
     * KPI排序
     * @param kpiTypeDto
     * @param kpiCodeMap
     * @return
     */
    <T> List<T> kpiSortList(KpiTypeDto kpiTypeDto, Map<String, T> kpiCodeMap);

    /**
     * 获取KPI树基础数据
     * @param kpiInfo
     * @return
     */
    Result<?> kpiControlConfiguration(KpiInfo kpiInfo);

    List<KpiInfo> listKpiInfo(KpiInfo kpiInfo);

    /**
     * 获取KPI信息
     * @param factory
     * @param dept
     * @param year
     * @return
     */
    Map<String, KpiInfo> kpiNameMapping(String factory, String dept, String year);

    /**
     * 复制
     * @param kpiInfo
     * @return
     */
    Map<String, String> copyData(KpiInfo kpiInfo);

    /**
     * KPI信息查询
     * @param kpiCodeList
     * @return
     */
    List<KpiInfo> listReprot(Set<String> kpiCodeList);

    /**
     * 获取KPI树
     * @param dto
     * @return
     */
    List<Tree<String>> kpiTree(KpiTypeDto dto);
}
