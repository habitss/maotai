package com.keytech.modules.kpi.service;

import com.keytech.modules.kpi.entity.KpiPlatoTwo;
import com.keytech.base.service.BaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

/**
 * @Description: 二级柏拉图（控制点）
 * @Author: jeecg-boot
 * @Date:   2025-02-10
 * @Version: V1.0
 */
public interface IKpiPlatoTwoService extends BaseService<KpiPlatoTwo> {


    //二级柏拉图 保存
    void platoTwoSave(KpiPlatoTwo kpiPlatoTwo);

    //二级柏拉图 导出
    Result<?> platoTwoImportExcel(HttpServletRequest request, HttpServletResponse response, Class<KpiPlatoTwo> kpiPlatoTwoClass);
}
