package com.keytech.modules.alarm.service.impl;

import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.alarm.entity.AlarmKafkaLog;
import com.keytech.modules.alarm.mapper.AlarmKafkaLogMapper;
import com.keytech.modules.alarm.service.IAlarmKafkaLogService;
import org.springframework.stereotype.Service;

/**
 * KAFKA报警消息
 * @author yepeng
 * @since 2026.01.20
 * */
@Service
public class AlarmKafkaLogServiceImpl extends BaseServiceImpl<AlarmKafkaLogMapper, AlarmKafkaLog> implements IAlarmKafkaLogService {

}
