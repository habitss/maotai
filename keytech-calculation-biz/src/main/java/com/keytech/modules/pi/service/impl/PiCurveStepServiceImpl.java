package com.keytech.modules.pi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.pi.dto.PiCurveStepDto;
import com.keytech.modules.pi.entity.PiCurveStep;
import com.keytech.modules.pi.entity.PiRaStep;
import com.keytech.modules.pi.entity.PiSistarRtSteps;
import com.keytech.modules.pi.mapper.PiCurveStepMapper;
import com.keytech.modules.pi.service.IPiCurveStepService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Description: 曲线步骤配置
 * @Author: jeecg-boot
 * @Date:   2026-01-06
 * @Version: V1.0
 */
@Service
public class PiCurveStepServiceImpl extends BaseServiceImpl<PiCurveStepMapper, PiCurveStep> implements IPiCurveStepService {

    @Autowired
    private PiCurveStepMapper piCurveStepMapper;

    @Override
    public void saveOrUpdateByQuery(String config,String excludeConfig, Integer areaNo,
                                    Integer reccatNo, Object pcuNo, String unitNo, Integer recipeNo, String curveDataType) {
        // 构建查询条件
        LambdaQueryWrapper<PiCurveStep> queryWrapper = Wrappers.<PiCurveStep>lambdaQuery()
                .eq(PiCurveStep::getAreaNo, areaNo)
                //.eq(yearNo != null, PiCurveStep::getYearNo, yearNo)
                .eq(!"3".equals(curveDataType) ,PiCurveStep::getReccatNo, reccatNo)
                .eq(pcuNo != null, PiCurveStep::getPcuNo, pcuNo)
                .eq(PiCurveStep::getUnitNo, unitNo)
                .eq(PiCurveStep::getRecipeNo, recipeNo);
        // 查询是否已存在
        PiCurveStep existing = getOne(queryWrapper,false);

        // 创建实体对象
        PiCurveStep entity = new PiCurveStep();
        entity.setAreaNo(areaNo);
        //entity.setYearNo(yearNo);
        entity.setReccatNo(reccatNo);
        entity.setPcuNo(pcuNo != null ? String.valueOf(pcuNo) : null);
        entity.setUnitNo(unitNo);
        entity.setRecipeNo(recipeNo);
//        entity.setEopNos(config);
        entity.setRopIds(config);
        entity.setExcludeRopIds(excludeConfig);

        if (existing != null && existing.getId() != null) {
            // 更新操作：设置ID
            entity.setId(existing.getId());
            updateById(entity);
        } else {
            // 新增操作
            save(entity);
        }
    }

    /**
     * 根据SISTAR步骤信息获取曲线步骤信息
     * @param piSistarRtStep
     * @return
     */
    @Override
    public PiCurveStep getCurveStep(PiSistarRtSteps piSistarRtStep) {
        LambdaQueryWrapper<PiCurveStep> queryWrapper = Wrappers.<PiCurveStep>lambdaQuery()
                .eq(PiCurveStep::getAreaNo, piSistarRtStep.getAreaNo())
                //.eq(PiCurveStep::getYearNo,  piSistarRtStep.getYearNo())
                .eq(PiCurveStep::getReccatNo, piSistarRtStep.getReccatNo())
                .eq(PiCurveStep::getPcuNo, piSistarRtStep.getPcuNo())
                .eq(PiCurveStep::getUnitNo, piSistarRtStep.getUnitNo())
                .eq(PiCurveStep::getRecipeNo, piSistarRtStep.getRecipeNo());
        return getOne(queryWrapper,false);
    }

    /**
     * 根据RA步骤信息获取曲线步骤信息
     * @param raStep
     * @return
     */
    @Override
    public PiCurveStep getRaCurveStep(PiRaStep raStep) {
        LambdaQueryWrapper<PiCurveStep> queryWrapper = Wrappers.<PiCurveStep>lambdaQuery()
                .eq(PiCurveStep::getAreaNo, raStep.getAreaId())
                .eq(raStep.getLineId() != null , PiCurveStep::getReccatNo, raStep.getLineId())
//                .eq(PiCurveStep::getPcuNo, raStep.getLineId())
                .eq(PiCurveStep::getUnitNo, raStep.getAreaId()+"-"+raStep.getUnitId())
                //.eq(PiCurveStep::getUnitNo, raStep.getUnitId())
                .eq(PiCurveStep::getRecipeNo, raStep.getRecipeId());
        return getOne(queryWrapper,false);
    }

    /**
     * 根据PI步骤信息获取曲线步骤信息
     * @param piCurveStepDto
     * @return
     */
    @Override
    public PiCurveStep getPiCurveStep(PiCurveStepDto piCurveStepDto, String dataType) {
        LambdaQueryWrapper<PiCurveStep> queryWrapper = Wrappers.<PiCurveStep>lambdaQuery()
                //.eq(PiCurveStep::getPcuNo, piCurveStepDto.getAreaNo())
                //.eq(PiCurveStep::getPcuNo, piCurveStepDto.getPcuNo())
                .eq(PiCurveStep::getUnitNo, piCurveStepDto.getUnitNo())
                .eq(PiCurveStep::getRecipeNo, piCurveStepDto.getRecipeNo());

        if("ra".equals(dataType)){
            queryWrapper.eq(PiCurveStep::getAreaNo, piCurveStepDto.getAreaNo());
        }else{
            queryWrapper.eq(PiCurveStep::getPcuNo, piCurveStepDto.getAreaNo());
        }

        return getOne(queryWrapper,false);
    }

    @Override
    public List<PiCurveStep> batchQueryByStepsSql(List<PiSistarRtSteps> stepList) {
        if (CollectionUtils.isEmpty(stepList)) {
            return Collections.emptyList();
        }

        // 限制批次大小，避免SQL过长
        int batchSize = 200;
        List<PiCurveStep> allResults = new ArrayList<>();

        for (int i = 0; i < stepList.size(); i += batchSize) {
            int end = Math.min(stepList.size(), i + batchSize);
            List<PiSistarRtSteps> batch = stepList.subList(i, end);

            List<PiCurveStep> batchResults = piCurveStepMapper.selectByStepList(batch);
            allResults.addAll(batchResults);
        }

        return allResults;
    }


    @Override
    public List<PiCurveStep> batchRaQueryByStepsSql(List<PiRaStep> stepList) {
        if (CollectionUtils.isEmpty(stepList)) {
            return Collections.emptyList();
        }
        // 限制批次大小，避免SQL过长
        int batchSize = 200;
        List<PiCurveStep> allResults = new ArrayList<>();

        for (int i = 0; i < stepList.size(); i += batchSize) {
            int end = Math.min(stepList.size(), i + batchSize);
            List<PiRaStep> batch = stepList.subList(i, end);

            List<PiCurveStep> batchResults = piCurveStepMapper.selectRaByStepList(batch);
            allResults.addAll(batchResults);
        }

        return allResults;
    }


    @Override
    public List<PiCurveStep> batchPiQueryByStepsSql(List<PiCurveStepDto> stepList) {
        if (CollectionUtils.isEmpty(stepList)) {
            return Collections.emptyList();
        }
        // 限制批次大小，避免SQL过长
        int batchSize = 200;
        List<PiCurveStep> allResults = new ArrayList<>();

        for (int i = 0; i < stepList.size(); i += batchSize) {
            int end = Math.min(stepList.size(), i + batchSize);
            List<PiCurveStepDto> batch = stepList.subList(i, end);

            List<PiCurveStep> batchResults = piCurveStepMapper.selectPiByStepList(batch);
            allResults.addAll(batchResults);
        }

        return allResults;
    }
}
