package com.keytech.modules.maotai.oplog.job;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.modules.maotai.eh.ehLoading.client.EhLoadingClient;
import com.keytech.modules.maotai.eh.ehLoading.config.LoadingEhProperties;
import com.keytech.modules.maotai.eh.ehLoading.dto.CloseRequest;
import com.keytech.modules.maotai.eh.ehLoading.dto.LoadingPlanDTO;
import com.keytech.modules.maotai.eh.ehLoading.entity.LoadingOrder;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingOrderService;
import com.keytech.modules.maotai.oplog.entity.ApiOpLog;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;
import com.keytech.modules.maotai.eh.vehiclecheck.dto.VehicleCheckResultRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import com.keytech.modules.maotai.scada.dto.WorkOrderStartRequest;
import com.keytech.modules.maotai.scada.dto.WorkOrderSyncLoadRequest;
import com.keytech.modules.maotai.eh.ehLoading.dto.EhResponse;
import com.keytech.modules.maotai.scada.client.ScadaClient;
import com.keytech.modules.maotai.scada.dto.ScadaResponse;

/**
 * E+H 出站推送失败重试 job。
 *
 * <p>扫 mes3_maotai_api_op_log 里 retry_status=0 且到期的行,按 api_code 反序列化 req_body
 * 重发 API04(Create)/API19(Close)/API11(车检结果)。API16 查询不挂重试(失败即返回由用户重查)。
 *
 * <p>并发防护两层:①claimForRetry 条件 UPDATE 抢占(影响行数=1 才处理,防 job 重叠);
 * ②重发前"超越判断"(API04 查工单是否已推成 / 其余查是否有更新的成功记录),手动重推成功后旧待重试行标记 3-已跳过。
 * 残余毫秒级窗口由 E+H 侧 OrderNo 唯一兜底。
 *
 * <p>重试成功后联动回写 loading_order(API04→push_status=1;API19→pushMsg)。
 *
 * @author maotai
 * @since 2026-08-14
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EhPushRetryJob {

    private static final String API04_CREATE = "API04";
    private static final String API19_CLOSE = "API19";
    private static final String API11_VEHICLE_CHECK = "API11";
    /** 中集SCADA 出站(复用同一套 op_log 重试基建;apiCode 前缀区分) */
    private static final String API05_START = "API05";
    private static final String API14_SYNC_LOAD = "API14";

    /** 0-待推 1-已推 2-推送失败(loading_order.push_status) */
    private static final int ORDER_STATUS_PUSHED = 1;

    /** 公共状态 status(E+H字典):1-新建业务 8-业务结束 */
    private static final int STATUS_EH_NEW = 1;
    private static final int STATUS_EH_FINISH = 8;
    /** 平台内部工单状态 order_status:1-新建 2-车检待校验 3-车检成功 8-装卸车工单结束(终态) */
    private static final int ORDER_STATUS_NEW = 1;
    private static final int ORDER_STATUS_CHECK_PENDING = 2;
    private static final int ORDER_STATUS_CHECK_PASS = 3;
    private static final int ORDER_STATUS_FINISH = 8;

    private final ApiOpLogService opLogService;
    private final EhLoadingClient ehLoadingClient;
    private final ScadaClient scadaClient;
    private final LoadingOrderService loadingOrderService;
    private final LoadingEhProperties properties;
    private final ObjectMapper objectMapper;

    /** 单轮扫描上限,防 E+H 宕机时一批超时请求拖死调度线程 */
    private static final int BATCH_LIMIT = 20;

    @Scheduled(fixedDelayString = "${loading.eh.retry-scan-fixed-delay-ms:60000}")
    public void scanAndRetry() {
        List<ApiOpLog> batch;
        try {
            batch = opLogService.findRetryBatch(BATCH_LIMIT);
        } catch (Exception e) {
            log.error("[E+H重试] 扫描待重试行失败", e);
            return;
        }
        if (batch.isEmpty()) {
            return;
        }
        log.info("[E+H重试] 本轮待重试 {} 条", batch.size());
        for (ApiOpLog row : batch) {
            try {
                processOne(row);
            } catch (Exception e) {
                log.error("[E+H重试] 单条处理异常 id={} apiCode={}", row.getId(), row.getApiCode(), e);
            }
        }
    }

    private void processOne(ApiOpLog row) {
        // ① 抢占(条件 UPDATE;占用时已 +1 retry_count 并顺延 next_retry_time)
        Date next = new Date(System.currentTimeMillis() + properties.getRetryIntervalSeconds() * 1000L);
        if (!opLogService.claimForRetry(row.getId(), next)) {
            return;
        }
        // ② 超越判断:已有更新的成功记录/工单已推成,无需重发
        if (superseded(row)) {
            opLogService.markSuperseded(row.getId(), "存在更新的成功记录或工单已推送成功,跳过重试");
            return;
        }
        // ③ 反序列化 req_body 重发
        EhResponse resp;
        try {
            resp = resend(row);
        } catch (Exception e) {
            log.warn("[E+H重试] 重发异常 id={} apiCode={}", row.getId(), row.getApiCode(), e);
            resp = null;
        }
        boolean success = resp != null && resp.isSuccess();
        if (success) {
            opLogService.markRetrySuccess(row.getId(), resp.getContent());
            writeBack(row);
            log.info("[E+H重试] 重试成功 id={} apiCode={} orderNo={}", row.getId(), row.getApiCode(), row.getOrderNo());
        } else {
            // retry_count 已在抢占时 +1
            int attempts = row.getRetryCount() == null ? 1 : row.getRetryCount() + 1;
            String failMsg = resp == null ? "重试请求异常" : resp.getContent();
            if (attempts >= properties.getRetryMaxAttempts()) {
                opLogService.markRetryExhausted(row.getId(), "重试 " + attempts + " 次仍失败: " + failMsg);
                log.warn("[E+H重试] 重试耗尽 id={} apiCode={} orderNo={} 最终失败: {}",
                        row.getId(), row.getApiCode(), row.getOrderNo(), failMsg);
            } else {
                // 保持 retry_status=0,next_retry_time 已在抢占时顺延,下轮再试
                log.info("[E+H重试] 第 {} 次重试仍失败,等下轮 id={} apiCode={} 失败: {}",
                        attempts, row.getId(), row.getApiCode(), failMsg);
            }
        }
    }

    /** 超越判断:API04 看工单是否已被推成(手动重推路径);其余看 op_log 里有没有更新的成功记录 */
    private boolean superseded(ApiOpLog row) {
        if (API04_CREATE.equals(row.getApiCode()) && StrUtil.isNotBlank(row.getOrderNo())) {
            LoadingOrder order = loadingOrderService.getOne(new QueryWrapper<LoadingOrder>()
                    .eq("order_no", row.getOrderNo()).last("LIMIT 1"));
            if (order != null && order.getPushStatus() != null && order.getPushStatus() == ORDER_STATUS_PUSHED) {
                return true;
            }
        }
        return opLogService.hasNewerSuccess(row.getOrderNo(), row.getApiCode(), row.getOperateTime());
    }

    /** 按 api_code 反序列化 req_body 并重发(复用 EhLoadingClient 原方法,client 内异常已降级返回 fail) */
    private EhResponse resend(ApiOpLog row) throws Exception {
        String body = row.getReqBody();
        switch (row.getApiCode() == null ? "" : row.getApiCode()) {
            case API04_CREATE:
                return ehLoadingClient.createPlan(objectMapper.readValue(body, LoadingPlanDTO.class));
            case API19_CLOSE:
                return ehLoadingClient.closeOrder(objectMapper.readValue(body, CloseRequest.class));
            case API11_VEHICLE_CHECK:
                return ehLoadingClient.sendVehicleCheckResult(objectMapper.readValue(body, VehicleCheckResultRequest.class));
            case API05_START:
                return scadaToEh(scadaClient.startWorkOrder(objectMapper.readValue(body, WorkOrderStartRequest.class)));
            case API14_SYNC_LOAD:
                return scadaToEh(scadaClient.syncLoad(objectMapper.readValue(body, WorkOrderSyncLoadRequest.class)));
            default:
                log.warn("[E+H重试] 不识别的 apiCode={},跳过 id={}", row.getApiCode(), row.getId());
                return null;
        }
    }

    /** ScadaResponse → EhResponse 归一(resultCode 1→code 1),复用本 job 的成败判定 */
    private EhResponse scadaToEh(ScadaResponse resp) {
        EhResponse r = new EhResponse();
        r.setCode(resp != null && resp.isSuccess() ? 1 : 0);
        r.setContent(resp == null ? null : resp.getResultDesp());
        return r;
    }

    /** 重试成功后的业务回写:API04→push_status=1/pushMsg;API19→pushMsg */
    private void writeBack(ApiOpLog row) {
        if (StrUtil.isBlank(row.getOrderNo())) {
            return;
        }
        try {
            LoadingOrder order = loadingOrderService.getOne(new QueryWrapper<LoadingOrder>()
                    .eq("order_no", row.getOrderNo()).last("LIMIT 1"));
            if (order == null) {
                return;
            }
            if (API04_CREATE.equals(row.getApiCode())) {
                order.setPushStatus(ORDER_STATUS_PUSHED);
                order.setPushMsg("重试推送成功");
                order.setPushTime(new Date());
                order.setStatus(STATUS_EH_NEW);        // API04 重试成功:公共状态=1新建业务
                order.setOrderStatus(ORDER_STATUS_NEW); // 平台内部状态=1新建工单
            } else if (API19_CLOSE.equals(row.getApiCode())) {
                order.setPushMsg("结束工单: 重试成功");
                order.setPushTime(new Date());
                order.setStatus(STATUS_EH_FINISH);      // API19 重试成功:公共状态=8业务结束
                order.setOrderStatus(ORDER_STATUS_FINISH); // 平台内部状态=8工单结束(终态)
            } else if (API11_VEHICLE_CHECK.equals(row.getApiCode())) {
                // API11 重试成功:车检结论由勾储MES校验,仅补写 3-车检成功(NULL/2 的单);终态8不覆盖
                if (order.getOrderStatus() == null || order.getOrderStatus() == ORDER_STATUS_CHECK_PENDING
                        || order.getOrderStatus() == ORDER_STATUS_CHECK_PASS) {
                    order.setOrderStatus(ORDER_STATUS_CHECK_PASS);
                }
            } else {
                return;
            }
            loadingOrderService.updateById(order);
        } catch (Exception e) {
            log.warn("[E+H重试] 重试成功后回写工单失败 orderNo={}", row.getOrderNo(), e);
        }
    }
}
