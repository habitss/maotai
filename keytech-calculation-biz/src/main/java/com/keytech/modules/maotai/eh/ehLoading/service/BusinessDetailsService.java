package com.keytech.modules.maotai.eh.ehLoading.service;

import com.keytech.modules.maotai.eh.ehLoading.dto.BusinessDetailsRequest;
import com.keytech.modules.maotai.eh.ehLoading.dto.EhResponse;

/**
 * 查询业务命令(API16: 平台 → E+H /api/Bussiness/Details)。
 * 不落库的便捷入口,直接把请求推给 E+H;token 管理/失效重试下沉到 EhLoadingClient。
 *
 * @author maotai
 * @since 2026-07-24
 */
public interface BusinessDetailsService {

    /** 查询 E+H 内未归档的装卸车单据(联调/测试/兜底用) */
    EhResponse queryBusinessDetails(BusinessDetailsRequest req);
}
