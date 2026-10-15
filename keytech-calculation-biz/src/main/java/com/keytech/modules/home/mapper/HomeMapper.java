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
 * @Description: 首页统计
 * @Author: jeecg-boot
 * @Date:   2025-03-17
 * @Version: V1.0
 */
@Mapper
public interface HomeMapper extends BaseMapper<KpiInfo> {

}
