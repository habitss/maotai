package com.keytech.modules.kpi.service;

import com.keytech.modules.kpi.dto.ConsistencyDto;
import com.keytech.modules.kpi.entity.KpiConsistencyReport;
import com.keytech.base.service.BaseService;

import java.util.Date;
import java.util.List;

/**
 * @Description: 一致KPI数据表
 * @Author: jeecg-boot
 * @Date:   2026-03-24
 * @Version: V1.0
 */
public interface IKpiConsistencyReportService extends BaseService<KpiConsistencyReport> {

    /**
     * 查询一致性指数数据列表
     * @param consistencyDto 查询条件
     * @return 列表
     */
    List<ConsistencyDto> queryPageList(ConsistencyDto consistencyDto);

    int physicalDeleteByWeekDate(Date weekDate);
}
