package com.keytech.modules.alarm.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.alarm.entity.AlarmApiApp;
import java.util.Map;

/**
 * 报警应用服务
 * @author yepeng
 * @since 2026.01.29
 */
public interface IAlarmApiAppService extends BaseService<AlarmApiApp> {

    /**
     * 根据appId查询应用
     * @param appId 应用标识
     * @return 应用信息
     */
    AlarmApiApp getByAppId(String appId);

    /**
     * 验证应用是否有效
     * @param appId 应用标识
     * @return 是否有效
     */
    boolean isValidApp(String appId);

    /**
     * 重置应用密钥
     * @param appId 应用标识
     * @return 新的应用密钥
     */
    String resetAppSecret(String appId);

    /**
     * 清理过期的旧密钥
     * @param appId 应用标识
     * @return 是否清理成功
     */
    boolean cleanExpiredOldSecret(String appId);

    /**
     * 检查并清理过期的旧密钥
     * @param app 应用信息
     * @return 是否清理成功
     */
    boolean checkAndCleanExpiredOldSecret(AlarmApiApp app);

    /**
     * 创建应用并生成密钥
     * @param app 应用信息
     * @return 包含appId和appSecret的Map
     */
    Map<String, String> createAppWithSecret(AlarmApiApp app);
}
