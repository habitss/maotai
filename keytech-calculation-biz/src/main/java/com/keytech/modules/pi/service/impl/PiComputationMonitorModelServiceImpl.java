package com.keytech.modules.pi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.pi.entity.PiComputationModel;
import com.keytech.modules.pi.entity.PiComputationMonitorModel;
import com.keytech.modules.pi.mapper.PiComputationMonitorModelMapper;
import com.keytech.modules.pi.service.IPiComputationMonitorModelService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * pi-监控模型
 * </p>
 *
 * @author HQ
 * @since 2024-12-5 14:34:22
 */
@Service
public class PiComputationMonitorModelServiceImpl extends BaseServiceImpl<PiComputationMonitorModelMapper, PiComputationMonitorModel> implements IPiComputationMonitorModelService {

    /**
     * 根据计算信号id集合查询所有的监控模型集合
     * @param calculateIds
     * @return
     */
    @Override
    public List<PiComputationMonitorModel> queryListBycalculateIds(List<String> calculateIds){
        List<PiComputationMonitorModel> piComputationMonitorList = new ArrayList<>();
        if(null != calculateIds && calculateIds.size() > 0){
            LambdaQueryWrapper<PiComputationMonitorModel> queryWrapper = Wrappers.lambdaQuery(PiComputationMonitorModel.class);
            queryWrapper.in(PiComputationMonitorModel::getCalculateId,calculateIds);
            queryWrapper.orderByAsc(PiComputationMonitorModel::getCreateTime);
            piComputationMonitorList = list(queryWrapper);
        }
        return piComputationMonitorList;
    }
}
