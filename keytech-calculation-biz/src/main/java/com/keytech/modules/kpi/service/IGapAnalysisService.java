package com.keytech.modules.kpi.service;

import cn.hutool.json.JSONObject;
import com.keytech.modules.kpi.dto.KpiDiffAnalysisDto;
import com.keytech.modules.kpi.entity.GapAnalysis;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: 差距分析
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
public interface IGapAnalysisService extends BaseService<GapAnalysis> {
    void add(GapAnalysis gapAnalysis);

    List<JSONObject> monthGapValue(KpiDiffAnalysisDto dto);

    List<JSONObject> monthTracking(KpiDiffAnalysisDto dto);
}
