package com.keytech.modules.alarm.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.alarm.entity.AlarmEvent;
import com.keytech.modules.alarm.vo.AlarmCountVo;

import java.util.List;
import java.util.Map;

/**
 * 报警事实
 * @author liu
 * @since 2026.01.12
 * */
public interface IAlarmEventService extends BaseService<AlarmEvent> {

    /**
     * 报警中心大屏-同比环比
     */
    Map<String, Object> bigScreenComparison(String factoryCode);

    /**
     * 报警中心大屏-报警类型
     */
    List<AlarmCountVo> bigScreenAlarmType(String factoryCode);

    /**
     * 报警中心大屏-设备异常排名TOP5
     */
    List<AlarmCountVo> bigScreenDeviceTopFive(String factoryCode);

    /**
     * 报警中心大屏-报警分析
     */
    List<AlarmCountVo> bigScreenStatus(String factoryCode);

}
