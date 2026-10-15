package com.keytech.modules.maotai.mes.service;

import com.keytech.modules.maotai.mes.dto.MesResponse;

/**
 * 工单信息回传服务(API07: 平台 → 勾储MES)。
 *
 * @author maotai
 * @since 2026-08-06
 */
public interface OrderInfoFeedbackService {

    /**
     * 工单信息回传(API07):按 orderNo 从 loading_order 组装后推勾储MES。
     * @param orderNo 工单号
     * @param rqi 请求流水Id(响应回传同请求)
     * @param status 执行状态 1新建/3开始装卸/5装卸结束/8业务结束/4删除业务
     * @param execData 执行明细JSON(可空)
     * @param feedbackTime 回传时间(可空,为空取当前时间)
     * @return MesResponse(resultCode 1=成功 0=失败;rqi 回传同请求)
     */
    MesResponse pushOrderInfo(String orderNo, String rqi, String status, String execData, String feedbackTime);
}
