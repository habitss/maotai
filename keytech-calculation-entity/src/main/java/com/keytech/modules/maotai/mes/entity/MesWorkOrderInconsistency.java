package com.keytech.modules.maotai.mes.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 工单重复校验不一致记录(API02):同工单号多次查回但内容不一致时,落一条记 diff_fields。
 * diff_fields 存 JSON 数组 [{field,old,new}]。
 *
 * @author maotai
 * @since 2026-08-13
 */
@Data
@TableName("mes3_maotai_work_order_inconsistency")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "工单重复校验不一致记录(API02)")
public class MesWorkOrderInconsistency extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "工单号")
    private String workOrderId;

    @Schema(description = "触发查询的rqi")
    private String rqi;

    @Schema(description = "员工工号")
    private String empNo;

    @Schema(description = "不一致字段JSON[{field,old,new}]")
    private String diffFields;
}
