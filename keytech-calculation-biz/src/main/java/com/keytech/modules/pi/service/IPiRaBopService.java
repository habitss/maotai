package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiRaBop;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: RA-步骤字典
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
public interface IPiRaBopService extends BaseService<PiRaBop> {

    /**
     * 步骤字典数据抽取
     */
    void stepExtract();

    /**
     * 步骤字典数据抽取
     * @param offset
     * @param pageSize
     * @return
     */
    List<PiRaBop> stepDataExtract(long offset, long pageSize);
}
