package com.keytech.modules.kpi.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.kpi.entity.KpiNotstandardLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: kpi不达标推送记录
 * @Author: jeecg-boot
 * @Date:   2025-07-24
 * @Version: V1.0
 */
@Mapper
public interface KpiNotstandardLogMapper extends BaseMapper<KpiNotstandardLog> {

}
