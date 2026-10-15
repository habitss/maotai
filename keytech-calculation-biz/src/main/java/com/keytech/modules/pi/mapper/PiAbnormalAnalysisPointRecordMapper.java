package com.keytech.modules.pi.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.pi.entity.PiAbnormalAnalysisPointRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: PI异常分析-异常点位记录表
 * @Author: jeecg-boot
 * @Date:   2025-03-06
 * @Version: V1.0
 */
@Mapper
public interface PiAbnormalAnalysisPointRecordMapper extends BaseMapper<PiAbnormalAnalysisPointRecord> {

}
