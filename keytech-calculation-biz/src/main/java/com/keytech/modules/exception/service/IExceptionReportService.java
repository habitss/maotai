package com.keytech.modules.exception.service;

import com.keytech.modules.exception.dto.ExceptionReportVO;
import com.keytech.modules.exception.dto.ExceptionTotalVO;
import com.keytech.modules.exception.dto.ReportViewDTO;
import com.keytech.modules.exception.entity.ExceptionReport;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: 异常处理报告
 * @Author: jeecg-boot
 * @Date:   2025-03-26
 * @Version: V1.0
 */
public interface IExceptionReportService extends BaseService<ExceptionReport> {

    /**
     * 异常包括按月统计
     * @param exceptionReport
     * @return
     */
    List<ReportViewDTO> reportView(ExceptionReportVO exceptionReport);

    /**
     * 异常统计
     * @return
     */
    List<ExceptionTotalVO> reportStatistics(ExceptionReportVO exceptionReport);
}
