package com.keytech.modules.pi.service;

import com.keytech.modules.pi.dto.RaAreaDto;
import com.keytech.modules.pi.entity.PiCurveStep;
import com.keytech.modules.pi.entity.PiRaStep;
import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.vo.DeviceCurveVo;
import com.keytech.modules.pi.vo.DevicePointVo;
import com.keytech.modules.pi.vo.RaLineVo;
import com.keytech.modules.pi.vo.StepDataVo;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @Description: RA-步骤
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
public interface IPiRaStepService extends BaseService<PiRaStep> {

    /**
     * 定时任务接口-RA步骤抽取
     */
    void extractData();
    /**
     * 步骤分页查询
     * @param lastLoadTime
     * @param currentTime
     * @param offset
     * @param batchSize
     * @return
     */
    List<PiRaStep> stepDataWithPaging(Timestamp lastLoadTime, Timestamp currentTime, long offset, int batchSize);

    /**
     * 获取产线
     * @param areaId
     * @return
     */
    List<RaLineVo> getLine(Integer areaId);

    /**
     * 获取区域
     * @return
     */
    List<RaAreaDto> getArea();

    /**
     * 步骤数据补抽
     * @param map
     */
    void retrieveData(Map map);

    /**
     * 获取下一批/上一批
     * @param piRaStep
     * @return
     */
    List<PiRaStep> getBatchByDirection(PiRaStep piRaStep);

    /**
     * 获取设备点位
     * @param piRaStep
     * @return
     */
    DevicePointVo unitAndPoint(PiRaStep piRaStep);

    /**
     * 获取设备
     * @param piRaStep
     * @return
     */
    List<DeviceCurveVo> getDevice(PiRaStep piRaStep);

    /**
     * 步骤保存
     * @param piCurveStep
     */
    void stepsRaSave(PiCurveStep piCurveStep);

    /**
     * 步骤数据
     * @param piRaStep
     * @return
     */
    StepDataVo getData(PiRaStep piRaStep);

    /**
     * 获取其他参数
     * @param piRaStep
     * @return
     */
    Object getOtherParam(PiRaStep piRaStep);

    /**
     * 获取配方
     * @param
     * @return
     */
    Object getRecipeByAreaId(String areaNo, String unitNo);

    /**
     * 获取步骤
     * @param
     * @return
     */
    Object getStepByAreaId(String areaNo, String unitNo, String recipeNo);

    /**
     * 获取最新上一批次
     * @param
     * @return
     */
    PiRaStep getMashingData(PiRaStep piRaStep);

    /**
     * 补抽结束时间
     * @param
     * @return
     */
    void retrieveEndTime(Map map);

    /**
     * 获取下一步骤
     * @param
     * @return
     */
    List<PiRaStep> selectNextData(PiRaStep piRaStep);
}
