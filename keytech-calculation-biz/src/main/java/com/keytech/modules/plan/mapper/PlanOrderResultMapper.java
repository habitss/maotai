package com.keytech.modules.plan.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.plan.entity.PlanOrderResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Date;
import java.util.List;

/**
 * @Description: 计划工单-下发任务结果
 * @Author: jeecg-boot
 * @Date:   2025-06-12
 * @Version: V1.0
 */
@Mapper
public interface PlanOrderResultMapper extends BaseMapper<PlanOrderResult> {
    /**
     * 该方法为彻底删除
     * @param planId
     * @param planDeviceIdList
     * @param now
     */
    void removeTodoOrderResult(String planId, List<String> planDeviceIdList, Date now);
}
