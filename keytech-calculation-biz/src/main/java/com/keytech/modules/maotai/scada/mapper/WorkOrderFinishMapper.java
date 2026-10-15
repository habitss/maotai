package com.keytech.modules.maotai.scada.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.maotai.scada.entity.WorkOrderFinish;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单结束通知(API06) Mapper。
 *
 * @author maotai
 * @since 2026-07-27
 */
@Mapper
public interface WorkOrderFinishMapper extends BaseMapper<WorkOrderFinish> {
}
