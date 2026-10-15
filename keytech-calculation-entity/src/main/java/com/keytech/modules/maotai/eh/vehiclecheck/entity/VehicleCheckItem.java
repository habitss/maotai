package com.keytech.modules.maotai.eh.vehiclecheck.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 车检项明细(一对多,关联 mes3_maotai_vehicle_check.check_no)。
 *
 * @author maotai
 * @since 2026-07-22
 */
@Data
@TableName("mes3_maotai_vehicle_check_item")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "车检项明细")
public class VehicleCheckItem extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "车检流水号(关联 vehicle_check.check_no)")
    private String checkNo;

    @Schema(description = "检查项名称")
    private String itemName;

    @Schema(description = "是否通过 1-通过 0-不通过")
    private Integer passFlag;

    @Schema(description = "备注")
    private String remark;
}
