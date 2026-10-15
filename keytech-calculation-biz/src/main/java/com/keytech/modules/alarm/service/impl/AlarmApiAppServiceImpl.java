package com.keytech.modules.alarm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.alarm.entity.AlarmApiApp;
import com.keytech.modules.alarm.mapper.AlarmApiAppMapper;
import com.keytech.modules.alarm.service.IAlarmApiAppService;
import com.keytech.modules.alarm.util.AppKeyGenerateUtil;
import org.jeecg.common.exception.JeecgBoot401Exception;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 报警应用服务实现
 * @author yepeng
 * @since 2026.01.29
 */
@Service
public class AlarmApiAppServiceImpl extends BaseServiceImpl<AlarmApiAppMapper, AlarmApiApp> implements IAlarmApiAppService {

    // 密钥轮换期（天）
    private static final int SECRET_ROTATE_DAYS = 7;

    /**
     * 根据appId查询应用
     * @param appId 应用标识
     * @return 应用信息
     */
    @Override
    public AlarmApiApp getByAppId(String appId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<AlarmApiApp>().eq(AlarmApiApp::getAppId, appId));
    }

    /**
     * 验证应用是否有效
     * @param appId 应用标识
     * @return 是否有效
     */
    @Override
    public boolean isValidApp(String appId) {
        AlarmApiApp app = getByAppId(appId);
        if (app == null) {
            return false;
        }
        // 检查状态是否启用
        if (app.getStatus() != 1) {
            return false;
        }
        // 检查是否过期
        if (app.getExpireTime() != null && app.getExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        return true;
    }

    /**
     * 重置应用密钥
     * @param appId 应用标识
     * @return 新的应用密钥
     */
    @Override
    public String resetAppSecret(String appId) {
        AlarmApiApp app = getByAppId(appId);
        if (app == null) {
            throw new JeecgBoot401Exception("应用不存在");
        }

        // 生成新密钥
        String newSecret = AppKeyGenerateUtil.generateAppSecret();

        // 轮换逻辑：将当前密钥保存为旧密钥，设置新密钥
        app.setAppSecretOld(app.getAppSecret());
        app.setAppSecret(newSecret);
        app.setSecretRotateTime(LocalDateTime.now());

        // 更新数据库
        baseMapper.updateById(app);

        // 只返回一次新密钥
        return newSecret;
    }

    /**
     * 清理过期的旧密钥
     * @param appId 应用标识
     * @return 是否清理成功
     */
    @Override
    public boolean cleanExpiredOldSecret(String appId) {
        AlarmApiApp app = getByAppId(appId);
        if (app == null) {
            return false;
        }

        // 检查是否处于轮换期
        if (app.getAppSecretOld() == null || app.getSecretRotateTime() == null) {
            return false;
        }

        // 检查轮换期是否结束
        LocalDateTime rotateEndTime = app.getSecretRotateTime().plusDays(SECRET_ROTATE_DAYS);
        if (LocalDateTime.now().isAfter(rotateEndTime)) {
            // 轮换期结束，清理旧密钥
            app.setAppSecretOld(null);
            app.setSecretRotateTime(null);
            baseMapper.updateById(app);
            return true;
        }

        return false;
    }

    /**
     * 检查并清理过期的旧密钥
     * @param app 应用信息
     * @return 是否清理成功
     */
    public boolean checkAndCleanExpiredOldSecret(AlarmApiApp app) {
        if (app == null || app.getAppSecretOld() == null || app.getSecretRotateTime() == null) {
            return false;
        }

        // 检查轮换期是否结束
        LocalDateTime rotateEndTime = app.getSecretRotateTime().plusDays(SECRET_ROTATE_DAYS);
        if (LocalDateTime.now().isAfter(rotateEndTime)) {
            // 轮换期结束，清理旧密钥
            app.setAppSecretOld(null);
            app.setSecretRotateTime(null);
            baseMapper.updateById(app);
            return true;
        }

        return false;
    }

    /**
     * 创建应用并生成密钥
     * @param app 应用信息
     * @return 包含appId和appSecret的Map
     */
    @Override
    public Map<String, String> createAppWithSecret(AlarmApiApp app) {
        // 验证appName是否为空
        if (app.getAppName() == null || app.getAppName().isEmpty()) {
            throw new IllegalArgumentException("应用名称不能为空");
        }

        // 生成appId（如果未提供）
        if (app.getAppId() == null || app.getAppId().isEmpty()) {
            app.setAppId(AppKeyGenerateUtil.generateAppId("app"));
        }

        // 生成appSecret
        String appSecret = AppKeyGenerateUtil.generateAppSecret();
        app.setAppSecret(appSecret);

        // 设置默认值
        if (app.getStatus() == null) {
            // 默认启用
            app.setStatus(1);
        }
        app.setCreateTime(LocalDateTime.now());
        app.setUpdateTime(LocalDateTime.now());

        // 保存到数据库
        baseMapper.insert(app);

        // 返回包含appId和appSecret的Map
        Map<String, String> result = new HashMap<>();
        result.put("appId", app.getAppId());
        result.put("appSecret", appSecret);

        return result;
    }
}
