package com.keytech.modules.maotai.eh.orderupdate.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 工单更新通知(API12: E+H → 平台,鹤位 batchNo / 车位 seatNo 更新)。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Data
@TableName("mes3_maotai_order_update_notify")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "工单更新通知")
public class OrderUpdateNotify extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "业务类型 1-装车 2-卸车")
    private Integer busiType;

    @Schema(description = "车牌号")
    private String carNo;

    @Schema(description = "推送时间")
    private String notifyTime;

    @Schema(description = "鹤位")
    private String batchNo;

    @Schema(description = "车位")
    private String seatNo;

    @Schema(description = "平台处理结果")
    private String processMsg;
}
