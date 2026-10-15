package com.keytech.modules.home.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.BaseService;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.kpi.dto.KpiTypeDto;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.pi.entity.PiAbnormalAnalysis;
import com.keytech.modules.pi.entity.PiCustomizeTable;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @Description: APP查询接口
 * @Author: jeecg-boot
 * @Date:   2025-02-07
 * @Version: V1.0
 */
public interface IAppService {

    JSONObject workbenchesSt(WSActionStatisticsDto dto);

    IPage<PiAbnormalAnalysis> findExcTaskList(Page<PiAbnormalAnalysis> page, PiAbnormalAnalysis dto, HttpServletRequest req);

    List<PiCustomizeTable> piTable(PiCustomizeTable dto);

    List<PiCustomizeTableField> piList(PiCustomizeTableField dto);
}
