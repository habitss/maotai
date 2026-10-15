package com.keytech.modules.alarm.service.impl;

import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.alarm.entity.AlarmPoint;
import com.keytech.modules.alarm.mapper.AlarmPointMapper;
import com.keytech.modules.alarm.service.IAlarmPointService;
import org.springframework.stereotype.Service;

/**
 * 报警点位
 * @author liu
 * @since 2026.01.12
 * */
@Service
public class AlarmPointServiceImpl extends BaseServiceImpl<AlarmPointMapper, AlarmPoint> implements IAlarmPointService {

}
