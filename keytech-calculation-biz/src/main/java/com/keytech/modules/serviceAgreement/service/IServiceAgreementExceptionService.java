package com.keytech.modules.serviceAgreement.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.lims.entity.LimsResultsAbnormal;
import com.keytech.modules.serviceAgreement.dto.AgreementExceptionDto;
import com.keytech.modules.serviceAgreement.entity.ServiceAgreementException;
import com.keytech.base.service.BaseService;
import com.keytech.modules.serviceAgreement.entity.ServiceAgreementInput;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @Description: 服务协议异常记录表
 * @Author: jeecg-boot
 * @Date:   2025-03-12
 * @Version: V1.0
 */
public interface IServiceAgreementExceptionService extends BaseService<ServiceAgreementException> {

    List<AgreementExceptionDto> listSt(Set<String> keySet, int year);


    IPage<ServiceAgreementException> queryPageList(Page<ServiceAgreementException> page, QueryWrapper<ServiceAgreementException> queryWrapper, HttpServletRequest req);


}
