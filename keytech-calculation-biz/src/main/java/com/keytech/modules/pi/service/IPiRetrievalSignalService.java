package com.keytech.modules.pi.service;


import com.keytech.base.service.BaseService;

import com.keytech.modules.pi.entity.PiCalculateSignal;
import com.keytech.modules.pi.entity.PiRetrievalSignal;

import java.util.List;

/**
 * <p>
 * pi-取值信号
 * </p>
 *
 * @author 敖磊
 * @since 2024-11-14 22:34:22
 */
public interface IPiRetrievalSignalService extends BaseService<PiRetrievalSignal> {

    /**
     * 根据计算信号id集合查询所有的取值信号集合
     * @param calculateIds
     * @return
     */
    List<PiRetrievalSignal> queryListBycalculateIds(List<String> calculateIds);
}
