package com.keytech.modules.pi.service;


import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.common.MeasurementData;
import com.keytech.modules.pi.entity.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * pi-监控模型
 * </p>
 *
 * @author HQ
 * @since 2024-12-5 14:34:22
 */
public interface IPiComputationMonitorModelService extends BaseService<PiComputationMonitorModel> {

    /**
     * 根据计算信号id集合查询所有的监控模型集合
     * @param calculateIds
     * @return
     */
    List<PiComputationMonitorModel> queryListBycalculateIds(List<String> calculateIds);

}
