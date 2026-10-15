package com.keytech.modules.maotai.eh.ehLoading.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.maotai.eh.ehLoading.entity.LoadingOrder;

/**
 * 装卸车工单服务。
 *
 * @author maotai
 * @since 2026-07-21
 */
public interface LoadingOrderService extends BaseService<LoadingOrder> {

    /** 推送单条工单到 E+H,并回写推送状态/时间/响应 */
    LoadingOrder pushOrder(String id);

    /**
     * 手动结束工单(API19):操作员手动触发,调 E+H Close。
     * 仅已推工单可结束;回写 pushMsg/pushTime。不传 CloseType(由 E+H 内部判断)。
     */
    LoadingOrder closeOrder(String id);

    // 车牌交换已停用:待确认"一车绑定一单"业务规则,确定后整体删除
    // /**
    //  * 交换两个工单的车牌(只换 CarNo,其余不变),按各自推送状态分别处理:
    //  * <ul>
    //  *   <li>已推:Close 作废 E+H 旧单 + 用新 OrderNo 重新 Create(CarNo 已换)</li>
    //  *   <li>待推/失败:只改 MES 的 CarNo,不碰 E+H,之后正常推送</li>
    //  * </ul>
    //  */
    // boolean swapCarNo(String id1, String id2);

    /**
     * 工单业务校验(必填由 @Valid 保证;此方法校以下 4.1.2 规则):
     * <ul>
     *   <li>OrderNo 租户内唯一(靠 uk_tenant_order 唯一索引,save/edit 时 catch DuplicateKeyException 幂等提示)</li>
     *   <li>IC 卡号(UInt32)在 0–4294967295 之间</li>
     *   <li>IC 卡号在"已推未结束"工单内不重复(业务结束前不可重复)</li>
     *   <li>有效期 ≤ 48 小时、且 EndDate &gt; BeginDate</li>
     * </ul>
     * @param excludeId 编辑/推送自身时传其 id 以排除
     */
    void validateOrder(LoadingOrder order, String excludeId);

    /** 取当前登录用户真实姓名作为出站操作人,取不到记 "platform" */
    String currentModifyor();
}
