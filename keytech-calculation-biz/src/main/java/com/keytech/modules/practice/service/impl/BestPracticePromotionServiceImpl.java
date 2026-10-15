package com.keytech.modules.practice.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.practice.entity.BestPracticeFill;
import com.keytech.modules.practice.entity.BestPracticePromotion;
import com.keytech.modules.practice.mapper.BestPracticePromotionMapper;
import com.keytech.modules.practice.service.IBestPracticeFillService;
import com.keytech.modules.practice.service.IBestPracticePromotionLogService;
import com.keytech.modules.practice.service.IBestPracticePromotionService;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 最佳实践评估
 * @Author: jeecg-boot
 * @Date:   2025-03-21
 * @Version: V1.0
 */
@Service
public class BestPracticePromotionServiceImpl extends BaseServiceImpl<BestPracticePromotionMapper, BestPracticePromotion> implements IBestPracticePromotionService {
    @Autowired
    private IBestPracticeFillService bestPracticeFillService;

    @Autowired
    private IBestPracticePromotionLogService practicePromotionLogService;

    @Autowired
    private ISysDepartService sysDepartService;

    @Override
    public void saveByFill(String fillId, String score) {
        //获取填报数据
        List<BestPracticeFill> fillList = bestPracticeFillService.list(
                new LambdaQueryWrapper<BestPracticeFill>()
                        .eq(BestPracticeFill::getId, fillId)
        );
        //填报数据不为空执行操作
        if(CollUtil.isNotEmpty(fillList) && StrUtil.isNotBlank(score)){
            BestPracticeFill bestPracticeFill = fillList.get(0);
            BigDecimal bigDecimalScore = new BigDecimal(score);
            //获取最佳实践评估数据
            List<BestPracticePromotion> list = this.list(
                    new LambdaQueryWrapper<BestPracticePromotion>()
                            .eq(BestPracticePromotion::getDataId, fillId)
            );

            BestPracticePromotion bestPracticePromotion = new BestPracticePromotion();
            if(CollUtil.isNotEmpty(list)){
                bestPracticePromotion.setId(list.get(0).getId());
            }
            //工厂填报平均分大于等于18进入最佳实践评估，否则删除
            if(bigDecimalScore.compareTo(BigDecimal.valueOf(18)) >= 0){
                //所有单位信息
                List<SysDepart> allDeptList = sysDepartService.list();
                //编码为key的map
                Map<String, String> allDeptMap = CollStreamUtil.toMap(allDeptList, SysDepart::getOrgCode, bean -> bean.getDepartName());
                //组装数据
                bestPracticePromotion.setTitle(bestPracticeFill.getTitle());
                bestPracticePromotion.setDataId(fillId);
                bestPracticePromotion.setYearMon(bestPracticeFill.getYearMon());
                bestPracticePromotion.setReportFactory(allDeptMap.get(bestPracticeFill.getFactoryCode()));
                bestPracticePromotion.setReportWorkshop(bestPracticeFill.getReportDeptId());
                bestPracticePromotion.setReportType(bestPracticeFill.getImprovementType());
                bestPracticePromotion.setKpiYear(bestPracticeFill.getKpiYear());
                bestPracticePromotion.setKpiCode(bestPracticeFill.getKpiCode());
                bestPracticePromotion.setReviewScore(score);
                bestPracticePromotion.setReviewOpinion(bestPracticeFill.getFactoryReview());
                bestPracticePromotion.setReviewResult(bigDecimalScore.compareTo(BigDecimal.valueOf(22)) > 0 ? 3 : 2);
                bestPracticePromotion.setReviewTime(bestPracticeFill.getReportTime());
                this.saveOrUpdate(bestPracticePromotion);
            } else {
                this.removeById(bestPracticePromotion.getId());
            }
        }
    }

    @Override
    public void deleteByFillIds(String fillIds) {
        List<String> fillIdList = Arrays.asList(fillIds.split(","));
        //评估数据查询条件
        LambdaUpdateWrapper lambdaUpdateWrapper =
                new LambdaUpdateWrapper<BestPracticePromotion>()
                        .in(BestPracticePromotion::getDataId, Arrays.asList(fillIds.split(",")));
        //获取评估数据
        List<BestPracticePromotion> list = this.list(lambdaUpdateWrapper);
        //获取评估数据id
        String promotionIds = list.stream()
                .map(BestPracticePromotion::getId) // 提取每个对象的id
                .collect(Collectors.joining(",")); // 使用逗号连接成一个字符串
        //删除评估数据
        this.remove(lambdaUpdateWrapper);
        //删除推广记录
        practicePromotionLogService.removePromotionLog(promotionIds);
    }
}
