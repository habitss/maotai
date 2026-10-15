package com.keytech.modules.kpi.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.kpi.entity.KpiPlatoOne;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: 一级柏拉图（生产流程）
 * @Author: jeecg-boot
 * @Date:   2025-02-10
 * @Version: V1.0
 */
@Mapper
public interface KpiPlatoOneMapper extends BaseMapper<KpiPlatoOne> {

    @Select(" SELECT code, process_name from mes3_kpi_plato_one WHERE parent_code =#{kpiCode} and year =#{year} " +
            " UNION ALL " +
            " SELECT code, controller_name  process_name from mes3_kpi_plato_two WHERE super_code =#{kpiCode} and year =#{year} " +
            " UNION ALL " +
            " SELECT code, pi_name  process_name from mes3_kpi_plato_three  WHERE super_code =#{kpiCode} and year =#{year} "
    )
    List<KpiPlatoOne> listUnion(@Param("kpiCode") String kpiCode, @Param("year") String year);
}
