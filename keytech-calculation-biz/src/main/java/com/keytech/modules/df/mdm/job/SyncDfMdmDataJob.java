package com.keytech.modules.df.mdm.job;

import com.keytech.modules.df.mdm.service.ISyncDfMdmDataService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.config.TenantContext;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

/**
 * 同步DF-MDM主数据
 * 负责通过DF接口同步MDM主数据
 *
 * @author yepeng
 * @since 2025.12.09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SyncDfMdmDataJob {
    private final ISyncDfMdmDataService syncDfMdmDataService;

    /**
     * 每小时执行一次MDM指标主数据的同步
     * 通过XXL-JOB调度，每小时的第5分钟执行
     * @param params 任务参数: 1006,INST_IND,500,2025-11-01
     * @return 执行结果
     */
    @XxlJob("syncMdmIndicatorJob")
    public ReturnT<String> doSyncMdmIndicatorJob(String params) {
        log.info("同步MDM指标主数据任务开始...");
        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        try {
            String[] args = params.split(",");
            Integer tenantId = Integer.parseInt(args[0]);
            TenantContext.setTenant(String.valueOf(tenantId));
            String modelCodes = args.length > 1 ? args[1] : null;
            if (StringUtils.isBlank(modelCodes)) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "模型编码不能为空");
            }
            List<String> modelCodeList = Arrays.asList(modelCodes.split(";"));
            int size = args.length > 2 ? Integer.parseInt(args[2]) : 1000;
            String startDate = args.length > 3 ? args[3] : null;
            String endDate = args.length > 4 ? args[4] : null;
            syncDfMdmDataService.syncMdmIndicatorData(tenantId, startDate, endDate, size, modelCodeList);
        } catch (Exception e) {
            log.error("同步MDM指标主数据任务执行失败", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "同步MDM指标主数据任务执行失败：" + e.getMessage());
        } finally {
            TenantContext.clear();
            log.info("同步MDM指标主数据任务结束...");
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 每小时执行一次同步任务
     * 通过XXL-JOB调度，每小时的第5分钟执行
     * @param params 任务参数: 1006,IND_STAND,500,2025-11-01
     * @return 执行结果
     */
    @XxlJob("syncMdmDataJob")
    public ReturnT<String> syncMdmDataJob(String params) {
        if(params == null) {
            params = XxlJobHelper.getJobParam();
        }
        try {
            String[] args = params.split(",");
            Integer tenantId = Integer.parseInt(args[0]);
            TenantContext.setTenant(String.valueOf(tenantId));
            String modelCode = args.length > 1 ? args[1] : null;
            if (StringUtils.isBlank(modelCode)) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "模型编码不能为空");
            }
            int size = args.length > 2 ? Integer.parseInt(args[2]) : 1000;
            String startDate = args.length > 3 ? args[3] : null;
            String endDate = args.length > 4 ? args[4] : null;
            if("IND_STAND".equals(modelCode)){
                syncDfMdmDataService.syncMdmIndStandData(tenantId, startDate, endDate, size, modelCode);
            }else if("FUNLOC_BASIC_INFO2".equals(modelCode)){
                syncDfMdmDataService.syncMdmFunlocData(tenantId, startDate, endDate, size, modelCode);
            }
        } catch (Exception e) {
            log.error("同步MDM指标主数据任务执行失败", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "同步MDM指标主数据任务执行失败：" + e.getMessage());
        } finally {
            TenantContext.clear();
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 每小时执行一次MDM配方主数据的同步
     * 通过XXL-JOB调度，每小时的第8分钟执行
     * @param params 任务参数: 1006,FAC_BASIC_INFO_RECIPE,500,2025-11-01
     * @return 执行结果
     */
    @XxlJob("syncMdmRecipeJob")
    public ReturnT<String> doSyncMdmRecipeJob(String params) {
        log.info("同步MDM配方主数据任务开始...");
        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        try {
            String[] args = params.split(",");
            Integer tenantId = Integer.parseInt(args[0]);
            TenantContext.setTenant(String.valueOf(tenantId));
            String modelCodes = args.length > 1 ? args[1] : null;
            if (StringUtils.isBlank(modelCodes)) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "模型编码不能为空");
            }
            List<String> modelCodeList = Arrays.asList(modelCodes.split(";"));
            int size = args.length > 2 ? Integer.parseInt(args[2]) : 1000;
            String startDate = args.length > 3 ? args[3] : null;
            String endDate = args.length > 4 ? args[4] : null;
            syncDfMdmDataService.syncMdmRecipeData(tenantId, startDate, endDate, size, modelCodeList);
        } catch (Exception e) {
            log.error("同步MDM配方主数据任务执行失败", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "同步MDM配方主数据任务执行失败：" + e.getMessage());
        } finally {
            TenantContext.clear();
            log.info("同步MDM配方主数据任务结束...");
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 每小时执行一次MDM报警点位主数据的同步
     * 通过XXL-JOB调度，每小时的第9分钟执行
     * @param params 任务参数: 1006,TAG_DICTIONARY,500,2025-12-01
     * @return 执行结果
     */
    @XxlJob("syncMdmAlarmtagJob")
    public ReturnT<String> doSyncMdmAlarmtagJob(String params) {
        log.info("同步MDM报警点位主数据任务开始...");
        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        try {
            String[] args = params.split(",");
            Integer tenantId = Integer.parseInt(args[0]);
            TenantContext.setTenant(String.valueOf(tenantId));
            String modelCodes = args.length > 1 ? args[1] : null;
            if (StringUtils.isBlank(modelCodes)) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "模型编码不能为空");
            }
            List<String> modelCodeList = Arrays.asList(modelCodes.split(";"));
            int size = args.length > 2 ? Integer.parseInt(args[2]) : 1000;
            String startDate = args.length > 3 ? args[3] : null;
            String endDate = args.length > 4 ? args[4] : null;
            syncDfMdmDataService.syncMdmAlarmtagData(tenantId, startDate, endDate, size, modelCodeList);
        } catch (Exception e) {
            log.error("同步MDM报警点位主数据任务执行失败", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "同步MDM报警点位主数据任务执行失败：" + e.getMessage());
        } finally {
            TenantContext.clear();
            log.info("同步MDM报警点位主数据任务结束...");
        }
        return ReturnT.SUCCESS;
    }

}

