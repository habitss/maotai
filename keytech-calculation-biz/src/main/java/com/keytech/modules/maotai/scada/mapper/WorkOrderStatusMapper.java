package com.keytech.modules.maotai.scada.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.maotai.scada.entity.WorkOrderStatus;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单状态更新通知(API13) Mapper。
 *
 * @author maotai
 * @since 2026-07-28
 */
@Mapper
public interface WorkOrderStatusMapper extends BaseMapper<WorkOrderStatus> {
}
