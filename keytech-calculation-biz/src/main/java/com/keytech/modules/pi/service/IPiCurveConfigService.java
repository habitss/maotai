package com.keytech.modules.pi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.modules.mdm3.entity.MdmDevice;
import com.keytech.modules.pi.dto.PiCurveStepDto;
import com.keytech.modules.pi.entity.PiCurveConfig;
import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.entity.PiCurveStep;
import com.keytech.modules.pi.entity.PiSistarRtSteps;
import com.keytech.modules.pi.vo.DevicePointVo;
import com.keytech.modules.pi.vo.PiCurveStepVo;
import com.keytech.modules.pi.vo.PointDataVo;

import java.util.List;

/**
 * @Description: PI曲线配置
 * @Author: jeecg-boot
 * @Date:   2025-12-09
 * @Version: V1.0
 */
public interface IPiCurveConfigService extends BaseService<PiCurveConfig> {

    /***
     * @Description: 编辑数据
     * @param piCurveConfig
     */
    void editData(PiCurveConfig piCurveConfig);

    /***
     * @Description: 获取设备
     * @param piCurveStepDto
     * @return
     */
    List<MdmDevice> getDevice(PiCurveStepDto piCurveStepDto);

    /**
     * @Description: 获取配方
     * @param piCurveStepDto
     * @return
     */
    List<PiCurveStepVo> getRecipe(PiCurveStepDto piCurveStepDto);

    /**
     * 获取订单
     * @param piCurveStepDto
     * @return
     */
    List<PiCurveStepVo> getOrder(PiCurveStepDto piCurveStepDto);
    /**
     * 获取批次
     * @param piCurveStepDto
     * @return
     */
    List<PiCurveStepVo> getBatch(PiCurveStepDto piCurveStepDto);
    /**
     * 获取数据(步骤时间)
     * @param piCurveStepDto
     * @return
     */
    PiCurveStepVo getData(PiCurveStepDto piCurveStepDto);
    /**
     * 获取数据(点位数据)
     * @param piSistarRtSteps
     * @return
     */
    PointDataVo pointData(PiSistarRtSteps piSistarRtSteps);

    /**
     * 获取批次数据
     * @param piCurveStepDto
     * @return
     */
    List<PiCurveStepDto> getRtBatches(PiCurveStepDto piCurveStepDto);

    /**
     * 获取单位点位
     * @param piCurveStepDto
     * @return
     */
    DevicePointVo unitAndPoint(PiCurveStepDto piCurveStepDto);

    /**
     * 获取点位数据
     * @param piSistarRtSteps
     * @return
     */
    PointDataVo getPointData(PiSistarRtSteps piSistarRtSteps);

    /**
     * 转换数据
     * @param pageList
     */
    void convert(IPage<PiCurveConfig> pageList);

    /**
     * 获取曲线配置
     * @param areaNo
     * @return
     */
    List<PiCurveConfig> getCurveConfigByArea(String areaNo);

    /**
     * 获取曲线数据
     * @param asList
     * @param piCurveStep
     * @return
     */
//    List<PiSistarRtSteps> getDataById(List<String> asList, PiCurveStep piCurveStep);
}
