package com.keytech.modules.meeting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.meeting.entity.MeetingConfig;
import com.keytech.modules.meeting.mapper.MeetingConfigMapper;
import com.keytech.modules.meeting.service.IMeetingConfigService;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

/**
 * @Description: 有效会议类型配置
 * @Author: jeecg-boot
 * @Date:   2025-03-17
 * @Version: V1.0
 */
@Service
public class MeetingConfigServiceImpl extends BaseServiceImpl<MeetingConfigMapper, MeetingConfig> implements IMeetingConfigService {

    @Autowired
    private ISysDepartService sysDepartService;

    @Override
    public MeetingConfig queryByMeetingType(String meetingType) {
        return this.getOne(new LambdaQueryWrapper<MeetingConfig>().eq(StringUtils.isNotBlank(meetingType), MeetingConfig::getMeetingType, meetingType));
    }

    @Override
    public void setMeetingConfigRecursively(SysDepart sysDepart, int orgType, MeetingConfig meetingConfig) {
        String factoryCode = "";
        String deptCode = "";
        String workshop = "";
        String lineCode = "";
        if (orgType <= 3) {
            factoryCode = sysDepart.getOrgCode();
        }else if (orgType == 4) {
            deptCode = sysDepart.getOrgCode();
        } else if (orgType == 5) {
            workshop = sysDepart.getOrgCode();
        } else if (orgType == 6) {
            lineCode = sysDepart.getOrgCode();
        }
        meetingConfig.setFactoryCode(factoryCode);
        meetingConfig.setDeptCode(deptCode);
        meetingConfig.setWorkshop(workshop);
        meetingConfig.setLineCode(lineCode);

    }
}
