package com.keytech.modules.maotai.eh.orderfeedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.maotai.eh.orderfeedback.entity.OrderFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单信息回传 Mapper
 *
 * @author maotai
 * @since 2026-07-22
 */
@Mapper
public interface OrderFeedbackMapper extends BaseMapper<OrderFeedback> {
}
