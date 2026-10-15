package com.keytech.modules.pi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.pi.dto.PiCurveStepDto;
import com.keytech.modules.pi.entity.PiCurveStep;
import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.entity.PiRaStep;
import com.keytech.modules.pi.entity.PiSistarRtSteps;

import java.util.List;

/**
 * @Description: 曲线步骤配置
 * @Author: jeecg-boot
 * @Date:   2026-01-06
 * @Version: V1.0
 */
public interface IPiCurveStepService extends BaseService<PiCurveStep> {

    /**
     * 通过查询条件更新数据
     */
    void saveOrUpdateByQuery(String config,String excludeConfig, Integer areaNo,
                             Integer reccatNo, Object pcuNo, String unitNo, Integer recipeNo, String curveDataType);
    /**
     * 通过查询条件获取数据
     */
    PiCurveStep getCurveStep(PiSistarRtSteps piSistarRtStep);

    /**
     * 通过查询条件获取数据
     */
    PiCurveStep getRaCurveStep(PiRaStep raStep);

    /**
     * 通过查询条件批量获取数据
     */
    List<PiCurveStep> batchQueryByStepsSql(List<PiSistarRtSteps> stepList);
    /**
     * 通过查询条件获取数据
     */
    PiCurveStep getPiCurveStep(PiCurveStepDto piCurveStepDto,String dataType);
    /**
     * 通过查询条件批量获取数据
     */
    List<PiCurveStep> batchRaQueryByStepsSql(List<PiRaStep> stepList);

    /**
     * 通过查询条件批量获取数据
     */
    List<PiCurveStep> batchPiQueryByStepsSql(List<PiCurveStepDto> stepDedupList);
}
