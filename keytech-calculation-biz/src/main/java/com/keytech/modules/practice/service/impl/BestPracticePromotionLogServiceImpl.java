package com.keytech.modules.practice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.keytech.modules.practice.entity.BestPracticePromotion;
import com.keytech.modules.practice.entity.BestPracticePromotionLog;
import com.keytech.modules.practice.mapper.BestPracticePromotionLogMapper;
import com.keytech.modules.practice.service.IBestPracticePromotionLogService;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 最佳实践推广记录
 * @Author: jeecg-boot
 * @Date:   2025-03-21
 * @Version: V1.0
 */
@Service
public class BestPracticePromotionLogServiceImpl extends BaseServiceImpl<BestPracticePromotionLogMapper, BestPracticePromotionLog> implements IBestPracticePromotionLogService {

    @Override
    public void saveOrUpdatePromotion(BestPracticePromotion promotion) {
        //获取记录是否存在
        List<BestPracticePromotionLog> list = this.list(
                new LambdaQueryWrapper<BestPracticePromotionLog>()
                        .eq(BestPracticePromotionLog::getProId, promotion.getId())
        );
        //转换json数据
        List<JSONObject> jsonList = JSONUtil.toList(promotion.getIdentificationShop(), JSONObject.class);

        if(CollUtil.isEmpty(list)){
            //如果记录不存在，组装数据
            for(JSONObject jsonObject : jsonList ){
                BestPracticePromotionLog bestPracticePromotionLog = new BestPracticePromotionLog();
                //copy对象，排除id字段
                BeanUtil.copyProperties(promotion, bestPracticePromotionLog, "id");
                //单独处理的数据
                bestPracticePromotionLog.setProId(promotion.getId());
                bestPracticePromotionLog.setStatus("1".equals(jsonObject.getStr("value")) ? 1 : 0);
                bestPracticePromotionLog.setApplicableWorkshopId(jsonObject.getStr("orgCode"));
                bestPracticePromotionLog.setYearMon(promotion.getYearMon());
                list.add(bestPracticePromotionLog);
            }
        } else {
            //如果记录存在
            //将json数据转换成map集合，方便后续操作
            Map<String, String> result = jsonList.stream()
                .collect(Collectors.toMap(
                        jsonObject -> jsonObject.getStr("orgCode"),
                        jsonObject -> jsonObject.getStr("value")
                ));
            //遍历记录，更新状态
            for(BestPracticePromotionLog log : list ){
                if(result.containsKey(log.getApplicableWorkshopId())){
                    log.setStatus("1".equals(result.get(log.getApplicableWorkshopId())) ? 1 : 0);
                    //单独处理的数据
                    BeanUtil.copyProperties(promotion, log, "id");
                }
            }
        }
        this.saveOrUpdateBatch(list);
    }

    @Override
    public void removePromotionLog(String promotionIds) {
        List<String> promotionIdList = Arrays.asList(promotionIds.split(","));
        this.remove(
                new LambdaUpdateWrapper<BestPracticePromotionLog>()
                        .in(BestPracticePromotionLog::getProId, promotionIdList)
        );
    }
}
