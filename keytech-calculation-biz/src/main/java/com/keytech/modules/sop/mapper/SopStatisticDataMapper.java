package com.keytech.modules.sop.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.sop.entity.SopReviewPlan;
import com.keytech.modules.sop.entity.SopStatisticData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: SOP-诊断计划
 * @Author: jeecg-boot
 * @Date:   2025-04-14
 * @Version: V1.0
 */
@Mapper
public interface SopStatisticDataMapper extends BaseMapper<SopStatisticData> {


    /**
     * 获取回顾
     *
     * */
    @Select(" select a.*,b.sop_number,b.sop_desc from " +
            " mes3_sop_statistic_data a left join " +
            " mes3_sop_home_page b on a.sop_id = b.id "+
            "${ew.customSqlSegment} ")
    List<SopStatisticData> listAll(@Param("ew") QueryWrapper<SopStatisticData> queryWrapper);
}
