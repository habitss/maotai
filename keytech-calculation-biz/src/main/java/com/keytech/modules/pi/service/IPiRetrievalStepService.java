package com.keytech.modules.pi.service;


import com.keytech.base.service.BaseService;

import com.keytech.modules.pi.entity.PiCalculateSignal;
import com.keytech.modules.pi.entity.PiRetrievalStep;

import java.util.List;

/**
 * <p>
 * pi-取值步骤
 * </p>
 *
 * @author 敖磊
 * @since 2024-11-15 09:34:22
 */
public interface IPiRetrievalStepService extends BaseService<PiRetrievalStep> {

    /**
     * 根据计算信号id集合查询所有的取值信号集合
     * @param calculateIds
     * @return
     */
    List<PiRetrievalStep> queryListBycalculateIds(List<String> calculateIds);

}
