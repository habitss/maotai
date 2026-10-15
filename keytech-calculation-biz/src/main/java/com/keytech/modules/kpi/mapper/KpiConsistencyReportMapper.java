package com.keytech.modules.kpi.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.kpi.entity.KpiConsistencyReport;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 一致KPI数据表
 * @Author: jeecg-boot
 * @Date:   2026-03-24
 * @Version: V1.0
 */
@Mapper
public interface KpiConsistencyReportMapper extends BaseMapper<KpiConsistencyReport> {

    /**
     * 物理删除指定周日期的报表数据（绕过 @TableLogic）
     */
    @Delete("DELETE FROM mes3_kpi_consistency_report WHERE week_date = #{weekDate}")
    int physicalDeleteByWeekDate(java.util.Date weekDate);
}
