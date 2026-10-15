package com.keytech.modules.period.service;

import com.keytech.modules.period.entity.PeriodVerificationConfig;
import com.keytech.modules.period.entity.PeriodVerificationDevice;
import com.keytech.base.service.BaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.io.IOException;
import java.util.List;

/**
 * @Description: 期间核查设备信息
 * @Author: jeecg-boot
 * @Date:   2025-03-09
 * @Version: V1.0
 */
public interface IPeriodVerificationDeviceService extends BaseService<PeriodVerificationDevice> {

     void addPeriodVerificationConfig(List<PeriodVerificationConfig> list, PeriodVerificationDevice periodVerificationDevice, String fieldType, String fieldName, double sort);
    /**
     * 自定义导入
     */
    Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<PeriodVerificationDevice> clazz) throws IOException;

    /**
     * 仪器管理-通过id删除
     * @param ids
     */
    void removeByIds(List<String> ids);
}
