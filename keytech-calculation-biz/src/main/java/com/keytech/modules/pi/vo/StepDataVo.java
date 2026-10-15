package com.keytech.modules.pi.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.keytech.modules.pi.entity.PiCurvePartialStandard;
import com.keytech.modules.pi.entity.PiCurveStep;
import com.keytech.modules.pi.entity.PiRaStep;
import com.keytech.modules.pi.entity.PiSistarRtSteps;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @FileName ColumnInfoVO
 * @Description 表字段信息值对象
 * @Author HQ
 * @date 2025-08-26
 **/
@Data
public class StepDataVo {

    /**开始时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**结束时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**步骤集合*/
    private List<PiSistarRtSteps> stepList;
    /**步骤集合(RA)*/
    private List<PiRaStep> raStepList;
    /**步骤配置集合*/
    private Map<String, Boolean> curveStepMap;

    /**曲线局部配置集合*/
    private List<PiCurvePartialStandard> partialStandardList;
    /**标准生成的步骤集合*/
    private List<PiRaStep> standardStepList;
    /**
     * 配方名称
     */
    private String recipeName;
}
