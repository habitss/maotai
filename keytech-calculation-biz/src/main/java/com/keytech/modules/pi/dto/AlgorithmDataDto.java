package com.keytech.modules.pi.dto;

import com.keytech.modules.pi.entity.*;
import lombok.Data;

import java.util.List;

/**
 * 算法配置
 */
@Data
public class AlgorithmDataDto {

    //计算信号
    private PiCalculateSignal calculateSignal;
    //取数信号 List(json)
    private String retrievalSignal;
    //取数步骤-开始
    private PiRetrievalStep retrievalStepStart;
    //取数步骤-结束
    private PiRetrievalStep retrievalStepEnd;
    //计算模型
    private PiComputationModel computationModel;
    //计算模型-监控取数
    private List<PiComputationMonitorModel> computationMonitorModelList;
}
