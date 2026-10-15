package com.keytech.modules.practice.service;

import com.keytech.modules.practice.entity.BestPracticePromotion;
import com.keytech.base.service.BaseService;

/**
 * @Description: 最佳实践评估
 * @Author: jeecg-boot
 * @Date:   2025-03-21
 * @Version: V1.0
 */
public interface IBestPracticePromotionService extends BaseService<BestPracticePromotion> {
    /**
     * 根据填报id新增评估数据
     * @param fillId 填报id
     * @param score 分数
     */
    void saveByFill(String fillId, String score);

    void deleteByFillIds(String fillIds);
}
