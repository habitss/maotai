package com.keytech.modules.maotai.workorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * 工单统计(状态 tab 计数 + 车间 tab 计数)
 *
 * @author workorder
 * @since 2026-09-15
 */
@Data
@Accessors(chain = true)
@Schema(description = "工单统计")
public class WorkOrderStatisticsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "各状态计数 key=pending/running/paused/completed/abandoned")
    private Map<String, Long> statusCount;

    @Schema(description = "各车间工单总数 key=车间码 ZZG/XLG/CPG/GWG")
    private Map<String, Long> workshopCount;
}
