package com.keytech.modules.maotai.hikvision.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 监控点分页列表视图对象。
 * 对应海康 /artemis/api/resource/v1/cameras 的 data 结构。
 *
 * @author maotai
 * @since 2026-08-10
 */
@Data
@Schema(description = "监控点分页列表")
public class CameraPageVO {

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页监控点列表")
    private List<CameraVO> list;

    @Schema(description = "当前页码")
    private Integer pageNo;

    @Schema(description = "每页大小")
    private Integer pageSize;
}
