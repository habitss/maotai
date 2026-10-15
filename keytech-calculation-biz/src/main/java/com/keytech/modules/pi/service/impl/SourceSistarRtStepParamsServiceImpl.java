package com.keytech.modules.pi.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.pi.entity.PiSistarRtStepParams;
import com.keytech.modules.pi.entity.SourceSistarRtStepParams;
import com.keytech.modules.pi.entity.SourceSistarRtSteps;
import com.keytech.modules.pi.mapper.PiSistarRtStepParamsMapper;
import com.keytech.modules.pi.mapper.SourceSistarRtStepParamsMapper;
import com.keytech.modules.pi.service.IPiSistarRtStepLogService;
import com.keytech.modules.pi.service.IPiSistarRtStepParamsService;
import com.keytech.modules.pi.service.IPiSistarRtStepsService;
import com.keytech.modules.pi.service.ISourceSistarRtStepParamsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: Adapter数据-步骤明细
 * @Author: jeecg-boot
 * @Date:   2025-08-31
 * @Version: V1.0
 */
@Service
@Slf4j
public class SourceSistarRtStepParamsServiceImpl extends BaseServiceImpl<SourceSistarRtStepParamsMapper, SourceSistarRtStepParams> implements ISourceSistarRtStepParamsService {

    @Autowired
    private SourceSistarRtStepParamsMapper sourceSistarRtStepParamsMapper;

    @DS("bmsqlservice")
    @Override
    public List<SourceSistarRtStepParams> selectIncrementalDataWithPaging(Timestamp lastLoadTime, Timestamp currentTime, long offset, long pageSize) {
        return sourceSistarRtStepParamsMapper.selectIncrementalDataWithPaging(lastLoadTime, currentTime, offset, pageSize);
    }

    @Override
    public int processBatchData(List<SourceSistarRtSteps> dataList) {
        return 0;
    }
}
