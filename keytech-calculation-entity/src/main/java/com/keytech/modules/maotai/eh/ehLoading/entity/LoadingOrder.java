package com.keytech.modules.maotai.eh.ehLoading.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 装卸车工单(对接 E+H 装卸车系统)。
 * push_status:0-待推 1-已推 2-推送失败
 * 必填(9):orderNo、carNo、preWeight、productName、validate、driverName、identity、carrierName、customerName
 *
 * @author maotai
 * @since 2026-07-21
 */
@Data
@TableName("mes3_maotai_loading_order")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "装卸车工单")
public class LoadingOrder extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "业务类型 1-销售(出库/装车) 2-采购(入库/卸车)")
    private Integer busiType;

    @Schema(description = "订单号/派车单号")
    @NotBlank
    @Size(max = 50)
    private String orderNo;

    @Schema(description = "车牌号")
    @NotBlank
    @Size(max = 50)
    private String carNo;

    @Schema(description = "定量(预装/预卸量)")
    @NotNull
    private BigDecimal preWeight;

    @Schema(description = "单位 kg/t")
    @Size(max = 50)
    private String unit;

    @Schema(description = "产品名称")
    @NotBlank
    @Size(max = 50)
    private String productName;

    @Schema(description = "来源罐号")
    @Size(max = 50)
    private String tankNameBefore;

    @Schema(description = "预目标罐号(卸车必填,装车可空——动态校验在 validateOrder 按业务类型判断)")
    @Size(max = 50)
    private String tankName;

    @Schema(description = "计划启动时间 yyyy-MM-dd HH:mm:ss")
    @Size(max = 50)
    private String validateBeginDate;

    @Schema(description = "工单过期时间 yyyy-MM-dd HH:mm:ss")
    @Size(max = 50)
    private String validateEndDate;

    @Schema(description = "IC 卡号(0–4294967295),无则 \"0\"")
    @NotBlank
    @Size(max = 50)
    private String validate;

    @Schema(description = "创建人")
    @Size(max = 50)
    private String userName;

    @Schema(description = "创建时间 yyyy-MM-dd HH:mm:ss")
    @Size(max = 50)
    private String beginDate;

    @Schema(description = "司机姓名")
    @NotBlank
    @Size(max = 50)
    private String driverName;

    @Schema(description = "司机身份证号/驾照号")
    @NotBlank
    @Size(max = 50)
    private String identity;

    @Schema(description = "司机手机号")
    @Size(max = 50)
    private String mobile;

    @Schema(description = "批次/勾兑批次号")
    @Size(max = 50)
    private String extendBlendBatchNo;

    @Schema(description = "产品代码")
    @Size(max = 50)
    private String productCode;

    @Schema(description = "鹤位号")
    @Size(max = 50)
    private String batchNo;

    @Size(max = 50)
    private String seatNo;

    @Schema(description = "承运公司名称")
    @NotBlank
    @Size(max = 50)
    private String carrierName;

    @Schema(description = "承运商代码")
    @Size(max = 50)
    private String carrierCode;

    @Schema(description = "客户名称")
    @NotBlank
    @Size(max = 50)
    private String customerName;

    @Schema(description = "客户代码")
    @Size(max = 50)
    private String customerCode;

    @Schema(description = "铅封号")
    @Size(max = 50)
    private String sealNo;

    @Schema(description = "标密")
    @Size(max = 50)
    private String density;

    @Schema(description = "酒精度数(ABV)")
    @Size(max = 50)
    private String abv;

    @Schema(description = "备注")
    @Size(max = 500)
    private String remark;

    @Schema(description = "推送状态 0-待推 1-已推 2-推送失败")
    private Integer pushStatus;

    @Schema(description = "推送时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date pushTime;

    @Schema(description = "推送响应/失败原因")
    private String pushMsg;

    @Schema(description = "业务状态 0-未入厂 1-已入厂 2-已出厂")
    private Integer businessStatus;

    @Schema(description = "实际装卸量 kg(API15 回传)")
    private BigDecimal actualWeight;

    @Schema(description = "装卸开始时间(API15 回传)")
    private String loadStartTime;

    @Schema(description = "装卸结束时间(API15 回传)")
    private String loadEndTime;

    @Schema(description = "公共状态(由对方报文写入,原样承接):E+H 1-新建/5-装卸结束/8-业务结束;API13 中集回传值直接写入")
    private Integer status;

    @Schema(description = "中集SCADA运行状态(API17 专用,原样承接):0-空闲 1-运行 7-暂停 10-异常终止 11-正常结束")
    private Integer scadaStatus;

    @Schema(description = "平台内部工单状态:1-新建工单(API04推送成功) 2-车检结果待校验(API08联动) 3-车检成功(API11回传成功) 4-车检失败(API11回传失败) 6-叫号入场(API12联动) 5-装车/卸车结束(API15联动) 8-工单结束(API19成功,终态不可覆盖)")
    private Integer orderStatus;
}
