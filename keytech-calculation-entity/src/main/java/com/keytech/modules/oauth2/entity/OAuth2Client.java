package com.keytech.modules.oauth2.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 第三方OAuth2应用表
 * @author claude
 * @since 2026.08.19
 */
@Data
@TableName("mes3_oauth2_client")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "第三方OAuth2应用表")
public class OAuth2Client extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 应用标识(client_id) */
    @Excel(name = "应用标识", width = 20)
    @Schema(description = "应用标识(client_id)")
    private String clientId;

    /** 应用名称 */
    @Excel(name = "应用名称", width = 20)
    @Schema(description = "应用名称")
    private String clientName;

    /** 密钥哈希（服务端内部生成/校验，禁止通过任何JSON请求或响应读写） */
    @JsonIgnore
    @Schema(description = "密钥哈希(SHA-256)，不对外暴露", hidden = true)
    private String clientSecretHash;

    /** 旧密钥哈希（服务端内部生成/校验，禁止通过任何JSON请求或响应读写） */
    @JsonIgnore
    @Schema(description = "旧密钥哈希（轮换期使用）", hidden = true)
    private String clientSecretOldHash;

    /** 密钥轮换时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "密钥轮换时间")
    private Date secretRotateTime;

    /** 授权范围 */
    @Excel(name = "授权范围", width = 25)
    @Schema(description = "授权范围，逗号分隔，如 data:read,data:write")
    private String scopes;

    /** IP白名单 */
    @Excel(name = "IP白名单", width = 25)
    @Schema(description = "IP白名单，逗号分隔，为空表示不限制")
    private String ipWhitelist;

    /** 状态 1:启用 0:禁用 */
    @Excel(name = "状态", width = 15, dictTable = "mes3_mdm_dict where group_code = 'EMS_StateYN'", dicText = "item_key", dicCode = "item_val")
    @Dict(dictTable = "mes3_mdm_dict where group_code = 'EMS_StateYN'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "状态 1:启用 0:禁用")
    private Integer status;

    /** 过期时间 */
    @Excel(name = "过期时间", width = 20)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "过期时间，为空表示不过期")
    private Date expireTime;
}
