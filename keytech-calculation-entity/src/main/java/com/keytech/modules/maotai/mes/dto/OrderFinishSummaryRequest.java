package com.keytech.modules.maotai.mes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 工单结束汇总请求(API18: 平台 → 勾储MES, /GZMES/notify/orderFinish, POST)。
 * 由平台从 loading_order 组装后发出(非用户直接输入)。字段对应参数字典表 V0.3 API18。
 * 字段大小写按 MES 规格:ABV/Modifyor 大写,其余 camelCase。
 *
 * @author maotai
 * @since 2026-07-31
 */
@Data
@Schema(description = "工单结束汇总请求(API18 平台→勾储MES)")
public class OrderFinishSummaryRequest {

    @Schema(description = "请求流水Id")
    @JsonProperty("rqi")
    private String rqi;

    @Schema(description = "工单号(与下发一致)")
    @JsonProperty("orderId")
    private String orderId;

    @Schema(description = "实际装卸量 kg(平台汇总)")
    @JsonProperty("actualWeight")
    private BigDecimal actualWeight;

    @Schema(description = "装卸开始时间")
    @JsonProperty("loadStartTime")
    private String loadStartTime;

    @Schema(description = "装卸结束时间")
    @JsonProperty("loadEndTime")
    private String loadEndTime;

    @Schema(description = "酒精度数(取样后)")
    @JsonProperty("ABV")
    private String abv;

    @Schema(description = "业务状态 8-业务结束(通知MES)")
    @JsonProperty("status")
    private String status;

    @Schema(description = "汇总信息(平台汇总的工单执行摘要)")
    @JsonProperty("summaryInfo")
    private String summaryInfo;

    @Schema(description = "操作人")
    @JsonProperty("Modifyor")
    private String modifyor;
}
