package com.keytech.modules.ems.service;

import com.keytech.modules.ems.entity.AggregationStatus;
import java.util.List;

public interface IAggregationCalcExecutor {

    /**
     * 根据指定状态集合，按时间段分组批量计算
     * @param statusList 待计算状态
     */
    void execute(List<AggregationStatus> statusList);

}
