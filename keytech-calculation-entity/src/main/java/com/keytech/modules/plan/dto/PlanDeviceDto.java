package com.keytech.modules.plan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: TODO
 * @Author: sjw
 * @Date: 2025/6/18 11:15
 */
@Data
public class PlanDeviceDto {
    //计划id
    private String planId;
    //设备id
    private String deviceIds;
    //计划类型
    private String planType;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "实际结束时间")
    private Date realEndTime;
}
