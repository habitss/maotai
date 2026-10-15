package com.keytech.modules.maotai.hikvision.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 录像回放取流入参。
 * 时间格式海康要求 ISO8601(含时区),如 2018-05-21T12:00:00+08:00。
 *
 * @author maotai
 * @since 2026-08-10
 */
@Data
@Schema(description = "录像回放取流入参")
public class PlaybackCommand {

    @Schema(description = "监控点唯一标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "监控点编号不能为空")
    private String cameraIndexCode;

    @Schema(description = "回放开始时间(ISO8601 含时区,如 2018-05-21T12:00:00+08:00)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "开始时间不能为空")
    private String beginTime;

    @Schema(description = "回放结束时间(ISO8601 含时区,如 2018-05-21T13:00:00+08:00)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "结束时间不能为空")
    private String endTime;
}
