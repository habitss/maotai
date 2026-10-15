package com.keytech.modules.maotai.scada.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 工单状态更新通知(API13: 中集自控系统 → 平台, /api/WorkOrder/StatusUpdate)。
 * status:1-新建 3-开始 5-结束 8-业务结束(参数字典表 V0.3 API13 / 枚举字典)。
 *
 * @author maotai
 * @since 2026-07-28
 */
@Data
@TableName("mes3_maotai_work_order_status")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "工单状态更新通知(API13)")
public class WorkOrderStatus extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "请求流水Id")
    private String rqi;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "工单状态 1-新建 3-开始 5-结束 8-业务结束")
    private String status;

    @Schema(description = "鹤位")
    private String batchNo;

    @Schema(description = "状态更新时间(来自请求 updateTime)")
    private String statusUpdateTime;

    @Schema(description = "平台处理结果")
    private String processMsg;
}
