package com.keytech.modules.maotai.workorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 转酒路径候选
 *
 * @author workorder
 * @since 2026-09-15
 */
@Data
@Accessors(chain = true)
@Schema(description = "转酒路径候选")
public class TransferPathVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "路径编码,如 Z100FX01")
    private String code;

    @Schema(description = "路径名称(名称+编码),如 1#卸车线（Z100FX01）")
    private String name;

    @Schema(description = "线名(短标签),如 1#卸车线")
    private String lineName;

    @Schema(description = "卸车撬/鹤位编码(卸/装车工艺路径携带)")
    private String skidCode;
}
