package com.keytech.modules.i18n.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 国际化词条多语言翻译VO
 * @author yepeng
 * @since 2025.12.21
 */
@Schema(description = "国际化词条多语言翻译VO")
@Data
public class I18nEntryLangVO {
    @Schema(description = "词条Key")
    private String entryKey;
    @Schema(description = "翻译内容")
    private String content;
}
