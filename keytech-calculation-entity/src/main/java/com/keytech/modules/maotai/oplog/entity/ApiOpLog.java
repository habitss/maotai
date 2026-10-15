package com.keytech.modules.maotai.oplog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.keytech.modules.common.DataBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口操作日志总表(全系统入站+出站接口审计:E+H/中集自控/勾储MES;出站可重试接口兼做重试状态机)。
 *
 * <p>operator 口径:出站手动=登录用户 realname,出站自动="system",
 * 出站取不到登录用户="platform";入站为来源系统标识(如 "E+H"/"SCADA")。
 *
 * <p>retry_status 仅出站可重试接口(API04/11/19)使用:
 * NULL-不参与重试(入站/API16) 0-待重试 1-重试成功 2-重试耗尽 3-已跳过;
 * 与 loading_order.push_status(0待推/1已推/2失败) 语义不同,勿混用。
 *
 * @author maotai
 * @since 2026-08-14
 */
@Data
@TableName("mes3_maotai_api_op_log")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "接口操作日志")
public class ApiOpLog extends DataBase implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String DIRECTION_IN = "IN";
    public static final String DIRECTION_OUT = "OUT";

    /** 重试状态:不参与重试(入站/API16 查询) */
    public static final Integer RETRY_NONE = null;
    /** 重试状态:待重试 */
    public static final Integer RETRY_PENDING = 0;
    /** 重试状态:重试成功 */
    public static final Integer RETRY_SUCCESS = 1;
    /** 重试状态:重试耗尽 */
    public static final Integer RETRY_EXHAUSTED = 2;
    /** 重试状态:已跳过(存在更新的成功记录,无需重发) */
    public static final Integer RETRY_SUPERSEDED = 3;

    @Schema(description = "方向 IN-入站(外部系统→平台) OUT-出站(平台→外部系统)")
    private String direction;

    @Schema(description = "接口编码 API04/08/11/12/15/16/19")
    private String apiCode;

    @Schema(description = "工单号")
    private String orderNo;

    @Schema(description = "车检流水号(API08/11)")
    private String checkNo;

    @Schema(description = "操作人:出站手动=登录用户realname,自动=system,入站=来源系统标识(如E+H/SCADA)")
    private String operator;

    @Schema(description = "操作时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;

    @Schema(description = "最近一次尝试结果 1-成功 0-失败")
    private Integer result;

    @Schema(description = "请求体 JSON")
    private String reqBody;

    @Schema(description = "响应摘要")
    private String respSummary;

    @Schema(description = "处理说明/失败原因")
    private String processMsg;

    @Schema(description = "已重试次数")
    private Integer retryCount;

    @Schema(description = "下次重试时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextRetryTime;

    @Schema(description = "重试状态 NULL-不参与 0-待重试 1-成功 2-耗尽 3-已跳过")
    private Integer retryStatus;
}
