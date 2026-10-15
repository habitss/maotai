package com.keytech.modules.oauth2.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.oauth2.entity.OAuth2Client;
import java.util.Map;
import java.util.Set;

/**
 * 第三方OAuth2应用服务
 * @author claude
 * @since 2026.08.19
 */
public interface IOAuth2ClientService extends BaseService<OAuth2Client> {

    /** 根据clientId查询应用 */
    OAuth2Client getByClientId(String clientId);

    /** 应用是否启用且未过期 */
    boolean isValidClient(String clientId);

    /** 应用是否启用且未过期（基于已查到的实体，避免同一请求内重复查库） */
    boolean isValidClient(OAuth2Client client);

    /** 校验client_secret是否正确（含轮换期旧密钥） */
    boolean verifySecret(String clientId, String rawSecret);

    /** 校验client_secret是否正确（基于已查到的实体，避免同一请求内重复查库） */
    boolean verifySecret(OAuth2Client client, String rawSecret);

    /** 获取应用被授权的scope集合 */
    Set<String> getScopes(String clientId);

    /** 获取应用被授权的scope集合（基于已查到的实体，避免同一请求内重复查库） */
    Set<String> getScopes(OAuth2Client client);

    /** 创建应用并生成密钥，返回值仅包含一次性可见的clientSecret */
    Map<String, String> createClientWithSecret(OAuth2Client client);

    /** 重置密钥，旧密钥进入轮换期 */
    String resetClientSecret(String clientId);

    /** 轮换期结束后清理旧密钥 */
    boolean checkAndCleanExpiredOldSecret(OAuth2Client client);
}
