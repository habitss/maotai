package com.keytech.modules.pi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.pi.entity.PiRetrievalSignal;
import com.keytech.modules.pi.entity.PiRetrievalStep;
import com.keytech.modules.pi.mapper.PiRetrievalStepMapper;
import com.keytech.modules.pi.service.IPiRetrievalStepService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * pi-取值步骤
 * </p>
 *
 * @author 敖磊
 * @since 2024-11-15 09:34:22
 */
@Service
public class PiRetrievalStepServiceImpl extends BaseServiceImpl<PiRetrievalStepMapper, PiRetrievalStep> implements IPiRetrievalStepService {

    /**
     * 根据计算信号id集合查询所有的取值信号集合
     * @param calculateIds
     * @return
     */
    @Override
    public List<PiRetrievalStep> queryListBycalculateIds(List<String> calculateIds){
        List<PiRetrievalStep> piRetrievalStepList = new ArrayList<>();
        if(null != calculateIds && calculateIds.size() > 0){
            LambdaQueryWrapper<PiRetrievalStep> queryWrapper = Wrappers.lambdaQuery(PiRetrievalStep.class);
            queryWrapper.in(PiRetrievalStep::getCalculateId,calculateIds);
            queryWrapper.orderByAsc(PiRetrievalStep::getCreateTime);
            piRetrievalStepList = list(queryWrapper);
        }
        return piRetrievalStepList;
    }
}
