package com.keytech.modules.pi.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.SourceSistarRtStepsMapper;
import com.keytech.modules.pi.service.ISourceSistarRtStepsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: Adapter-步骤
 * @Author: jeecg-boot
 * @Date:   2025-08-28
 * @Version: V1.0
 */
@Service
public class SourceSistarRtStepsServiceImpl extends BaseServiceImpl<SourceSistarRtStepsMapper, SourceSistarRtSteps> implements ISourceSistarRtStepsService {

    @Autowired
    private SourceSistarRtStepsMapper sourceSistarRtStepsMapper;

    @Override
    @DS("bmsqlservice")
    public List<SourceSistarRtSteps> selectIncrementalDataWithPaging(Timestamp lastLoadTime, Timestamp currentTime, long offset, long pageSize) {
        return sourceSistarRtStepsMapper.selectIncrementalDataWithPaging(lastLoadTime, currentTime, offset, pageSize);
    }

    @Override
    @DS("bmsqlservice")
    public List<PiSistarEngReccats> getReccats() {
        return sourceSistarRtStepsMapper.getReccats();
    }

    @Override
    public void deleteData(String tableName) {
        sourceSistarRtStepsMapper.deleteData(tableName);
    }

    @Override
    @DS("bmsqlservice")
    public List<PiSistarEngPcus> getPcus() {
        return sourceSistarRtStepsMapper.getPcus();
    }

    @Override
    @DS("bmsqlservice")
    public List<PiSistarEngUnits> getUnits() {
        return sourceSistarRtStepsMapper.getUnits();
    }

    @Override
    @DS("bmsqlservice")
    public List<PiSistarEngEops> getEops() {
        return sourceSistarRtStepsMapper.getEops();
    }

    @Override
    @DS("bmsqlservice")
    public List<PiSistarEngRecipes> getRecipes() {
        return sourceSistarRtStepsMapper.getRecipes();
    }
}
