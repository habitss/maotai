package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiSistarRtStepLog;
import com.keytech.base.service.BaseService;

import java.sql.Timestamp;

/**
 * @Description: 抽取记录表-最后更新时间
 * @Author: jeecg-boot
 * @Date:   2025-08-31
 * @Version: V1.0
 */
public interface IPiSistarRtStepLogService extends BaseService<PiSistarRtStepLog> {

    /**
     * 获取最后更新时间
     * @param s
     * @return
     */
    Timestamp getLastLoadTime(String s);

    /**
     * 更新最后更新时间
     * @param s
     * @param currentTime
     */
    void updateLastLoadTime(String s, Timestamp currentTime, Integer tenantId);
}
