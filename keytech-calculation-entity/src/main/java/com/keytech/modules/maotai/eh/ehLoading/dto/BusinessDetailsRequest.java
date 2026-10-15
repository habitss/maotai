package com.keytech.modules.maotai.eh.ehLoading.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 查询业务命令请求(API16: 平台 → E+H, /api/Bussiness/Details, POST+Token)。
 * 查 E+H 系统内未结束(未归档)的装卸车单据;备用/兜底接口(分钟级)。
 * 字段对应 PDF V0.5 第 4.4 节,JSON 字段名保留原大小写。
 *
 * @author maotai
 * @since 2026-07-24
 */
@Data
@Schema(description = "查询业务命令请求(API16)")
public class BusinessDetailsRequest {

    @Schema(description = "业务类型 1-销售(出库/装车) 2-采购(入库/卸车)(默认)", example = "2")
    @JsonProperty("busiType")
    private Integer busiType;

    @Schema(description = "订单号(唯一)", example = "MT-DEMO-001")
    @NotBlank
    @Size(max = 50)
    @JsonProperty("OrderNo")
    private String orderNo;

    @Schema(description = "车牌号(订单对应)", example = "沪A12345")
    @NotBlank
    @Size(max = 50)
    @JsonProperty("CarNo")
    private String carNo;
}
