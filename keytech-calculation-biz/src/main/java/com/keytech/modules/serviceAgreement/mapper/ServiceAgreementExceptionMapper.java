package com.keytech.modules.serviceAgreement.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.serviceAgreement.entity.ServiceAgreementException;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 服务协议异常记录表
 * @Author: jeecg-boot
 * @Date:   2025-03-12
 * @Version: V1.0
 */
@Mapper
public interface ServiceAgreementExceptionMapper extends BaseMapper<ServiceAgreementException> {

}
