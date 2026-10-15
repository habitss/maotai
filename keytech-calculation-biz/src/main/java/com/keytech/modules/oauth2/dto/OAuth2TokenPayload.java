package com.keytech.modules.oauth2.dto;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 存入Redis的access_token载荷
 * @author claude
 * @since 2026.08.19
 */
@Data
public class OAuth2TokenPayload implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String clientId;
    private Set<String> scopes;
    private Integer tenantId;
}
