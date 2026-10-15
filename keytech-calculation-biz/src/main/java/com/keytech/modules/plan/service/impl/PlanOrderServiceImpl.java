package com.keytech.modules.plan.service.impl;

import cn.hutool.core.util.StrUtil;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.plan.dto.PlanOrderRequestVO;
import com.keytech.modules.plan.dto.PlanOrderResultTotalDTO;
import com.keytech.modules.plan.dto.PlanOrderTotalDTO;
import com.keytech.modules.plan.entity.PlanOrder;
import com.keytech.modules.plan.mapper.PlanOrderMapper;
import com.keytech.modules.plan.service.IPlanOrderService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description: 计划工单-下发任务
 * @Author: jeecg-boot
 * @Date:   2025-06-12
 * @Version: V1.0
 */
@Service
public class PlanOrderServiceImpl extends BaseServiceImpl<PlanOrderMapper, PlanOrder> implements IPlanOrderService {

    @Override
    public void removeTodoOrder(String planId, List<String> planDeviceIdList, Date now) {
        baseMapper.removeTodoOrder(planId, planDeviceIdList, now);
    }

    @Override
    public void removePauseOrder(String planId, List<String> planDeviceIdList, Date now) {
        baseMapper.removePauseOrder(planId, planDeviceIdList, now);
    }

    @Override
    public List<PlanOrderTotalDTO> planOrderTotal(PlanOrderRequestVO planOrder) {
        processParam(planOrder);
        return baseMapper.queryPlanOrderTotal(planOrder);
    }

    @Override
    public List<PlanOrderResultTotalDTO> planOrderResultTotal(PlanOrderRequestVO planOrder) {
        processParam(planOrder);
        return baseMapper.queryPlanOrderItemTotal(planOrder);
    }

    private void processParam(PlanOrderRequestVO planOrder){
        if(StrUtil.isNotBlank(planOrder.getRealStartTime_begin())){
            planOrder.setYear(null);
            planOrder.setMonth(null);
        }
        if(StrUtil.isNotBlank(planOrder.getYear()) && StrUtil.isNotBlank(planOrder.getMonth())){
            planOrder.setMonth(planOrder.getYear()+"-"+String.format("%02d", Integer.valueOf(planOrder.getMonth())));
            planOrder.setYear(null);
        }
        if(StrUtil.isNotBlank(planOrder.getDeviceIds())){
            planOrder.setDeviceList(Arrays.asList(planOrder.getDeviceIds().split(",")));
        }
        if(StrUtil.isNotBlank(planOrder.getTaskId())){
            planOrder.setTaskIdList(Arrays.asList(planOrder.getTaskId().split(",")));
        }
    }
}
