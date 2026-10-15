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
 * 工单明细(API02 查询落库 / 历史 API03 回传共用此表)。
 * push_id 关联主表 mes3_maotai_work_order_push.id。work_order_id 为业务工单号(重复校验天然键)。
 * 字段对应 PDF《程控系统与勾储MES系统接口》第5节工单模型。
 *
 * @author maotai
 * @since 2026-08-04
 */
@Data
@TableName("mes3_maotai_work_order_push_item")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "工单明细(API02查询/API03回传)")
public class MesWorkOrderPushItem extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主表 id")
    private String pushId;

    @Schema(description = "工单编码")
    private String workOrderId;

    @Schema(description = "工单名称")
    private String name;

    @Schema(description = "业务类型 1-装车 2-卸车")
    private Integer busiType;

    @Schema(description = "任务类型 1-5(罐间作业)")
    private Integer taskType;

    @Schema(description = "任务类型名称")
    private String taskTypeName;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "类型名称")
    private String typeName;

    @Schema(description = "操作位置")
    private String location;

    @Schema(description = "车牌号")
    private String carNo;

    @Schema(description = "定量 kg")
    private BigDecimal preWeight;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "进酒入罐总量 kg")
    private BigDecimal inStoreTotal;

    @Schema(description = "进酒罐列表(空格分隔)")
    private String inStoreTanks;

    @Schema(description = "进酒明细(空格分隔)")
    private String inStoreDetails;

    @Schema(description = "放酒出罐总量 kg")
    private BigDecimal outStoreTotal;

    @Schema(description = "放酒罐列表(空格分隔)")
    private String outStoreTanks;

    @Schema(description = "放酒明细(空格分隔)")
    private String outStoreDetails;

    @Schema(description = "计划开始时间")
    private String startTime;

    @Schema(description = "计划结束时间")
    private String endTime;

    @Schema(description = "勾兑批次号")
    private String extendBlendBatchNo;

    @Schema(description = "虹检批次号")
    private String extendInspectionBatchNo;

    @Schema(description = "包装收酒点")
    private String extendPackagePoint;
}
