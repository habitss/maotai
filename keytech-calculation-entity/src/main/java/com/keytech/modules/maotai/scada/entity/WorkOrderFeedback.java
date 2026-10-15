package com.keytech.modules.maotai.scada.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 工单信息回传通知(API17: 中集自控系统 → 平台, /openapi/v1/api/WorkOrder/InfoFeedback)。
 * scadaStatus:中集运行状态 0-空闲 1-运行 7-暂停 10-异常终止 11-正常结束(2026-08-26 字段改名+换字典,
 * 原 status 1/3/5/8 作废);infoData 为回传信息JSON(原样存)。
 *
 * @author maotai
 * @since 2026-07-29
 */
@Data
@TableName("mes3_maotai_work_order_feedback")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "工单信息回传通知(API17)")
public class WorkOrderFeedback extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "请求流水Id")
    private String rqi;

    @Schema(description = "工单号(装卸车,报文 OrderNo;08-26 库列由 order_id 统一改名 order_no)")
    private String orderNo;

    @Schema(description = "中集运行状态:0-空闲 1-运行 7-暂停 10-异常终止 11-正常结束")
    private Integer scadaStatus;

    @Schema(description = "回传信息JSON(原样存)")
    private String infoData;

    @Schema(description = "回传时间")
    private String feedbackTime;

    @Schema(description = "平台处理结果")
    private String processMsg;
}
