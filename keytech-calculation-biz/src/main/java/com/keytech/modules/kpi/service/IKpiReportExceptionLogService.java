package com.keytech.modules.kpi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.BaseService;
import com.keytech.modules.kpi.entity.KpiReportExceptionLog;

import java.util.List;

/**
 * @Description: KPI数据修改
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
public interface IKpiReportExceptionLogService extends BaseService<KpiReportExceptionLog> {
    IPage<KpiReportExceptionLog> queryKpiExcList(KpiReportExceptionLog dto, Page<KpiReportExceptionLog> page, QueryWrapper<KpiReportExceptionLog> queryWrapper);

    void batchAddOrEdit(List<KpiReportExceptionLog> kpiReportExceptionLogs);
}
