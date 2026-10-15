package com.keytech.modules.maotai.scada.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.maotai.scada.entity.WorkOrderFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单信息回传通知(API17) Mapper。
 *
 * @author maotai
 * @since 2026-07-29
 */
@Mapper
public interface WorkOrderFeedbackMapper extends BaseMapper<WorkOrderFeedback> {
}
