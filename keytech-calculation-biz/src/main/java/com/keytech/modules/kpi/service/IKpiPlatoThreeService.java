package com.keytech.modules.kpi.service;

import com.keytech.modules.kpi.entity.KpiPlatoThree;
import com.keytech.base.service.BaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

/**
 * @Description: 三级柏拉图(PI点)
 * @Author: jeecg-boot
 * @Date:   2025-02-18
 * @Version: V1.0
 */
public interface IKpiPlatoThreeService extends BaseService<KpiPlatoThree> {

    /**
     * 三级柏拉图新增
     * @param kpiPlatoThree
     */
    void addThree(KpiPlatoThree kpiPlatoThree);

    /**
     * 三级柏拉图导入
     * @param request
     * @param response
     * @param kpiPlatoThreeClass
     * @return
     */
    Result<?> platoThreeImportExcel(HttpServletRequest request, HttpServletResponse response, Class<KpiPlatoThree> kpiPlatoThreeClass);
}
