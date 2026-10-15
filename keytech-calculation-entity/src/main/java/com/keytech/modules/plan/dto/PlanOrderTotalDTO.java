package com.keytech.modules.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;

@Data
public class PlanOrderTotalDTO {

//    @Schema(description = "设备")
//    private String deviceName;

    @Schema(description = "设备")
    @Dict(dictTable = "mes3_mdm_device", dicText = "device_name", dicCode = "id")
    private String deviceId;

    /**任务id*/
    @Schema(description = "任务")
    private String taskName;

    @Schema(description = "任务数")
    private Integer taskCount;

    @Schema(description = "任务完成数")
    private Integer taskCompleteCount;

    @Schema(description = "任务未完成数")
    private Integer taskNoCompleteCount;

    @Schema(description = "任务完成率")
    private Integer taskCompleteRate;

    @Schema(description = "检查项数量")
    private Integer orderResultCount;

    @Schema(description = "合格数量")
    private Integer orderResultCompleteCount;

    @Schema(description = "不合格数量")
    private Integer orderResultNoCompleteCount;

    @Schema(description = "合格率")
    private Integer orderResultRate;
}
