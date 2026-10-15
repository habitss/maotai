package com.keytech.modules.practice.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.practice.dto.BestPracticeReviewDto;
import com.keytech.modules.practice.entity.BestPracticeFill;
import com.keytech.modules.practice.entity.BestPracticePromotion;
import com.keytech.modules.practice.entity.BestPracticeReview;
import com.keytech.modules.practice.mapper.BestPracticeReviewMapper;
import com.keytech.modules.practice.service.IBestPracticeFillService;
import com.keytech.modules.practice.service.IBestPracticePromotionService;
import com.keytech.modules.practice.service.IBestPracticeReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @Description: 最佳实践评审
 * @Author: jeecg-boot
 * @Date:   2025-03-19
 * @Version: V1.0
 */
@Service
public class BestPracticeReviewServiceImpl extends BaseServiceImpl<BestPracticeReviewMapper, BestPracticeReview> implements IBestPracticeReviewService {
    @Autowired
    private IBestPracticeFillService bestPracticeFillService;

    @Autowired
    private IBestPracticePromotionService practicePromotionService;

    @Override
    @Transactional
    public void saveOrUpdateData(BestPracticeReviewDto dto) {
        BestPracticeFill bestPracticeFill = new BestPracticeFill();
        bestPracticeFill.setId(dto.getFillId());
        BigDecimal total = BigDecimal.valueOf(0);
        for(BestPracticeReview bestPracticeReview : dto.getList()){
            total = total.add(BigDecimal.valueOf(bestPracticeReview.getCollectValue()));
        }
        BigDecimal result = total.divide(BigDecimal.valueOf(dto.getList().size()), 2, RoundingMode.HALF_UP);
        if(1 == dto.getFillType()){
            bestPracticeFill.setShiftGroupResult(1);
            bestPracticeFill.setShiftGroupAvgScore(result.toString());
            bestPracticeFill.setShiftGroupReportId(dto.getFillReportId());
        }else if(2 == dto.getFillType()){
            bestPracticeFill.setDeptResult(1);
            bestPracticeFill.setDeptAvgScore(result.toString());
            bestPracticeFill.setDeptReportId(dto.getFillReportId());
        }else if(3 == dto.getFillType()){
            bestPracticeFill.setFactoryResult(1);
            bestPracticeFill.setFactoryAvgScore(result.toString());
            bestPracticeFill.setFactoryReportId(dto.getFillReportId());
            //添加评估数据
            practicePromotionService.saveByFill(dto.getFillId(), result.toString());
        }
        bestPracticeFillService.updateById(bestPracticeFill);
        this.saveOrUpdateBatch(dto.getList());
    }
}
