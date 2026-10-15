package com.keytech.modules.line.efficiency.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.line.efficiency.entity.LineEfficiencyRealTimeLast;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 实时计算逻辑-最新记录表
 * @Author: jeecg-boot
 * @Date:   2025-09-05
 * @Version: V1.0
 */
@Mapper
public interface LineEfficiencyRealTimeLastMapper extends BaseMapper<LineEfficiencyRealTimeLast> {

}
