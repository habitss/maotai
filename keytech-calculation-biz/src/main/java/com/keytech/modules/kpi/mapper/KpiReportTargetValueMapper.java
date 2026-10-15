package com.keytech.modules.kpi.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.kpi.entity.KpiReportTargetValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: kpi报表目标值
 * @Author: jeecg-boot
 * @Date:   2025-02-24
 * @Version: V1.0
 */
@Mapper
public interface KpiReportTargetValueMapper extends BaseMapper<KpiReportTargetValue> {



    @Select("SELECT c.kpi_name,c.unit,c.kpi_code,c.perent_id typeId,c.parent_id AS kpiParentId,c.sort AS kpiSort ,c.input_sort " +
            ",t.*" +
            " from mes3_kpi_info c INNER JOIN mes3_kpi_type y on c.perent_id = y.id " +
            "LEFT JOIN mes3_kpi_report_target_value t on c.kpi_code = t.kpi_code " +
            "${ew.customSqlSegment}")
    List<KpiReportTargetValue> listByReportIdJoin(@Param("year") Integer year, @Param("ew") QueryWrapper<KpiReportTargetValue> queryWrapper);


}
