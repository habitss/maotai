package com.keytech.modules.plan.mapper;

import com.keytech.modules.plan.dto.PlanOrderRequestVO;
import com.keytech.modules.plan.dto.PlanOrderResultTotalDTO;
import com.keytech.modules.plan.dto.PlanOrderTotalDTO;
import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.plan.entity.PlanOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Date;
import java.util.List;

/**
 * @Description: 计划工单-下发任务
 * @Author: jeecg-boot
 * @Date:   2025-06-12
 * @Version: V1.0
 */
@Mapper
public interface PlanOrderMapper extends BaseMapper<PlanOrder> {
    /**
     * 该方法为彻底删除待做任务
     * @param planId
     * @param planDeviceIdList
     * @param now
     */
    void removeTodoOrder(String planId, List<String> planDeviceIdList, Date now);
    /**
     * 该方法为彻底删除暂停任务
     * @param planId
     * @param planDeviceIdList
     * @param now
     */
    void removePauseOrder(String planId, List<String> planDeviceIdList, Date now);

    /**
     * 查询计划工单-统计
     * @return 数据
     */
    List<PlanOrderTotalDTO> queryPlanOrderTotal(PlanOrderRequestVO planOrder);

    /**
     * 查询计划工单检查项-统计
     * @return 数据
     */
    List<PlanOrderResultTotalDTO> queryPlanOrderItemTotal(PlanOrderRequestVO planOrder);
}
