package com.keytech.modules.kpi.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.kpi.entity.KpiReportActualDayValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: KPI实际值
 * @Author: jeecg-boot
 * @Date:   2025-02-24
 * @Version: V1.0
 */
@Mapper
public interface KpiReportActualDayValueMapper extends BaseMapper<KpiReportActualDayValue> {

    @Select("select k.kpi_name, v.*  from mes3_kpi_report_actual_day_value v inner join mes3_kpi_info k on v.kpi_code = k.kpi_code " +
            " ${ew.customSqlSegment} ")
    List<KpiReportActualDayValue> listJoinKpiInfo(@Param("ew") Wrapper<KpiReportActualDayValue> queryWrapper);
}
