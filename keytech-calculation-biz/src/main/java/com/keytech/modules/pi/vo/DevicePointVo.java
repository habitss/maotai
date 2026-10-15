package com.keytech.modules.pi.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.keytech.modules.pi.entity.PiCurvePartialStandard;
import com.keytech.modules.pi.entity.PiRaStep;
import com.keytech.modules.pi.entity.PiSistarEngUnits;
import com.keytech.modules.pi.entity.PiSistarRtSteps;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @FileName DevicePointVo
 * @Description 设备点位
 * @Author HQ
 * @date 2025-10-05
 **/
@Data
public class DevicePointVo {

    /**
     * 设备点位集合
     */
    private List<?> unitList;

    /**
     * 开始时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     * 步骤集合(通过设备code分组)
     */
    private Map<String, List<StepVo>> stepGroupMap;

    /**
     * 配置的步骤集合 按设备分类
     */
    private Map<String, List<String>> configStepsMap;

    /**
     * 设备分组(Ra)
     */
    private Map<String, Map<String, List<PiRaStep>>> stepGrouped;
    /**
     * 设备分组(Adapter)
     */
    private Map<String, Map<String, List<PiSistarRtSteps>>> adapterStepGrouped;

    /**
     * 局部标准配置集合(按设备分组, key为unitNo)
     */
    private Map<String, List<PiCurvePartialStandard>> partialStandardMap;
    /**
     * 标准生成的步骤集合(按设备分组, key为areaId-unitId)
     */
    private Map<String, List<PiRaStep>> standardStepListMap;
}
