package com.keytech.modules.line.efficiency.mapper;

import com.keytech.modules.line.efficiency.entity.LineEfficiencyStateLast;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 线效状态记录表-最新记录
 * @Author: jeecg-boot
 * @Date:   2025-06-05
 * @Version: V1.0
 */
@Mapper
public interface LineEfficiencyStateLastMapper extends BaseMapper<LineEfficiencyStateLast> {

}
