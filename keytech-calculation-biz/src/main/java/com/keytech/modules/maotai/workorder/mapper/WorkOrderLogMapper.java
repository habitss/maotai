package com.keytech.modules.maotai.workorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.maotai.workorder.entity.WorkOrderLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单操作日志 Mapper
 *
 * @author workorder
 * @since 2026-09-15
 */
@Mapper
public interface WorkOrderLogMapper extends BaseMapper<WorkOrderLog> {
}
