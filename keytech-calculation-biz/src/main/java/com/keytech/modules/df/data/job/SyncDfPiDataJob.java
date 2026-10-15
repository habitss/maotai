package com.keytech.modules.df.data.job;

import com.keytech.modules.df.data.service.ISyncDfDataService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.config.TenantContext;
import org.springframework.stereotype.Component;

/**
 * 同步DF-PI数据
 * 负责通过DBVIEW同步PI数据
 *
 * @author yepeng
 * @since 2025.12.12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SyncDfPiDataJob {
    private final ISyncDfDataService syncDfDataService;

    /**
     * 每10分钟执行一次PI数据的同步
     * 通过XXL-JOB调度，每10分钟执行一次
     * @param params 任务参数: 1006,2025-11-01
     * @return 执行结果
     */
    @XxlJob("syncDfPiDataJob")
    public ReturnT<String> doSyncDfPiDataJob(String params) {
        log.info("同步PI数据任务开始...");
        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        try {
            String[] args = params.split(",");
            Integer tenantId = Integer.parseInt(args[0]);
            TenantContext.setTenant(String.valueOf(tenantId));
            String startDate = args.length > 1 ? args[1] : null;
            String endDate = args.length > 2 ? args[2] : null;
            syncDfDataService.syncDfDataToLog(tenantId, startDate, endDate);
        } catch (Exception e) {
            log.error("同步PI数据任务执行失败", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "同步PI数据任务执行失败：" + e.getMessage());
        } finally {
            TenantContext.clear();
            log.info("同步PI数据任务结束...");
        }
        return ReturnT.SUCCESS;
    }

}

