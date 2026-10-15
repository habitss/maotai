package com.keytech.modules.sop.service;

import com.keytech.modules.sop.entity.SopDiagnoseData;
import com.keytech.base.service.BaseService;
import com.keytech.modules.sop.entity.SopStatisticData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Description: SOP诊断计划-工作诊断
 * @Author: jeecg-boot
 * @Date:   2025-04-26
 * @Version: V1.0
 */
public interface ISopDiagnoseDataService extends BaseService<SopDiagnoseData> {

    Result<?> diagnosticPostSave(SopDiagnoseData sopDiagnoseData);

    Result<?> getDiagnosticPost(String sopStatisticId, String week, String year);

}
