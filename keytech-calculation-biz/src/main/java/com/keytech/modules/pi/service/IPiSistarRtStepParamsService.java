package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiSistarRtStepParams;
import com.keytech.base.service.BaseService;

/**
 * @Description: Adapter数据-步骤明细
 * @Author: jeecg-boot
 * @Date:   2025-08-31
 * @Version: V1.0
 */
public interface IPiSistarRtStepParamsService extends BaseService<PiSistarRtStepParams> {

    /**
     * 定时任务接口-Adapter数据抽取
     */
    void extractData();
}
