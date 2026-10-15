package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiCurvePartialStandard;
import com.keytech.base.service.BaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.io.IOException;

/**
 * @Description: 曲线局部标准配置
 * @Author: jeecg-boot
 * @Date:   2026-04-16
 * @Version: V1.0
 */
public interface IPiCurvePartialStandardService extends BaseService<PiCurvePartialStandard> {

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    Result<?> importExcel(HttpServletRequest request, HttpServletResponse response, Integer dataSource) throws IOException;

}
