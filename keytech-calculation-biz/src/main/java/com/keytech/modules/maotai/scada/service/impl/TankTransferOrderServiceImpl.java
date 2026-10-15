package com.keytech.modules.maotai.scada.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keytech.modules.maotai.scada.entity.TankTransferOrder;
import com.keytech.modules.maotai.scada.mapper.TankTransferOrderMapper;
import com.keytech.modules.maotai.scada.service.TankTransferOrderService;
import org.springframework.stereotype.Service;

/**
 * 罐转罐工单服务实现。建单/关单逻辑分别在 API05(ScadaWorkOrderServiceImpl)/
 * API06(ScadaWorkOrderFinishServiceImpl)里调用,本类只提供 IService 能力。
 *
 * @author maotai
 * @since 2026-08-26
 */
@Service
public class TankTransferOrderServiceImpl extends ServiceImpl<TankTransferOrderMapper, TankTransferOrder> implements TankTransferOrderService {
}
