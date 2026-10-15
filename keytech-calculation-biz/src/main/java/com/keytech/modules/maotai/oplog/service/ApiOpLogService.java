package com.keytech.modules.maotai.oplog.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.maotai.oplog.entity.ApiOpLog;

import java.util.Date;
import java.util.List;

/**
 * 接口操作日志服务(全系统入站+出站接口审计总表 + 出站重试状态机)。
 *
 * <p>写入原则:所有 record 方法内部 try/catch 不抛,日志失败不影响主流程;
 * 待重试行(retry_status=0)写入失败会 log.error——重试能力随该行丢失,需要人看见。
 *
 * @author maotai
 * @since 2026-08-14
 */
public interface ApiOpLogService extends BaseService<ApiOpLog> {

    /** 入站操作人默认值(E+H 推送场景;SCADA 等其它来源显式传 operator) */
    String OPERATOR_INBOUND = "E+H";
    /** 自动触发(如 API08 联动 API11)的操作人 */
    String OPERATOR_SYSTEM = "system";


    /**
     * 记入站调用(外部系统 → 平台)。operator 默认 "E+H"(可显式传 "SCADA" 等),不参与重试。
     * @param apiCode 接口编码(API08/12/15)
     * @param orderNo 工单号(可空)
     * @param checkNo 车检流水号(API08 用,可空)
     * @param result 1-成功 0-失败
     * @param reqBody 请求体 JSON
     * @param processMsg 处理说明(成功摘要/失败原因)
     */
    /**
     * 记入站调用(带操作方来源,SCADA 入站用):operator 传"SCADA"(后续拿到中集真实用户名后替换)。
     */
    void recordInbound(String operator, String apiCode, String orderNo, String checkNo, Integer result, String reqBody, String processMsg);

    /**
     * 补记入站响应摘要:按 api_code 定位最新一条入站记录,回填 resp_summary。
     * <p>入站 record 发生在响应构造之前拿不到返回报文,由 controller 在 return 前调用本方法补写。
     * @param apiCode 接口编码(与 recordInbound 一致)
     * @param respSummary 响应报文 JSON(超长自动截断)
     */
    void attachInboundResponse(String apiCode, String respSummary);

    /**
     * 记出站调用(平台 → 外部系统,已出结果)。不参与重试(成功调用/不可重试接口如 API16 查询)。
     * @param operator 操作人(登录 realname / "system" / "platform")
     * @param apiCode 接口编码(API04/11/16/19)
     * @param orderNo 工单号(可空)
     * @param checkNo 车检流水号(API11 用,可空)
     * @param reqBody 请求体 JSON
     * @param resp 外部系统响应(E+H)
     */
    /**
     * 记出站调用(通用字符串响应版,供 SCADA 等非 EhResponse 协议复用)。
     * @param success 调用是否成功
     * @param respSummary 响应摘要文案(如 ScadaResponse.resultDesp)
     */
    void recordOutbound(String operator, String apiCode, String orderNo, String checkNo, String reqBody, boolean success, String respSummary);

    /**
     * 记出站失败并挂起待重试(retry_status=0,next_retry_time=now+间隔)。
     * 仅可重试接口(API04/11/19)调用。
     * @param operator 操作人(同 recordOutbound 口径)
     * @param apiCode 接口编码(API04/11/19)
     * @param orderNo 工单号(可空)
     * @param checkNo 车检流水号(API11 用,可空)
     * @param reqBody 请求体 JSON(重发时按 apiCode 反序列化回 DTO)
     * @param failMsg 失败原因
     */
    void recordPendingRetry(String operator, String apiCode, String orderNo, String checkNo, String reqBody, String failMsg);

    /** 抢占一条待重试行(条件 UPDATE,影响行数=1 才算占到;占用时已 +1 retry_count 并顺延 next_retry_time) */
    boolean claimForRetry(String id, Date nextRetryTime);

    /** 重试成功:retry_status=1,result=1,记响应摘要 */
    void markRetrySuccess(String id, String respSummary);

    /** 重试耗尽:retry_status=2,result=0,记最终失败原因 */
    void markRetryExhausted(String id, String failMsg);

    /** 已跳过:retry_status=3(存在更新的成功记录或工单已被手动推成,无需重发) */
    void markSuperseded(String id, String reason);

    /** 取一批到期的待重试行(按 next_retry_time 升序,限量防单轮拖死调度线程) */
    List<ApiOpLog> findRetryBatch(int limit);

    /** 是否存在同工单同接口更晚的成功出站记录(重发前的"超越判断") */
    boolean hasNewerSuccess(String orderNo, String apiCode, Date operateTime);

    /** 物理清理 create_time 超过 retentionDays 的非待重试行,返回本轮删除行数 */
    int cleanup(int retentionDays);
}
