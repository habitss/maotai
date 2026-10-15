package com.keytech.modules.maotai.mes.service;

import com.keytech.modules.maotai.mes.dto.MesResponse;

/**
 * 工单结束汇总服务(API18: 平台 → 勾储MES)。
 *
 * @author maotai
 * @since 2026-07-31
 */
public interface OrderFinishSummaryService {

    /** 工单结束汇总(API18):按 orderNo 从 loading_order 组装后推勾储MES */
    MesResponse pushOrderFinish(String orderNo, String rqi);
}
