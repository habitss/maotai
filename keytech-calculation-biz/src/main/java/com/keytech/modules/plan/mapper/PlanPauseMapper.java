package com.keytech.modules.plan.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.plan.entity.PlanPause;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 计划暂停或恢复记录
 * @Author: jeecg-boot
 * @Date:   2025-07-02
 * @Version: V1.0
 */
@Mapper
public interface PlanPauseMapper extends BaseMapper<PlanPause> {

}
