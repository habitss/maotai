package com.keytech.modules.alarm.service.impl;

import com.keytech.modules.alarm.dto.AlarmReceiveRequest;
import com.keytech.modules.alarm.mapper.AlarmEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 报警幂等性Service
 * @author yepeng
 * @since 2026.01.20
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmIdempotentService {
    private final AlarmEventMapper alarmEventMapper;

    /**
     * 判断报警是否重复
     * 根据报警来源系统、报警piCode、报警时间判断是否重复
     * 如果重复，则返回true，否则返回false
     */
    public boolean isDuplicate(AlarmReceiveRequest req) {
        Integer count = alarmEventMapper.countDuplicate(req.getSourceSystem(), req.getRequestId());
        return count != null && count > 0;
    }
}
