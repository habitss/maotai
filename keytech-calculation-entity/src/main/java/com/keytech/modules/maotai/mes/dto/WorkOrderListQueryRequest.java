package com.keytech.modules.maotai.mes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工单查询请求(API02: 平台 → 勾储MES, /GZMES/workOrders/list, POST)。
 * 嵌套结构对应 PDF 文档:{rqi, serialNo, params:{empNo, password(AES-ECB), timestamp}}。
 * empNo 为动态查询条件(查哪个员工的工单),由平台内部端点转发。
 *
 * @author maotai
 * @since 2026-08-04
 */
@Data
@Schema(description = "工单查询请求(API02 平台→勾储MES)")
public class WorkOrderListQueryRequest {

    @Schema(description = "请求流水Id")
    @JsonProperty("rqi")
    private String rqi;

    @Schema(description = "系统标识")
    @JsonProperty("serialNo")
    private String serialNo;

    @Schema(description = "业务参数(嵌套:empNo/password/timestamp)")
    @JsonProperty("params")
    private Params params;

    @Data
    public static class Params {

        @Schema(description = "员工工号(动态查询条件)")
        @JsonProperty("empNo")
        private String empNo;

        @Schema(description = "密码(AES-ECB 密文)")
        @JsonProperty("password")
        private String password;

        @Schema(description = "时间戳,unix 10 位")
        @JsonProperty("timestamp")
        private String timestamp;
    }
}
