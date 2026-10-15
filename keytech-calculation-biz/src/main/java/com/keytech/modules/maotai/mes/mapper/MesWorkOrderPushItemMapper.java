package com.keytech.modules.maotai.mes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.maotai.mes.entity.MesWorkOrderPushItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单回传明细(API03 workOrders[i]) Mapper。
 *
 * @author maotai
 * @since 2026-08-04
 */
@Mapper
public interface MesWorkOrderPushItemMapper extends BaseMapper<MesWorkOrderPushItem> {
}
