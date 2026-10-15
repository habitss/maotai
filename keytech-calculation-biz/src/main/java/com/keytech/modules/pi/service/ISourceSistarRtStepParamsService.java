package com.keytech.modules.pi.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.entity.SourceSistarRtStepParams;
import com.keytech.modules.pi.entity.SourceSistarRtSteps;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: Adapter数据-步骤明细
 * @Author: jeecg-boot
 * @Date:   2025-08-31
 * @Version: V1.0
 */
public interface ISourceSistarRtStepParamsService extends BaseService<SourceSistarRtStepParams> {

    List<SourceSistarRtStepParams> selectIncrementalDataWithPaging(Timestamp lastLoadTime, Timestamp currentTime, long offset, long pageSize);

    int processBatchData(List<SourceSistarRtSteps> dataList);
}
