package com.keytech.modules.maotai.hikvision.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 取流 URL 视图对象(实时预览/录像回放共用)。
 * 对应海康 previewURLs/playbackURLs 接口 data 里的 url;URL 有效期 5 分钟,过期需重取。
 *
 * @author maotai
 * @since 2026-08-10
 */
@Data
@Schema(description = "取流 URL")
public class PreviewUrlVO {

    @Schema(description = "取流 URL(protocol=ws 时配合海康 H5 播放器无插件播放;有效期 5 分钟)")
    private String url;
}
