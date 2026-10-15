package com.keytech.modules.i18n.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * 单行词条 DTO
 * @author yepeng
 * @since 2025.12.18
 */
@Schema(description = "单行词条 DTO")
@Data
public class I18nEntryRowDto {
    @Schema(description = "租户ID")
    private Integer tenetId;
    @Schema(description = "词条ID")
    private Integer entryId;
    @Schema(description = "词条Key")
    private String entryKey;
    @Schema(description = "模块")
    private String module;
    @Schema(description = "描述")
    private String description;

    /**
     * 翻译 Map
     * key = langCode
     * value = 翻译值
     * 例：{"zh-CN":"登录","en-US":"Login"}
     */
    private Map<String, String> langMap = new HashMap<>();
}