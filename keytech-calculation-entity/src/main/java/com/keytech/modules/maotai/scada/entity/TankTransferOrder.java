package com.keytech.modules.maotai.scada.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 罐转罐工单主表(API05 启动建单/API06 结束关单,2026-08-26 建)。
 * <p>API05 推送成功 INSERT 一条"进行中"(uk_work_order_no 幂等);API06 结束通知到达时 UPDATE 关单。
 * status:9-罐转罐启动 10-罐转罐结束(对齐 loading_order.order_status 字典 9/10)。
 *
 * @author maotai
 * @since 2026-08-26
 */
@Data
@TableName("mes3_maotai_tank_transfer_order")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "罐转罐工单(API05/06)")
public class TankTransferOrder extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "工单号(罐转罐工单,API05/06 报文 workOrderNo)")
    private String workOrderNo;

    @Schema(description = "状态:9-罐转罐启动(API05) 10-罐转罐结束(API06)")
    private Integer orderStatus;

    @Schema(description = "启动请求流水Id(API05 rqi)")
    private String startRqi;

    @Schema(description = "结束请求流水Id(API06 rqi)")
    private String finishRqi;

    @Schema(description = "当前车载酒数量(API05 currentVehicleQty)")
    private String currentVehicleQty;

    @Schema(description = "目标罐(API05 targetTankCode)")
    private String targetTankCode;

    @Schema(description = "传输量设定值(API05 transferSetpoint)")
    private String transferSetpoint;

    @Schema(description = "酒精度(API05 alcoholContent)")
    private String alcoholContent;

    @Schema(description = "装酒路径(API05 loadingPath)")
    private String loadingPath;

    @Schema(description = "源罐/卸车撬(API05 sourceTankCode)")
    private String sourceTankCode;

    @Schema(description = "操作人员名称(API05 operatorName)")
    private String operatorName;

    @Schema(description = "启动时间(平台触发 API05 成功时间)")
    private String startTime;

    @Schema(description = "结束时间(API06 报文 syncTime)")
    private String finishTime;
}
