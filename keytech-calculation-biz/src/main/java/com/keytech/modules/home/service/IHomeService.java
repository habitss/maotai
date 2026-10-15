package com.keytech.modules.home.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.BaseService;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.kpi.dto.KPiConfigDto;
import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.dto.KpiTypeDto;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.lims.entity.LimsResultsAbnormal;
import com.keytech.modules.pi.entity.PiAbnormalAnalysis;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @Description: 首页
 * @Author: jeecg-boot
 * @Date:   2025-02-07
 * @Version: V1.0
 */
public interface IHomeService extends BaseService<KpiInfo> {


    Map<String, Function<WSActionStatisticsDto, JSONObject>> getFunctionMap();

    Object factoryActionStatisticsPi(WSActionStatisticsDto dto);

}
