package com.keytech.modules.alarm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 报警数量统计VO
 * @author liu
 * @since 2025.01.16
 */
@Data
@Schema(description = "报警数量统计VO")
public class AlarmCountVo {
    @Schema(description = "项目")
    private String item;
    @Schema(description = "数量")
    private Integer cnt;
}
