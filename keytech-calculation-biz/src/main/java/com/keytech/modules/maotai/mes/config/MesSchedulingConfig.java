package com.keytech.modules.maotai.mes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启用 Spring @Scheduled(mes API02 工单查询轮询用)。
 * 项目其他定时任务走 xxl-job;mes API02 轮询用 @Scheduled 自包含,不依赖 xxl-job admin。
 *
 * @author maotai
 * @since 2026-08-04
 */
@Configuration
@EnableScheduling
public class MesSchedulingConfig {
}
