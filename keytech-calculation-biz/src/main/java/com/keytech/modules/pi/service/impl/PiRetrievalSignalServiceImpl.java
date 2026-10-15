package com.keytech.modules.pi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.pi.entity.PiRetrievalSignal;
import com.keytech.modules.pi.mapper.PiRetrievalSignalMapper;
import com.keytech.modules.pi.service.IPiRetrievalSignalService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * pi-取值信号
 * </p>
 *
 * @author 敖磊
 * @since 2024-11-14 22:34:22
 */
@Service
public class PiRetrievalSignalServiceImpl extends BaseServiceImpl<PiRetrievalSignalMapper, PiRetrievalSignal> implements IPiRetrievalSignalService {

    /**
     * 根据计算信号id集合查询所有的取值信号集合
     * @param calculateIds
     * @return
     */
    @Override
    public List<PiRetrievalSignal> queryListBycalculateIds(List<String> calculateIds){
        List<PiRetrievalSignal> piRetrievalSignalList = new ArrayList<>();
        if(null != calculateIds && calculateIds.size() > 0){
            LambdaQueryWrapper<PiRetrievalSignal> queryWrapper = Wrappers.lambdaQuery(PiRetrievalSignal.class);
            queryWrapper.in(PiRetrievalSignal::getCalculateId,calculateIds);
            queryWrapper.orderByAsc(PiRetrievalSignal::getCreateTime);
            piRetrievalSignalList = list(queryWrapper);
        }
        return piRetrievalSignalList;
    }
}
