package com.keytech.modules.maotai.eh.orderfeedback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 工单信息回传请求(API15: E+H → 平台 /api/Bussiness/Finish)。
 * 已按需求去掉 ABV(酒精度数)、flowMeterTagNumber(流量计位号)。
 * actualWeight/loadStartTime/loadEndTime/status 按文档非必填,只限长度。校验注解带中文 message。
 *
 * @author maotai
 * @since 2026-07-22
 */
@Data
@Schema(description = "工单信息回传请求(API15)")
public class OrderFeedbackDTO {

    @Schema(description = "订单号", example = "20240123_001")
    @NotBlank(message = "订单号(OrderNo)不能为空")
    @Size(max = 50, message = "订单号(OrderNo)长度不能超过50")
    @JsonProperty("OrderNo")
    private String orderNo;

    @Schema(description = "车牌号", example = "沪A12345")
    @NotBlank(message = "车牌号(CarNo)不能为空")
    @Size(max = 50, message = "车牌号(CarNo)长度不能超过50")
    @JsonProperty("CarNo")
    private String carNo;

    @Schema(description = "实际装卸量 kg", example = "33800")
    @JsonProperty("actualWeight")
    private BigDecimal actualWeight;

    @Schema(description = "装卸开始时间 yyyy-MM-dd HH:mm:ss")
    @Size(max = 50, message = "装卸开始时间(loadStartTime)长度不能超过50")
    @JsonProperty("loadStartTime")
    private String loadStartTime;

    @Schema(description = "装卸结束时间 yyyy-MM-dd HH:mm:ss")
    @Size(max = 50, message = "装卸结束时间(loadEndTime)长度不能超过50")
    @JsonProperty("loadEndTime")
    private String loadEndTime;

    @Schema(description = "业务状态")
    @Size(max = 50, message = "业务状态(status)长度不能超过50")
    @JsonProperty("status")
    private String status;

    @Schema(description = "回传时间 yyyy-MM-dd HH:mm:ss", example = "2026-07-22 16:00:00")
    @NotBlank(message = "回传时间(feedbackTime)不能为空")
    @Size(max = 50, message = "回传时间(feedbackTime)长度不能超过50")
    @JsonProperty("feedbackTime")
    private String feedbackTime;
}
