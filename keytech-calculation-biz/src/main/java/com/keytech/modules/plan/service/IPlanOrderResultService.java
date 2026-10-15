package com.keytech.modules.plan.service;

import com.keytech.modules.plan.dto.PlanOrderRequestVO;
import com.keytech.modules.plan.dto.PlanOrderResultDto;
import com.keytech.modules.plan.dto.PlanOrderResultSaveDto;
import com.keytech.modules.plan.dto.PlanOrderResultTotalDTO;
import com.keytech.modules.plan.entity.PlanOrderResult;
import com.keytech.base.service.BaseService;

import java.util.Date;
import java.util.List;

/**
 * @Description: 计划工单-下发任务结果
 * @Author: jeecg-boot
 * @Date:   2025-06-12
 * @Version: V1.0
 */
public interface IPlanOrderResultService extends BaseService<PlanOrderResult> {
    /**
     * 删除未开始工单-该方法是彻底删除
     * @param planId 计划id
     * @param planDeviceIdList 机台计划id
     */
    void removeTodoOrderResult(String planId, List<String> planDeviceIdList, Date now);

    /**
     * 获取任务结果
     * @param orderId
     * @param resultConfigId
     * @return
     */
    List<PlanOrderResultDto> queryByOrderId(String orderId, String resultConfigId);

    /**
     * 添加任务结果
     * @param dto 传入参数
     */
    void saveOrUpdateDataBatch(PlanOrderResultSaveDto dto);
}
