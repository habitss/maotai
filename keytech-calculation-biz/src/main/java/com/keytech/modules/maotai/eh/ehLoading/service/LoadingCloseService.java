package com.keytech.modules.maotai.eh.ehLoading.service;

import com.keytech.modules.maotai.eh.ehLoading.dto.CloseRequest;
import com.keytech.modules.maotai.eh.ehLoading.dto.EhResponse;

/**
 * 工单结束推送(API19: 平台 → E+H /api/Bussiness/Close)。
 * 不落库的便捷入口,直接把 CloseRequest 推给 E+H;token 管理/失效重试下沉到 EhLoadingClient。
 *
 * @author maotai
 * @since 2026-07-23
 */
public interface LoadingCloseService {

    /** 手动结束工单(联调/测试用):直接把请求推给 E+H,不写工单表 */
    EhResponse closeOrder(CloseRequest close);
}
