package com.keytech.modules.i18n.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 分页返回结构
 * @author yepeng
 * @since 2025.12.18
 */
@Schema(description = "分页返回结构")
@Data
public class I18nEntryPageResult {
    /**
     * 动态语言列，如：
     * [{"langCode":"zh-CN","langName":"中文"},{"langCode":"en-US","langName":"English"}]
     */
    @Schema(description = "动态语言列")
    private List<I18nLangDto> columns;

    /**
     * 分页数据（真正一行一词条）
     */
    @Schema(description = "分页数据")
    private IPage<I18nEntryRowDto> page;
}