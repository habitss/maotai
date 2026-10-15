package com.keytech.modules.plan.service;

import cn.hutool.core.util.StrUtil;
import com.keytech.modules.plan.dto.PlanOrderRequestVO;
import com.keytech.modules.plan.dto.PlanOrderResultTotalDTO;
import com.keytech.modules.plan.dto.PlanOrderTotalDTO;
import com.keytech.modules.plan.entity.PlanOrder;
import com.keytech.base.service.BaseService;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description: 计划工单-下发任务
 * @Author: jeecg-boot
 * @Date:   2025-06-12
 * @Version: V1.0
 */
public interface IPlanOrderService extends BaseService<PlanOrder> {
    /**
     * 删除未开始工单-该方法是彻底删除
     * @param planId 计划id
     * @param planDeviceIdList 机台计划id
     */
    void removeTodoOrder(String planId, List<String> planDeviceIdList, Date now);
    /**
     * 删除暂停工单-该方法是彻底删除
     * @param planId 计划id
     * @param planDeviceIdList 机台计划id
     */
    void removePauseOrder(String planId, List<String> planDeviceIdList, Date now);

    /**
     * 质量检查计划统计
     * @param planOrder 查询参数
     * @return 结果
     */
    List<PlanOrderTotalDTO> planOrderTotal(PlanOrderRequestVO planOrder);

    /**
     * 质量检查统计
     * @param planOrder 查询参数
     * @return 结果
     */
    List<PlanOrderResultTotalDTO> planOrderResultTotal(PlanOrderRequestVO planOrder);

}
