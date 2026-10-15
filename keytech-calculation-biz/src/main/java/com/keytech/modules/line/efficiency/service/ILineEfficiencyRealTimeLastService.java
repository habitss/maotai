package com.keytech.modules.line.efficiency.service;

import com.keytech.modules.line.efficiency.entity.LineEfficiencyRealTimeLast;
import com.keytech.base.service.BaseService;

/**
 * @Description: 实时计算逻辑-最新记录表
 * @Author: jeecg-boot
 * @Date:   2025-09-05
 * @Version: V1.0
 */
public interface ILineEfficiencyRealTimeLastService extends BaseService<LineEfficiencyRealTimeLast> {

    void executeCip();
}
