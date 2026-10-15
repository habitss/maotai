package com.keytech.modules.alarm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.alarm.entity.AlarmApiPermission;
import com.keytech.modules.alarm.mapper.AlarmApiPermissionMapper;
import com.keytech.modules.alarm.service.IAlarmApiPermissionService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 报警接口权限服务实现
 * @author yepeng
 * @since 2026.01.29
 */
@Service
public class AlarmApiPermissionServiceImpl extends BaseServiceImpl<AlarmApiPermissionMapper, AlarmApiPermission> implements IAlarmApiPermissionService {

    @Override
    public List<AlarmApiPermission> getPermissionsByAppId(String appId) {
        LambdaQueryWrapper<AlarmApiPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlarmApiPermission::getAppId, appId);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public boolean hasPermission(String appId, String apiCode) {
        LambdaQueryWrapper<AlarmApiPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlarmApiPermission::getAppId, appId).eq(AlarmApiPermission::getApiCode, apiCode);
        AlarmApiPermission permission = baseMapper.selectOne(queryWrapper);
        return permission != null;
    }
}
