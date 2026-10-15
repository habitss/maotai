package com.keytech.modules.alarm.service.impl;

import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.alarm.entity.AlarmIngestLog;
import com.keytech.modules.alarm.mapper.AlarmIngestLogMapper;
import com.keytech.modules.alarm.service.IAlarmIngestLogService;
import org.springframework.stereotype.Service;

/**
 * 报警接入日志
 * @author liu
 * @since 2026.01.12
 * */
@Service
public class AlarmIngestLogServiceImpl extends BaseServiceImpl<AlarmIngestLogMapper, AlarmIngestLog> implements IAlarmIngestLogService {

}
