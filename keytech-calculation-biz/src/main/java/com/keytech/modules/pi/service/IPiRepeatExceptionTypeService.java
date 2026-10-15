package com.keytech.modules.pi.service;

import com.keytech.modules.pi.dto.PiRepeatCopyDto;
import com.keytech.modules.pi.entity.PiRepeatExceptionType;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: pi重复异常类型配置
 * @Author: jeecg-boot
 * @Date:   2025-12-03
 * @Version: V1.0
 */
public interface IPiRepeatExceptionTypeService extends BaseService<PiRepeatExceptionType> {
    void saveOrUpdateData(PiRepeatExceptionType piRepeatExceptionType);
    void addCopy(PiRepeatCopyDto piRepeatCopyDto);
}
