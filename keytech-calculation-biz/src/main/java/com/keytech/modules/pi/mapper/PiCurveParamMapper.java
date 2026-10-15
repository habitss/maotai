package com.keytech.modules.pi.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.pi.entity.PiCurveParam;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: PI曲线参数配置
 * @Author: jeecg-boot
 * @Date:   2026-01-19
 * @Version: V1.0
 */
@Mapper
public interface PiCurveParamMapper extends BaseMapper<PiCurveParam> {

}
