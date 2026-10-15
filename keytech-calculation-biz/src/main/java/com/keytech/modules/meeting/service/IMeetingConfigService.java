package com.keytech.modules.meeting.service;

import com.keytech.modules.meeting.entity.MeetingConfig;
import com.keytech.base.service.BaseService;
import org.jeecg.modules.system.entity.SysDepart;

/**
 * @Description: 有效会议类型配置
 * @Author: jeecg-boot
 * @Date:   2025-03-17
 * @Version: V1.0
 */
public interface IMeetingConfigService extends BaseService<MeetingConfig> {

    MeetingConfig queryByMeetingType(String meetingType);

    void setMeetingConfigRecursively(SysDepart sysDepart, int orgType, MeetingConfig meetingConfig);
}
