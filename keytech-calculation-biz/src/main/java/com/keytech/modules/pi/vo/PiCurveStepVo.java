package com.keytech.modules.pi.vo;

import com.keytech.modules.pi.dto.PiCurveStepDto;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @FileName PiCurveStepDto
 * @Description PI曲线步骤数据
 * @Author HQ
 * @date 2025-12-11
 **/
@Data
public class PiCurveStepVo {

    /**订单号*/
    private String orderNo;
    /**批次*/
    private String batchNo;
    /**配方品种*/
    private String recipe;

    /**配方类型号*/
    //private String reccatNo;
    /**配方类型名称*/
    //private String reccatName;

    /**开始时间*/
    private String startTime;
    /**结束时间*/
    private String endTime;
    /**结束时间*/
    private String recipeName;
    /**步骤配置集合*/
    private Map<String, Boolean> curveStepMap;
    /***/

    /**步骤集合*/
    private List<PiCurveStepDto> stepList;

}
