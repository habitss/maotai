package com.keytech.modules.pi.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.pi.entity.SourceSistarRtBatches;
import com.keytech.modules.pi.entity.SourceSistarRtStepParams;
import com.keytech.modules.pi.entity.SourceSistarRtSteps;
import com.keytech.modules.pi.mapper.SourceSistarRtBatchesMapper;
import com.keytech.modules.pi.mapper.SourceSistarRtStepParamsMapper;
import com.keytech.modules.pi.service.ISourceSistarRtBatchesService;
import com.keytech.modules.pi.service.ISourceSistarRtStepParamsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: Adapter数据-批次
 * @Author: jeecg-boot
 * @Date:   2025-08-31
 * @Version: V1.0
 */
@Service
@Slf4j
public class SourceSistarRtBatchesServiceImpl extends BaseServiceImpl<SourceSistarRtBatchesMapper, SourceSistarRtBatches> implements ISourceSistarRtBatchesService {

    @Autowired
    private SourceSistarRtBatchesMapper sourceSistarRtBatchesMapper;

    @DS("bmsqlservice")
    @Override
    public List<SourceSistarRtBatches> selectIncrementalDataWithPaging(Timestamp lastLoadTime, Timestamp currentTime, long offset, long pageSize) {
        return sourceSistarRtBatchesMapper.selectIncrementalDataWithPaging(lastLoadTime, currentTime, offset, pageSize);
    }

}
