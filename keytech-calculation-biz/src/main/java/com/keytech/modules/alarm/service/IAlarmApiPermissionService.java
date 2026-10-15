package com.keytech.modules.alarm.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.alarm.entity.AlarmApiPermission;

import java.util.List;

/**
 * 报警接口权限服务
 * @author yepeng
 * @since 2026.01.29
 */
public interface IAlarmApiPermissionService extends BaseService<AlarmApiPermission> {

    /**
     * 根据appId查询权限列表
     * @param appId 应用标识
     * @return 权限列表
     */
    List<AlarmApiPermission> getPermissionsByAppId(String appId);

    /**
     * 验证应用是否有权限访问指定接口
     * @param appId 应用标识
     * @param apiCode 接口编码
     * @return 是否有权限
     */
    boolean hasPermission(String appId, String apiCode);
}
