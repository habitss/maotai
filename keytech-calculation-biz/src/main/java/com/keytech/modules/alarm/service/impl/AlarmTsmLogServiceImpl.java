package com.keytech.modules.alarm.service.impl;

import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.alarm.entity.AlarmTsmLog;
import com.keytech.modules.alarm.mapper.AlarmTsmLogMapper;
import com.keytech.modules.alarm.service.IAlarmTsmLogService;
import org.springframework.stereotype.Service;

/**
 * TSM接口请求日志Service实现类
 * @author yepeng
 * @since 2026.01.30
 */
@Service
public class AlarmTsmLogServiceImpl extends BaseServiceImpl<AlarmTsmLogMapper, AlarmTsmLog> implements IAlarmTsmLogService {

}
