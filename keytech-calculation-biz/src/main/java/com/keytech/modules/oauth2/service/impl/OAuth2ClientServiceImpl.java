package com.keytech.modules.oauth2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.oauth2.entity.OAuth2Client;
import com.keytech.modules.oauth2.mapper.OAuth2ClientMapper;
import com.keytech.modules.oauth2.service.IOAuth2ClientService;
import com.keytech.modules.oauth2.util.OAuth2KeyUtil;
import com.keytech.modules.oauth2.util.OAuth2SecretUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jeecg.common.exception.JeecgBoot401Exception;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 第三方OAuth2应用服务实现
 * @author claude
 * @since 2026.08.19
 */
@Service
public class OAuth2ClientServiceImpl extends BaseServiceImpl<OAuth2ClientMapper, OAuth2Client> implements IOAuth2ClientService {

    // 密钥轮换期（天）
    private static final int SECRET_ROTATE_DAYS = 7;

    @Override
    public OAuth2Client getByClientId(String clientId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<OAuth2Client>().eq(OAuth2Client::getClientId, clientId));
    }

    @Override
    public boolean isValidClient(String clientId) {
        return isValidClient(getByClientId(clientId));
    }

    @Override
    public boolean isValidClient(OAuth2Client client) {
        if (client == null || client.getStatus() == null || client.getStatus() != 1) {
            return false;
        }
        return client.getExpireTime() == null || client.getExpireTime().after(new Date());
    }

    @Override
    public boolean verifySecret(String clientId, String rawSecret) {
        return verifySecret(getByClientId(clientId), rawSecret);
    }

    @Override
    public boolean verifySecret(OAuth2Client client, String rawSecret) {
        if (client == null || StringUtils.isEmpty(rawSecret)) {
            return false;
        }
        String hash = OAuth2SecretUtil.hashSecret(rawSecret);
        if (hash.equals(client.getClientSecretHash())) {
            return true;
        }
        // 密钥轮换期内，旧密钥仍然有效
        return client.getClientSecretOldHash() != null && hash.equals(client.getClientSecretOldHash());
    }

    @Override
    public Set<String> getScopes(String clientId) {
        return getScopes(getByClientId(clientId));
    }

    @Override
    public Set<String> getScopes(OAuth2Client client) {
        if (client == null || StringUtils.isEmpty(client.getScopes())) {
            return Collections.emptySet();
        }
        return Arrays.stream(client.getScopes().split(","))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toSet());
    }

    @Override
    public Map<String, String> createClientWithSecret(OAuth2Client client) {
        if (StringUtils.isEmpty(client.getClientName())) {
            throw new IllegalArgumentException("应用名称不能为空");
        }
        if (client.getTenantId() == null) {
            // 正常情况下由 Controller 从 X-Tenant-Id 请求头回填；这里仅作为兜底校验，
            // 因为 tenant_id 是 NOT NULL 列，不能带 null 落库
            throw new IllegalArgumentException("租户ID不能为空");
        }
        if (StringUtils.isEmpty(client.getClientId())) {
            client.setClientId(OAuth2KeyUtil.generateClientId("oc"));
        } else if (getByClientId(client.getClientId()) != null) {
            // 手动指定的client_id若与已有应用重复，在这里给出清晰提示，
            // 避免直接撞到 uk_oauth2_client_id 唯一索引抛出原始SQL异常
            throw new IllegalArgumentException("应用标识(client_id)已存在: " + client.getClientId());
        }
        // 第三方应用凭证不归属具体工厂/部门/产线，按表结构约定填充空字符串
        if (client.getFactoryCode() == null) {
            client.setFactoryCode("");
        }
        if (client.getDeptCode() == null) {
            client.setDeptCode("");
        }
        if (client.getLineCode() == null) {
            client.setLineCode("");
        }
        String rawSecret = OAuth2KeyUtil.generateClientSecret();
        client.setClientSecretHash(OAuth2SecretUtil.hashSecret(rawSecret));
        if (client.getStatus() == null) {
            client.setStatus(1);
        }
        client.setCreateTime(new Date());
        client.setUpdateTime(new Date());
        baseMapper.insert(client);

        Map<String, String> result = new HashMap<>();
        result.put("clientId", client.getClientId());
        result.put("clientSecret", rawSecret); // 仅此一次可见，请妥善保存
        return result;
    }

    @Override
    public String resetClientSecret(String clientId) {
        OAuth2Client client = getByClientId(clientId);
        if (client == null) {
            throw new JeecgBoot401Exception("应用不存在");
        }
        String newSecret = OAuth2KeyUtil.generateClientSecret();
        client.setClientSecretOldHash(client.getClientSecretHash());
        client.setClientSecretHash(OAuth2SecretUtil.hashSecret(newSecret));
        client.setSecretRotateTime(new Date());
        client.setUpdateTime(new Date());
        baseMapper.updateById(client);
        return newSecret;
    }

    @Override
    public boolean checkAndCleanExpiredOldSecret(OAuth2Client client) {
        if (client == null || client.getClientSecretOldHash() == null || client.getSecretRotateTime() == null) {
            return false;
        }
        Date rotateEndTime = DateUtils.addDays(client.getSecretRotateTime(), SECRET_ROTATE_DAYS);
        if (new Date().after(rotateEndTime)) {
            client.setClientSecretOldHash(null);
            client.setSecretRotateTime(null);
            baseMapper.updateById(client);
            return true;
        }
        return false;
    }
}
