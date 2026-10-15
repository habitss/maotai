package com.keytech.modules.maotai.mes.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 工单查询/回传通知主表(API02 查询落库 / 历史 API03 回传)。
 * 一次 API02 查询落一条;子表明细挂 push_id。rqi 唯一索引(幂等)。
 * source 标识来源:QUERY=API02 查询, PUSH=历史 API03 回传。
 *
 * @author maotai
 * @since 2026-08-04
 */
@Data
@TableName("mes3_maotai_work_order_push")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "工单查询/回传通知(API02/API03)")
public class MesWorkOrderPush extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "请求流水Id")
    private String rqi;

    @Schema(description = "员工工号")
    private String empNo;

    @Schema(description = "平台处理结果")
    private String processMsg;

    @Schema(description = "来源 QUERY-API02查询 PUSH-历史API03回传")
    private String source;
}
