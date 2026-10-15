package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiSistarRtBatches;
import com.keytech.base.service.BaseService;

/**
 * @Description: BM-实时订单记录表
 * @Author: jeecg-boot
 * @Date:   2025-09-23
 * @Version: V1.0
 */
public interface IPiSistarRtBatchesService extends BaseService<PiSistarRtBatches> {

    void extractData();
}
