package com.keytech.modules.maotai.mes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 工单结束汇总触发入参(API18 平台内部端点 /maotai/mes/orderFinish/push)。
 * 只传 orderNo + rqi,业务字段由平台从 loading_order 组装后发给勾储MES。
 *
 * @author maotai
 * @since 2026-07-31
 */
@Data
@Schema(description = "工单结束汇总触发入参(API18)")
public class OrderFinishSummaryCommand {

    @Schema(description = "订单号(装卸车工单)")
    @NotBlank
    private String orderNo;

    @Schema(description = "请求流水Id(响应回传同请求)")
    @NotBlank
    private String rqi;
}
