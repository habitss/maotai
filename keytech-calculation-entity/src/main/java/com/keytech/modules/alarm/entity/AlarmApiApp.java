package com.keytech.modules.alarm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报警应用表
 * @author yepeng
 * @since 2026.01.29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("alarm_api_app")
public class AlarmApiApp implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;
    /** 应用唯一标识 */
    @Excel(name = "应用唯一标识", width = 15)
    @Schema(description = "应用唯一标识")
    private String appId;
    /** 应用名称 */
    @Excel(name = "应用名称", width = 15)
    @Schema(description = "应用名称")
    private String appName;
    /** 应用密钥 */
    @Excel(name = "应用密钥", width = 15)
    @Schema(description = "应用密钥")
    private String appSecret;
    /** 旧密钥（轮换期使用） */
    @Excel(name = "旧密钥", width = 15)
    @Schema(description = "旧密钥（轮换期使用）")
    private String appSecretOld;
    /** 密钥轮换时间 */
    @Excel(name = "密钥轮换时间", width = 15)
    @Schema(description = "密钥轮换时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime secretRotateTime;
    /** 状态 1:启用 0:禁用 */
    @Excel(name = "状态", width = 15, dictTable = "mes3_mdm_dict where group_code = 'EMS_StateYN'", dicText = "item_key", dicCode = "item_val")
    @Dict(dictTable = "mes3_mdm_dict where group_code = 'EMS_StateYN'", dicText = "item_key", dicCode = "item_val")
    @Schema(description = "状态 1:启用 0:禁用")
    private Integer status;
    /** 过期时间 */
    @Excel(name = "过期时间", width = 15)
    @Schema(description = "过期时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
    /** IP白名单 */
    @Excel(name = "IP白名单", width = 15)
    @Schema(description = "IP白名单")
    private String ipWhitelist;
    /** 创建时间 */
    @Excel(name = "创建时间", width = 15)
    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 更新时间 */
    @Excel(name = "更新时间", width = 15)
    @Schema(description = "更新时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    /** 租户ID */
    @Schema(description = "租户ID")
    private Integer tenantId;
}
