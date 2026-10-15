package com.keytech.modules.pi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.modules.pi.entity.PiSistarRtStepLog;
import com.keytech.modules.pi.mapper.PiSistarRtStepLogMapper;
import com.keytech.modules.pi.service.IPiSistarRtStepLogService;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * @Description: 抽取记录表-最后更新时间
 * @Author: jeecg-boot
 * @Date:   2025-08-31
 * @Version: V1.0
 */
@Service
public class PiSistarRtStepLogServiceImpl extends BaseServiceImpl<PiSistarRtStepLogMapper, PiSistarRtStepLog> implements IPiSistarRtStepLogService {

    /**
     * 获取最后更新时间
     * @param type
     * @return
     */
    @Override
    public Timestamp getLastLoadTime(String type) {
        List<PiSistarRtStepLog> piSistarRtStepLogs = list(new QueryWrapper<PiSistarRtStepLog>().eq("type", type));
        if(piSistarRtStepLogs != null && !piSistarRtStepLogs.isEmpty()){
            Date date = piSistarRtStepLogs.get(0).getDateTime();
            if (date != null) {
                return new Timestamp(date.getTime());
            }
        }
        return null;
    }

    /**
     * 更新最后更新时间
     * @param type
     * @param currentTime
     * @param tenantId
     */
    @Override
    public void updateLastLoadTime(String type, Timestamp currentTime, Integer tenantId) {
        PiSistarRtStepLog piSistarRtStepLog = new PiSistarRtStepLog();
        piSistarRtStepLog.setType(type);
        piSistarRtStepLog.setTenantId(tenantId);
        piSistarRtStepLog.setDateTime(currentTime);
        update(piSistarRtStepLog, new QueryWrapper<PiSistarRtStepLog>().eq("type", type));
    }
}
