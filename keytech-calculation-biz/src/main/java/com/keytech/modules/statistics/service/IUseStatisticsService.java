package com.keytech.modules.statistics.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiReportActualDayValue;
import com.keytech.modules.statistics.vo.TableChildrenVo;

import java.util.List;
import java.util.Map;

/**
 * @Description: 
 * @Author: jeecg-boot
 * @Date:   2025-03-07
 * @Version: V1.0
 */
public interface IUseStatisticsService extends BaseService<KpiInfo> {

    List<Map> getStatistics(String checkDateBegin, String checkDateEnd, Boolean week);

    List<TableChildrenVo> getHead();
}
