package com.keytech.modules.fivewhy.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.fivewhy.entity.FiveWhyAnalyse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: 5Why分析
 * @Author: jeecg-boot
 * @Date:   2025-01-20
 * @Version: V1.0
 */
@Mapper
public interface FiveWhyAnalyseMapper extends BaseMapper<FiveWhyAnalyse> {



    /**
     * 获取删除的5why数据
     * @param
     * @return
     */
    @Select({
            "<script>",
            "SELECT * FROM mes3_five_why_analyse",
            "WHERE update_time BETWEEN #{startDateStr} AND #{endDateStr}",
            "AND del_flag = 1",
            "AND kpi_code IS NOT NULL",
            "AND kpi_code IN ",
            "<foreach collection='kpiCodeList' item='code' open='(' separator=',' close=')'>",
            "   #{code}",
            "</foreach>",
            "AND kpi_code != ''",
            "</script>"
    })
    List<FiveWhyAnalyse> listDel(@Param("startDateStr") String startDateStr,
                                 @Param("endDateStr") String endDateStr,
                                 @Param("kpiCodeList") List<String> kpiCodeList);
}
