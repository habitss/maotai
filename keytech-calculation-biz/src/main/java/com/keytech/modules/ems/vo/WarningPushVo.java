package com.keytech.modules.ems.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 采集计算预警消息推送VO
 * @author yepeng
 * @since 2025.05.23
 */
@Data
@Schema(description = "采集计算预警消息推送VO")
public class WarningPushVo {
    @Schema(description = "时间点")
    private LocalDateTime timePoint;
    @Schema(description = "消息内容")
    private String content;
    @Schema(description = "日志ID列表")
    private List<Integer> logIdList;
}
