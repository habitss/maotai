package com.keytech.modules.period.service;

import com.keytech.modules.period.entity.PeriodVerificationConfig;
import com.keytech.base.service.BaseService;
import com.keytech.modules.period.entity.PeriodVerificationDevice;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.io.IOException;

/**
 * @Description: 期间核查填报项配置
 * @Author: jeecg-boot
 * @Date:   2025-03-10
 * @Version: V1.0
 */
public interface IPeriodVerificationConfigService extends BaseService<PeriodVerificationConfig> {

    /**
     * 自定义导入
     */
    Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<PeriodVerificationConfig> clazz) throws IOException;
}
