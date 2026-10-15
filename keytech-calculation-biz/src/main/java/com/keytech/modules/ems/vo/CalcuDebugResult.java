package com.keytech.modules.ems.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 用于调试模式查询结果
 * @author yepeng
 * @since 2025.06.15
 */
@Schema(description = "调试模式查询结果")
@Data
public class CalcuDebugResult {
    @Schema(description = "是否开启调试模式")
    private boolean debug;
    @Schema(description = "调试模式采集点位")
    private List<String> cTagIds;
    @Schema(description = "调试模式中间点位")
    private List<String> oTagIds;
}
