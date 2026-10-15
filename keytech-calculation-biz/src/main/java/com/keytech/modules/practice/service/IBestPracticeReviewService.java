package com.keytech.modules.practice.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.practice.dto.BestPracticeReviewDto;
import com.keytech.modules.practice.entity.BestPracticeReview;

/**
 * @Description: 最佳实践评审
 * @Author: jeecg-boot
 * @Date:   2025-03-19
 * @Version: V1.0
 */
public interface IBestPracticeReviewService extends BaseService<BestPracticeReview> {
    void saveOrUpdateData(BestPracticeReviewDto dto);
}
