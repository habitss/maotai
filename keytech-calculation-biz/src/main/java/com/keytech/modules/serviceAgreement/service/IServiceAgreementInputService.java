package com.keytech.modules.serviceAgreement.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.serviceAgreement.entity.ServiceAgreementInput;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.io.IOException;
import java.util.List;

/**
 * @Description: 服务协议登记表
 * @Author: jeecg-boot
 * @Date:   2025-03-12
 * @Version: V1.0
 */
public interface IServiceAgreementInputService extends BaseService<ServiceAgreementInput> {

    List<ServiceAgreementInput> trackingList( List<ServiceAgreementInput> list, Integer year);

    /**
     * 自定义导入
     */
    Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<ServiceAgreementInput> clazz) throws IOException;
}
