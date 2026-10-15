package com.keytech.modules.practice.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.practice.entity.BestPracticeFill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.io.IOException;

/**
 * @Description: 最佳实践-填报
 * @Author: jeecg-boot
 * @Date:   2025-03-17
 * @Version: V1.0
 */
public interface IBestPracticeFillService extends BaseService<BestPracticeFill> {
    /**
     * 自定义导入
     */
    Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
