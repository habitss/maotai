package com.keytech.modules.maotai.eh.vehiclecheck.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 车检信息(API08: E+H → 平台)。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Data
@TableName("mes3_maotai_vehicle_check")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "车检信息")
public class VehicleCheck extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "业务类型 1-销售(装车) 2-采购(卸车)")
    private Integer busiType;

    @Schema(description = "订单号/派车单号")
    private String orderNo;

    @Schema(description = "修改人")
    private String modifyor;

    @Schema(description = "修改类型")
    private Integer modifyType;

    @Schema(description = "修改内容(车检结果) 1-通过 0-不通过")
    private Integer modifyValue;

    @Schema(description = "标志位 默认1-直接走API11;0-只落库不触发")
    private Integer flag;

    @Schema(description = "车检流水号")
    private String checkNo;

    @Schema(description = "车牌号")
    private String carNo;

    @Schema(description = "IC 卡号")
    private String icCardNo;

    @Schema(description = "车检类型 1-入厂 2-出厂")
    private Integer checkType;

    @Schema(description = "车检时间 yyyy-MM-dd HH:mm:ss")
    private String checkTime;
}
