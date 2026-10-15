package com.keytech.modules.maotai.oplog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.maotai.eh.ehLoading.config.LoadingEhProperties;
import com.keytech.modules.maotai.oplog.entity.ApiOpLog;
import com.keytech.modules.maotai.oplog.mapper.ApiOpLogMapper;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 接口操作日志实现(全系统审计总表)。
 *
 * <p>写入永不抛:成功类失败 log.warn,待重试行失败 log.error(重试能力丢失需人看见)。
 * 项目无 MetaObjectHandler,createTime/createBy 显式 set(清理按 create_time 物理删,不能是 NULL)。
 *
 * @author maotai
 * @since 2026-08-14
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiOpLogServiceImpl extends BaseServiceImpl<ApiOpLogMapper, ApiOpLog> implements ApiOpLogService {

    private final LoadingEhProperties properties;

    @Override
    public void recordInbound(String operator, String apiCode, String orderNo, String checkNo, Integer result, String reqBody, String processMsg) {
        ApiOpLog rec = base(apiCode, orderNo, checkNo)
                .setDirection(ApiOpLog.DIRECTION_IN)
                .setOperator(operator)
                .setResult(result)
                .setReqBody(reqBody)
                .setProcessMsg(StrUtil.maxLength(processMsg, 497));
        saveQuietly(rec, false);
    }

    @Override
    public void attachInboundResponse(String apiCode, String respSummary) {
        try {
            ApiOpLog latest = this.getOne(new QueryWrapper<ApiOpLog>()
                    .eq("api_code", apiCode)
                    .eq("direction", ApiOpLog.DIRECTION_IN)
                    .orderByDesc("create_time")
                    .last("LIMIT 1"));
            if (latest == null) {
                return;
            }
            ApiOpLog patch = new ApiOpLog();
            patch.setId(latest.getId());
            patch.setRespSummary(StrUtil.maxLength(respSummary, 997));
            patch.setUpdateTime(new Date());
            this.updateById(patch);
        } catch (Exception e) {
            log.warn("[接口操作日志] 补记响应摘要失败 apiCode={}", apiCode, e);
        }
    }

    @Override
    public void recordOutbound(String operator, String apiCode, String orderNo, String checkNo, String reqBody, boolean success, String respSummary) {
        ApiOpLog rec = base(apiCode, orderNo, checkNo)
                .setDirection(ApiOpLog.DIRECTION_OUT)
                .setOperator(operator)
                .setResult(success ? 1 : 0)
                .setReqBody(reqBody)
                .setRespSummary(StrUtil.maxLength(respSummary, 997))
                .setProcessMsg(success ? null : (respSummary == null ? "无响应" : respSummary));
        saveQuietly(rec, false);
    }

    /**
     * 挂账行必须独立事务提交:调用方(如工单启动 transition)常处于业务事务中,
     * 推送失败回滚主事务时不能把挂账审计一起回滚——否则重试能力丢失。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordPendingRetry(String operator, String apiCode, String orderNo, String checkNo, String reqBody, String failMsg) {
        Date now = new Date();
        ApiOpLog rec = base(apiCode, orderNo, checkNo)
                .setDirection(ApiOpLog.DIRECTION_OUT)
                .setOperator(operator)
                .setResult(0)
                .setReqBody(reqBody)
                .setProcessMsg(StrUtil.maxLength(failMsg, 497))
                .setRespSummary(StrUtil.maxLength(failMsg, 997))
                .setRetryCount(0)
                .setNextRetryTime(new Date(now.getTime() + properties.getRetryIntervalSeconds() * 1000L))
                .setRetryStatus(ApiOpLog.RETRY_PENDING);
        saveQuietly(rec, true);
    }

    @Override
    public boolean claimForRetry(String id, Date nextRetryTime) {
        return baseMapper.claimForRetry(id, nextRetryTime) == 1;
    }

    @Override
    public void markRetrySuccess(String id, String respSummary) {
        ApiOpLog rec = new ApiOpLog();
        rec.setId(id);
        rec.setRetryStatus(ApiOpLog.RETRY_SUCCESS);
        rec.setResult(1);
        rec.setRespSummary(StrUtil.maxLength(respSummary, 997));
        rec.setNextRetryTime(null);
        rec.setUpdateTime(new Date());
        updateById(rec);
    }

    @Override
    public void markRetryExhausted(String id, String failMsg) {
        ApiOpLog rec = new ApiOpLog();
        rec.setId(id);
        rec.setRetryStatus(ApiOpLog.RETRY_EXHAUSTED);
        rec.setResult(0);
        rec.setProcessMsg(StrUtil.maxLength(failMsg, 497));
        rec.setNextRetryTime(null);
        rec.setUpdateTime(new Date());
        updateById(rec);
    }

    @Override
    public void markSuperseded(String id, String reason) {
        ApiOpLog rec = new ApiOpLog();
        rec.setId(id);
        rec.setRetryStatus(ApiOpLog.RETRY_SUPERSEDED);
        rec.setProcessMsg(StrUtil.maxLength(reason, 497));
        rec.setNextRetryTime(null);
        rec.setUpdateTime(new Date());
        updateById(rec);
    }

    @Override
    public List<ApiOpLog> findRetryBatch(int limit) {
        return this.list(new QueryWrapper<ApiOpLog>()
                .eq("retry_status", ApiOpLog.RETRY_PENDING)
                .le("next_retry_time", new Date())
                .orderByAsc("next_retry_time")
                .last("LIMIT " + limit));
    }

    @Override
    public boolean hasNewerSuccess(String orderNo, String apiCode, Date operateTime) {
        return this.count(new QueryWrapper<ApiOpLog>()
                .eq("order_no", orderNo)
                .eq("api_code", apiCode)
                .eq("result", 1)
                .gt("operate_time", operateTime)) > 0;
    }

    @Override
    public int cleanup(int retentionDays) {
        Date cutoff = new Date(System.currentTimeMillis() - retentionDays * 24L * 3600L * 1000L);
        int total = 0;
        int deleted;
        do {
            deleted = baseMapper.cleanupBefore(cutoff);
            total += deleted;
        } while (deleted >= 500);
        return total;
    }

    private ApiOpLog base(String apiCode, String orderNo, String checkNo) {
        Date now = new Date();
        ApiOpLog rec = new ApiOpLog()
                .setApiCode(apiCode)
                .setOrderNo(orderNo)
                .setCheckNo(checkNo)
                .setOperateTime(now)
                .setRetryCount(0);
        // 父类字段(DataBase 无链式 setter),显式 set——无 MetaObjectHandler,createTime 不能为 NULL(清理按它删)
        rec.setCreateTime(now);
        return rec;
    }

    /** 落库,异常不抛;pending=true 时(重试能力在该行上)失败要 error */
    private void saveQuietly(ApiOpLog rec, boolean pending) {
        try {
            this.save(rec);
        } catch (Exception e) {
            if (pending) {
                log.error("[接口操作日志] 待重试行写入失败,重试能力丢失 apiCode={} orderNo={}", rec.getApiCode(), rec.getOrderNo(), e);
            } else {
                log.warn("[接口操作日志] 记录失败 apiCode={} orderNo={}", rec.getApiCode(), rec.getOrderNo(), e);
            }
        }
    }
}
