package com.keytech.modules.pi.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.mdm3.entity.MdmDevice;
import com.keytech.modules.pi.entity.PiCurveStandard;
import com.keytech.modules.pi.entity.PiCurveStep;
import com.keytech.modules.pi.entity.PiSistarRtSteps;
import com.keytech.modules.pi.vo.DeviceCurveVo;
import com.keytech.modules.pi.vo.DevicePointVo;
import com.keytech.modules.pi.vo.StepDataVo;

import java.util.List;

/**
 * @Description: Adapter数据抽取-步骤
 * @Author: jeecg-boot
 * @Date:   2025-08-28
 * @Version: V1.0
 */
public interface IPiSistarRtStepsService extends BaseService<PiSistarRtSteps> {

    /**
     * 数据抽取
     */
    void extractData();

    /**
     * 清空MyBatis缓存
     */
    void clearMyBatisCache();

    /**
     * 数据字典抽取
     */
    void dictionaryData();

    /**
     * 设备和点位
     */
    DevicePointVo unitAndPoint(PiSistarRtSteps piSistarRtSteps);

    /**
     * 保存步骤配置
     */
    void stepsSave(PiCurveStep piCurveStep);

    /**
     * 获取步骤数据
     */
    StepDataVo getData(PiSistarRtSteps piSistarRtSteps);
    /**
     * 获取设备
     */
    List<DeviceCurveVo> getDevice(PiSistarRtSteps piSistarRtSteps);

    /**
     * 通过PCU和设备编号获取配方
     */
    List<PiSistarRtSteps> getRecipeByPcuNo(PiCurveStandard piCurveStandard);
    /**
     * 通过PCU和设备编号获取步骤
     */
    Object getStepByPcuNo(String pcuNo, String unitNo, String recipeNo, String reccatNo);

    /**
     * 通过区域编号获取设备
     */
    List<PiSistarRtSteps> getStepsUnitByAreaNo(String areaNo, String recipeNo, String recipeName, String reccatNo);
}
