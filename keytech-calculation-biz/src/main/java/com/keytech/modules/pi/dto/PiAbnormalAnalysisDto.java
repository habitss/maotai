package com.keytech.modules.pi.dto;

import com.keytech.modules.pi.entity.PiAbnormalAnalysis;
import com.keytech.modules.pi.entity.PiAbnormalAnalysisEvaluation;
import com.keytech.modules.pi.entity.PiAbnormalAnalysisPointRecord;
import lombok.Data;

import java.util.List;

/**
 * @Description: TODO
 * @Author: sjw
 * @Date: 2025/3/6 17:48
 */
@Data
public class PiAbnormalAnalysisDto extends PiAbnormalAnalysis {
    // 评价列表
    private List<PiAbnormalAnalysisEvaluation> evaluationList;
    // 异常点位记录列表
    private List<PiAbnormalAnalysisPointRecord> pointRecordList;
}
