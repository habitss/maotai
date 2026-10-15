package com.keytech.modules.i18n.service.impl;

import com.keytech.modules.i18n.service.I18nRedisSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 国际化词条全量初始化到Redis
 * @author yepeng
 * @since 2025.12.22
 */
@Component
@Slf4j
@Order(10)
@RequiredArgsConstructor
public class I18nRedisInitRunner implements ApplicationRunner {
    private final I18nRedisSyncService i18nRedisSyncService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("[I18N] 开始执行 Redis 国际化全量初始化...");
        long start = System.currentTimeMillis();

        try {
            i18nRedisSyncService.fullSyncAll();
        } catch (Exception e) {
            log.error("[I18N] Redis 初始化失败，系统将继续启动", e);
        }

        log.info("[I18N] Redis 国际化初始化完成，耗时={}ms", System.currentTimeMillis() - start);
    }
}
