package com.keytech.modules.kpi.mapper;

import com.keytech.modules.kpi.vo.KpiTreeVo;
import com.keytech.modules.kpi.vo.YearDataVo;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.kpi.entity.KpiPlanData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: KPI树-基础数据
 * @Author: jeecg-boot
 * @Date:   2025-03-07
 * @Version: V1.0
 */
@Mapper
public interface KpiPlanDataMapper extends BaseMapper<KpiPlanData> {


    @Select("SELECT " +
        "plan_name AS planName, " +
        "SUM(plan_values) AS yearlyTotal, " +
        "SUM(CASE WHEN MONTH(time_day) = 1 THEN plan_values ELSE 0 END) AS one, " +
        "SUM(CASE WHEN MONTH(time_day) = 2 THEN plan_values ELSE 0 END) AS two, " +
        "SUM(CASE WHEN MONTH(time_day) = 3 THEN plan_values ELSE 0 END) AS three, " +
        "SUM(CASE WHEN MONTH(time_day) = 4 THEN plan_values ELSE 0 END) AS four, " +
        "SUM(CASE WHEN MONTH(time_day) = 5 THEN plan_values ELSE 0 END) AS five, " +
        "SUM(CASE WHEN MONTH(time_day) = 6 THEN plan_values ELSE 0 END) AS six, " +
        "SUM(CASE WHEN MONTH(time_day) = 7 THEN plan_values ELSE 0 END) AS seven, " +
        "SUM(CASE WHEN MONTH(time_day) = 8 THEN plan_values ELSE 0 END) AS eight, " +
        "SUM(CASE WHEN MONTH(time_day) = 9 THEN plan_values ELSE 0 END) AS nine, " +
        "SUM(CASE WHEN MONTH(time_day) = 10 THEN plan_values ELSE 0 END) AS ten, " +
        "SUM(CASE WHEN MONTH(time_day) = 11 THEN plan_values ELSE 0 END) AS eleven, " +
        "SUM(CASE WHEN MONTH(time_day) = 12 THEN plan_values ELSE 0 END) AS twelve  " +
        "FROM mes3_kpi_plan_data WHERE three_id = #{threeId} AND YEAR(time_day) = #{year} GROUP BY plan_name")
    List<YearDataVo> yearData(String year, String threeId);



    @Select("SELECT LEFT( three_id, LENGTH(three_id) - 6 ) AS id, 0 AS parentId,SUM( plan_values ) AS `value` ,1 level " +
            "FROM mes3_kpi_plan_data  " +
            "WHERE plan_name = 'diffQuantity' AND time_day LIKE CONCAT(#{month}, '%' )  " +
            "AND (${parameterTree})  " +
            "GROUP BY LEFT ( three_id, LENGTH(three_id) - 6 ) " +
            "UNION ALL " +
            "SELECT LEFT( three_id, LENGTH(three_id) - 3 ) AS id,LEFT( three_id, LENGTH(three_id) - 6 ) AS parentId,SUM( plan_values ) AS `value`,2 level  " +
            "FROM mes3_kpi_plan_data  " +
            "WHERE plan_name = 'diffQuantity' AND time_day LIKE CONCAT( #{month}, '%' )  " +
            "AND (${parameterTree})  " +
            "GROUP BY LEFT( three_id, LENGTH(three_id) - 3 ),LEFT( three_id, LENGTH(three_id) - 6 ) " +
            "UNION ALL " +
            "SELECT three_id AS id,LEFT( three_id, LENGTH(three_id) - 3 ) AS parentId,SUM( plan_values ) AS `value`,3 level  " +
            "FROM mes3_kpi_plan_data  " +
            "WHERE plan_name = 'diffQuantity' AND time_day LIKE CONCAT( #{month}, '%' )  " +
            "AND (${parameterTree})  " +
            "GROUP BY three_id,LEFT( three_id, LENGTH(three_id) - 3 )")
    List<KpiTreeVo> treeData(@Param("month") String month, @Param("parameterTree") String parameterTree);
}
