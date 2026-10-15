package com.keytech.modules.maotai.scada.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.maotai.scada.entity.TankTransferOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 罐转罐工单(API05/06) Mapper。
 *
 * @author maotai
 * @since 2026-08-26
 */
@Mapper
public interface TankTransferOrderMapper extends BaseMapper<TankTransferOrder> {
}
