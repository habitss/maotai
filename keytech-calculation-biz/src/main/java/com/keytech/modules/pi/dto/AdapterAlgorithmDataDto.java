package com.keytech.modules.pi.dto;

import com.keytech.modules.pi.entity.PiAdapterAlgorithm;
import com.keytech.modules.pi.entity.PiAdapterAlgorithmCondition;
import com.keytech.modules.pi.entity.PiComputationMonitorModel;
import lombok.Data;

import java.util.List;

/**
 * Adapter算法配置
 */
@Data
public class AdapterAlgorithmDataDto {

    // 参数配置
    private PiAdapterAlgorithm piAdapterAlgorithm;
    // 约束条件(json)
    private String adapterAlgorithmCondition;
}
