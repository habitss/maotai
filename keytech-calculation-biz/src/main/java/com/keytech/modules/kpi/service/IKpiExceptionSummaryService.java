package com.keytech.modules.kpi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.kpi.entity.KpiExceptionSummary;
import com.keytech.base.service.BaseService;
import com.keytech.modules.kpi.vo.ExcPercentVo;

/**
 * @Description: KPI异常清单
 * @Author: jeecg-boot
 * @Date:   2025-03-12
 * @Version: V1.0
 */
public interface IKpiExceptionSummaryService extends BaseService<KpiExceptionSummary> {

    /**
     * 获取清单
     * @param page
     * @param kpiExceptionSummary
     * @return
     */
    IPage<KpiExceptionSummary> queryPageList(Page<KpiExceptionSummary> page, KpiExceptionSummary kpiExceptionSummary);

    /**
     * 统计
     * @param kpiExceptionSummary
     * @return
     */
    ExcPercentVo percent(KpiExceptionSummary kpiExceptionSummary);

    /**
     * KPI异常清单[定时任务]
     * @return
     */
    void synchronizeMonthWeekValueJob();
}
