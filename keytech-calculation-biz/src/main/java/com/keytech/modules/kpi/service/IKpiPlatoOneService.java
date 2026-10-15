package com.keytech.modules.kpi.service;

import com.keytech.modules.kpi.entity.KpiPlatoOne;
import com.keytech.base.service.BaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.util.Map;

/**
 * @Description: 一级柏拉图（生产流程）
 * @Author: jeecg-boot
 * @Date:   2025-02-10
 * @Version: V1.0
 */
public interface IKpiPlatoOneService extends BaseService<KpiPlatoOne> {

    void platoOneSave(KpiPlatoOne kpiPlatoOne);

    //级柏拉图 导入
    Result<?> platoOneImportExcel(HttpServletRequest request, HttpServletResponse response, Class<KpiPlatoOne> kpiPlatoOneClass);

    Map<String, String> listMapByKpiCode(String kpiCode,String year);
}
