package com.keytech.modules.maotai.mes.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 车检反馈通知(API10: 勾储MES → 平台, /api/VehicleCheck/Result)。
 * MES 回传车检结果(passFlag 1-通过 0-不通过)。只落通知,不联动 vehicle_check。
 *
 * @author maotai
 * @since 2026-07-31
 */
@Data
@TableName("mes3_maotai_vehicle_check_feedback")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "车检反馈通知(API10)")
public class VehicleCheckFeedback extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "请求流水Id")
    private String rqi;

    @Schema(description = "车检流水号")
    private String checkNo;

    @Schema(description = "车牌号")
    private String carNo;

    @Schema(description = "是否通过 1-通过 0-不通过")
    private Integer passFlag;

    @Schema(description = "不通过原因")
    private String passReason;

    @Schema(description = "允许装卸量 kg")
    private BigDecimal allowWeight;

    @Schema(description = "平台处理结果")
    private String processMsg;
}
