package com.keytech.modules.i18n.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 语言列 DTO
 * @author yepeng
 * @since 2025.12.18
 */
@Schema(description = "语言列 DTO")
@Data
public class I18nLangDto {
    @Schema(description = "语言编码")
    private String langCode;
    @Schema(description = "语言名称")
    private String langName;
}