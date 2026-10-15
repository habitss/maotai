package com.keytech.modules.maotai.oplog.job;

import com.keytech.modules.maotai.eh.ehLoading.config.LoadingEhProperties;
import com.keytech.modules.maotai.oplog.mapper.LoadingPushLogCleanupMapper;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 接口操作日志定期清理 job。
 * 每天 3:17(避开整点)物理 DELETE:
 * ① mes3_maotai_api_op_log 中 create_time 超保留期且非待重试(retry_status<>0)的行;
 * ② mes3_maotai_loading_push_log(client 层原始流水)同保留期一并清。
 * 物理 DELETE(entity 有 @TableLogic,逻辑删除仍占库)。
 *
 * @author maotai
 * @since 2026-08-14
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiOpLogCleanupJob {

    private final ApiOpLogService opLogService;
    private final LoadingEhProperties properties;
    private final LoadingPushLogCleanupMapper pushLogCleanupMapper;

    /** 每天 3:17 执行 */
    @Scheduled(cron = "0 17 3 * * ?")
    public void cleanup() {
        try {
            int n = opLogService.cleanup(properties.getOpLogRetentionDays());
            int m = 0;
            Date cutoff = new Date(System.currentTimeMillis() - properties.getOpLogRetentionDays() * 24L * 3600L * 1000L);
            int deleted;
            do {
                deleted = pushLogCleanupMapper.cleanupPushLogBefore(cutoff);
                m += deleted;
            } while (deleted >= 500);
            log.info("[接口日志清理] 完成:api_op_log 删 {} 行,loading_push_log(E+H推送流水) 删 {} 行(保留 {} 天)", n, m, properties.getOpLogRetentionDays());
        } catch (Exception e) {
            log.error("[接口日志清理] 执行失败", e);
        }
    }
}
