package com.keytech.modules.project.management.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: TODO
 * @Author: sjw
 * @Date: 2025/4/14 17:29
 */
@Data
public class ProjectManagementActionSummary {
    @Schema(description = "项目id")
    private String projectId;
    @Schema(description = "节点总数")
    private String nodeTotal = "0";
    @Schema(description = "节点完成数")
    private String nodeFinishTotal = "0";
    @Schema(description = "节点延迟数")
    private String nodeDelayTotal = "0";
    @Schema(description = "节点完成率")
    private String nodeFinishRate = "0";
    @Schema(description = "节点延迟率")
    private String nodeDelayRate = "0";
    @Schema(description = "行动完成数")
    private String actionFinishTotal = "0";
    @Schema(description = "行动延迟数")
    private String actionDelayTotal = "0";
    @Schema(description = "行动总数")
    private String actionTotal = "0";
    @Schema(description = "行动完成率")
    private String actionFinishRate = "0";
    @Schema(description = "行动延迟率")
    private String actionDelayRate = "0";


    @Schema(description = "月份")
    private String month;
    @Schema(description = "月总数")
    private Integer monthTotal = 0;
    @Schema(description = "周")
    private String weekNumber;
    @Schema(description = "周总数")
    private Integer weekTotal = 0;
}
