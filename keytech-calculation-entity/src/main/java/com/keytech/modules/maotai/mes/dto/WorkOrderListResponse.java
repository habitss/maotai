package com.keytech.modules.maotai.mes.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 工单查询响应(API02: 勾储MES → 平台)。
 * 结构对应 PDF 文档:{rqi, serial, resultCode, resultDesp, services[].properties.{empNo, workOrders[]}}。
 * resultCode 1=成功 0=失败(项目约定覆盖 PDF 的 0=成功)。workOrders 为工单明细,解析后落库。
 * 字段对应 PDF《程控系统与勾储MES系统接口》第5节工单模型。
 *
 * @author maotai
 * @since 2026-08-13
 */
@Data
@Schema(description = "工单查询响应(API02 勾储MES→平台)")
public class WorkOrderListResponse {

    @Schema(description = "请求流水Id(回传同请求)")
    @JsonProperty("rqi")
    private String rqi;

    @Schema(description = "系统标识(回传)")
    @JsonProperty("serial")
    private String serial;

    @Schema(description = "状态码 1-成功 0-失败")
    @JsonProperty("resultCode")
    private Integer resultCode;

    @Schema(description = "描述")
    @JsonProperty("resultDesp")
    private String resultDesp;

    @Schema(description = "服务列表(工单在 services[0].properties.workOrders)")
    @JsonProperty("services")
    private List<Service> services;

    public boolean isSuccess() {
        return resultCode != null && resultCode == 1;
    }

    @Data
    public static class Service {

        @Schema(description = "属性集(含工单列表)")
        @JsonProperty("properties")
        private Properties properties;
    }

    @Data
    public static class Properties {

        @Schema(description = "员工工号")
        @JsonProperty("empNo")
        private String empNo;

        @Schema(description = "工单列表")
        @JsonProperty("workOrders")
        private List<WorkOrderVO> workOrders;
    }

    @Data
    public static class WorkOrderVO {

        @Schema(description = "工单编码")
        @JsonProperty("id")
        private String id;

        @Schema(description = "工单名称")
        private String name;

        @Schema(description = "任务类型 1-5")
        private Integer taskType;

        @Schema(description = "任务类型名称")
        private String taskTypeName;

        @Schema(description = "类型")
        private String type;

        @Schema(description = "类型名称")
        private String typeName;

        @Schema(description = "操作位置 栋-库信息/缸区")
        private String location;

        @Schema(description = "计划放酒出罐总量 kg")
        private String outStoreTotal;

        @Schema(description = "放酒罐列表(空格分隔)")
        private String outStoreTanks;

        @Schema(description = "放酒明细(空格分隔)")
        private String outStoreDetails;

        @Schema(description = "计划进酒入罐总量 kg")
        private String inStoreTotal;

        @Schema(description = "进酒罐列表(空格分隔)")
        private String inStoreTanks;

        @Schema(description = "进酒明细(空格分隔)")
        private String inStoreDetails;

        @Schema(description = "计划开始时间")
        private String startTime;

        @Schema(description = "计划结束时间")
        private String endTime;

        @Schema(description = "勾兑批次号(空格分隔)")
        private String extendBlendBatchNo;

        @Schema(description = "虹检批次号")
        private String extendInspectionBatchNo;

        @Schema(description = "包装收酒点")
        private String extendPackagePoint;

        @Schema(description = "扩展信息(出库/入库/转运,暂不落库,联调再补)")
        private JsonNode extend;
    }
}
