package com.keytech.modules.maotai.scada.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 工单结束通知(loadingOrder: 中集自控系统 → 平台, 罐转罐工单结束)。
 *
 * @author maotai
 * @since 2026-07-27
 */
@Data
@TableName("mes3_maotai_work_order_finish")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "工单结束通知(API06)")
public class WorkOrderFinish extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "请求流水Id")
    private String rqi;

    @Schema(description = "工单号(罐转罐工单)")
    private String workOrderNo;

    @Schema(description = "任务类型 1-5 罐间作业")
    private Integer taskType;

    @Schema(description = "来源罐号")
    private String sourceTank;

    @Schema(description = "目标罐号")
    private String targetTank;

    @Schema(description = "目标量 kg")
    private BigDecimal targetWeight;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "同步时间")
    private String syncTime;

    @Schema(description = "目标品牌")
    private String targetProduct;

    @Schema(description = "输送量")
    private String transferVol;

    @Schema(description = "是否打空源罐 1-是 0-否")
    private Integer emptySource;

    @Schema(description = "平台处理结果")
    private String processMsg;
}
