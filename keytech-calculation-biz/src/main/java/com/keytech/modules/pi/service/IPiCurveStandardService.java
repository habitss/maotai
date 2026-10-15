package com.keytech.modules.pi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.modules.pi.entity.*;
import com.keytech.base.service.BaseService;

import java.util.List;
import java.util.Map;

/**
 * @Description: 曲线标准配置
 * @Author: jeecg-boot
 * @Date:   2026-01-22
 * @Version: V1.0
 */
public interface IPiCurveStandardService extends BaseService<PiCurveStandard> {

    Object getDevice(PiCurveStandard piCurveStandard);

    Object getRecipe(PiCurveStandard piCurveStandard);

    Object getStep(PiCurveStandard piCurveStandard);

    void convert(IPage<PiCurveStandard> pageList);

    List<PiCurveStandard> getCurveStandard(Map<String, List<PiSistarRtSteps>> stepEopGroup, List<PiBasePoint> pointList);

    List<PiCurveStandard> getCurveStandardByRa(Map<String, List<PiRaStep>> stepEopGroup, List<PiBasePoint> pointList);

    void recipeConvert(IPage<PiCurveStandardRecipe> pageList);
}
