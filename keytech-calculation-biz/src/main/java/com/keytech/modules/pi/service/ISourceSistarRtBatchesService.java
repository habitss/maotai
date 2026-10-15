package com.keytech.modules.pi.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.entity.SourceSistarRtBatches;
import com.keytech.modules.pi.entity.SourceSistarRtStepParams;
import com.keytech.modules.pi.entity.SourceSistarRtSteps;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: Adapter数据-批次
 * @Author: jeecg-boot
 * @Date:   2025-08-31
 * @Version: V1.0
 */
public interface ISourceSistarRtBatchesService extends BaseService<SourceSistarRtBatches> {

    List<SourceSistarRtBatches> selectIncrementalDataWithPaging(Timestamp lastLoadTime, Timestamp currentTime, long offset, long pageSize);

}
