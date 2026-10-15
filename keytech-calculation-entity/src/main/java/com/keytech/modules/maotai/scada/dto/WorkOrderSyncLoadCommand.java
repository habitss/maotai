package com.keytech.modules.maotai.scada.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 装卸车工单同步触发入参(API14 平台内部端点 /maotai/scada/workOrder/syncLoad)。
 * 只传 orderNo + rqi,业务字段由平台从 loading_order 组装后发给中集自控。
 *
 * @author maotai
 * @since 2026-07-29
 */
@Data
@Schema(description = "装卸车工单同步触发入参(API14)")
public class WorkOrderSyncLoadCommand {

    @Schema(description = "订单号(装卸车工单)")
    @NotBlank(message = "工单号(OrderNo)不能为空")
    @JsonProperty("OrderNo")
    private String orderNo;

    @Schema(description = "请求流水Id(响应回传同请求)")
    @NotBlank(message = "请求流水Id(rqi)不能为空")
    @JsonProperty("rqi")
    private String rqi;
}
