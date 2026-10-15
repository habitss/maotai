package com.keytech.modules.maotai.eh.orderupdate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 工单更新通知请求(API12: E+H → 平台 /api/Bussiness/UpdateNotify,鹤位/车位更新)。
 * 字段对齐参数字典 V0.3:OrderNo / busiType / CarNo / notifyTime / batchNo(鹤位) / seatNo(车位)。
 * 鹤位、车位为两个独立字段,哪个非空就更新哪个(不再用 UpdateType 判别)。
 * busiType/carNo/batchNo/seatNo 按文档非必填,不加 @NotNull,只限长度。校验注解带中文 message。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Data
@Schema(description = "工单更新通知请求(API12 鹤位/车位)")
public class OrderUpdateNotifyDTO {

    @Schema(description = "订单号", example = "20240123_001")
    @NotBlank(message = "订单号(OrderNo)不能为空")
    @Size(max = 50, message = "订单号(OrderNo)长度不能超过50")
    @JsonProperty("OrderNo")
    private String orderNo;

    @Schema(description = "业务类型 1-装车 2-卸车", example = "1")
    @JsonProperty("busiType")
    private Integer busiType;

    @Schema(description = "车牌号")
    @Size(max = 50, message = "车牌号(CarNo)长度不能超过50")
    @JsonProperty("CarNo")
    private String carNo;

    @Schema(description = "推送时间 yyyy-MM-dd HH:mm:ss", example = "2026-07-21 10:00:00")
    @NotBlank(message = "推送时间(notifyTime)不能为空")
    @Size(max = 50, message = "推送时间(notifyTime)长度不能超过50")
    @JsonProperty("notifyTime")
    private String notifyTime;

    @Schema(description = "鹤位")
    @Size(max = 50, message = "鹤位(batchNo)长度不能超过50")
    @JsonProperty("batchNo")
    private String batchNo;

    @Schema(description = "车位")
    @Size(max = 50, message = "车位(seatNo)长度不能超过50")
    @JsonProperty("seatNo")
    private String seatNo;
}
