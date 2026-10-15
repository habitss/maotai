package com.keytech.modules.maotai.scada.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 装卸车工单同步请求(API14: 平台 → 中集自控系统, /api/WorkOrder/SyncLoad, POST)。
 * 由平台从 loading_order 组装后发出(非用户直接输入),字段对应参数字典表 V0.3 API14;
 * 2026-08-27 新增 TankNameBefore/TankName(照单全传,必填性由 API04 建档按 busiType 保证)。
 * 字段大小写按自控规格原样:rqi/busiType/syncTime 小写开头,其余 PascalCase。
 *
 * @author maotai
 * @since 2026-07-29
 */
@Data
@Schema(description = "装卸车工单同步请求(API14 平台→中集自控)")
public class WorkOrderSyncLoadRequest {

    @Schema(description = "请求流水Id")
    @JsonProperty("rqi")
    private String rqi;

    @Schema(description = "订单号/装卸车工单")
    @JsonProperty("OrderNo")
    private String orderNo;

    @Schema(description = "业务类型 1-装车 2-卸车")
    @JsonProperty("busiType")
    private Integer busiType;

    @Schema(description = "车牌号")
    @JsonProperty("CarNo")
    private String carNo;

    @Schema(description = "定量 kg")
    @JsonProperty("PreWeight")
    private BigDecimal preWeight;

    @Schema(description = "产品名称")
    @JsonProperty("ProductName")
    private String productName;

    @Schema(description = "来源罐号(装车工单传,卸车不传)")
    @JsonProperty("TankNameBefore")
    private String tankNameBefore;

    @Schema(description = "预目标罐号(卸车工单传,装车不传)")
    @JsonProperty("TankName")
    private String tankName;

    @Schema(description = "司机姓名")
    @JsonProperty("DriverName")
    private String driverName;

    @Schema(description = "IC卡号(UInt32)")
    @JsonProperty("Validate")
    private String validate;

    @Schema(description = "同步时间")
    @JsonProperty("syncTime")
    private String syncTime;
}
