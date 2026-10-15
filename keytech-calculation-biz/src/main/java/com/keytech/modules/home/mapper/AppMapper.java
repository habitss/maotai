package com.keytech.modules.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.pi.entity.PiAbnormalAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: App查询
 * @Author: jeecg-boot
 * @Date:   2025-03-17
 * @Version: V1.0
 */
@Mapper
public interface AppMapper extends BaseMapper<KpiInfo> {


    @Select(" select * from mes3_pi_abnormal_analysis a INNER JOIN mes3_pi_customize_table b on a.table_code = b.table_name " +
            " where a.create_time BETWEEN #{dto.opTime} AND #{dto.edTime} and b.line_code = #{dto.lineId} ")
    List<PiAbnormalAnalysis> getPiAbnormalAnalysis(@Param("dto") WSActionStatisticsDto dto);
}
