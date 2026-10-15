package com.keytech.modules.plan.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.plan.entity.PlanOrderResultConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 工单记录配置表，用于存储工单生成时候的任务条目配置
 * @Author: jeecg-boot
 * @Date:   2025-06-19
 * @Version: V1.0
 */
@Mapper
public interface PlanOrderResultConfigMapper extends BaseMapper<PlanOrderResultConfig> {

}
