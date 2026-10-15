package com.keytech.modules.practice.service;

import com.keytech.modules.practice.entity.BestPracticePromotion;
import com.keytech.modules.practice.entity.BestPracticePromotionLog;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: 最佳实践推广记录
 * @Author: jeecg-boot
 * @Date:   2025-03-21
 * @Version: V1.0
 */
public interface IBestPracticePromotionLogService extends BaseService<BestPracticePromotionLog> {
    /**
     * 根据最佳实践评估，保存或更新推广记录
     * @param bestPracticePromotion   最佳实践评估对象
     */
    void saveOrUpdatePromotion(BestPracticePromotion bestPracticePromotion);

    /**
     * 根据最佳实践评估，删除新推广记录
     * @param promotionIds   最佳实践评估id
     */
    void removePromotionLog(String promotionIds);
}
