package com.keytech.modules.ems.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * EMS 统一线程池配置
 * @author yepneg
 * @since 2025.04.30
 */
@Configuration
public class ThreadPoolConfig {

    // 采集任务线程池
    @Bean("collectExecutor")
    public ExecutorService collectExecutor() {
        return new ThreadPoolExecutor(
                2, 4, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactoryBuilder().setNameFormat("collect-pool-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    // 补采任务线程池
    @Bean("backfillExecutor")
    public ExecutorService backfillExecutor() {
        return new ThreadPoolExecutor(
                4, 8, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(500),
                new ThreadFactoryBuilder().setNameFormat("backfill-pool-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    // 聚合任务线程池
    @Bean("aggregatExecutor")
    public ExecutorService aggregatExecutor() {
        return new ThreadPoolExecutor(
                2, 4, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactoryBuilder().setNameFormat("aggregat-pool-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    // 通用任务线程池
    @Bean("commonExecutor")
    public ExecutorService commonExecutor() {
        return new ThreadPoolExecutor(
                2, 4, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                new ThreadFactoryBuilder().setNameFormat("common-pool-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    // 计划补跑任务线程池（单线程，Redis锁保证串行）
    @Bean("planBackfillExecutor")
    public ExecutorService planBackfillExecutor() {
        return new ThreadPoolExecutor(
                1, 1, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10),
                new ThreadFactoryBuilder().setNameFormat("plan-backfill-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
