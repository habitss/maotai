package com.keytech.modules.maotai.eh.ehLoading.service;

import com.keytech.modules.maotai.eh.ehLoading.dto.EhResponse;
import com.keytech.modules.maotai.eh.ehLoading.dto.LoadingPlanDTO;

/**
 * 装卸车计划推送服务(对接 E+H 装卸车系统)。
 *
 * @author maotai
 * @since 2026-07-21
 */
public interface LoadingPushService {

    /**
     * 推送一条装卸车计划到 E+H(/api/Bussiness/Create)。
     * 内部自动取/缓存 token 并在疑似失效时重试一次。
     */
    EhResponse pushPlan(LoadingPlanDTO plan);
}
