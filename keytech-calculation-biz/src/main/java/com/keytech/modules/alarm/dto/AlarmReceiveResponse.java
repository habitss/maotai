package com.keytech.modules.alarm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 报警接入接口返回结果
 * @author yepeng
 * @since 2026.01.20
 */
@Data
@AllArgsConstructor
@Schema(description = "报警接入接口返回结果")
public class AlarmReceiveResponse {
    @Schema(description = "业务处理结果编码")
    private String code;
    @Schema(description = "是否处理成功")
    private boolean success;
    @Schema(description = "结果描述")
    private String message;
    @Schema(description = "请求唯一标识")
    private String requestId;
    @Schema(description = "服务器接收时间")
    private LocalDateTime serverTime;
}
