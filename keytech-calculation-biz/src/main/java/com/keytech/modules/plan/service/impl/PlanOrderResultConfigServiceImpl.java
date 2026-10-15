package com.keytech.modules.plan.service.impl;

import com.keytech.modules.plan.entity.PlanOrderResultConfig;
import com.keytech.modules.plan.mapper.PlanOrderResultConfigMapper;
import com.keytech.modules.plan.service.IPlanOrderResultConfigService;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

/**
 * @Description: 工单记录配置表，用于存储工单生成时候的任务条目配置
 * @Author: jeecg-boot
 * @Date:   2025-06-19
 * @Version: V1.0
 */
@Service
public class PlanOrderResultConfigServiceImpl extends BaseServiceImpl<PlanOrderResultConfigMapper, PlanOrderResultConfig> implements IPlanOrderResultConfigService {

}
