package com.keytech.modules.maotai.tank.service;

import com.keytech.modules.maotai.tank.vo.TankRealtimeListVO;

/**
 *
 * 罐体实时数据服务接口
 * @author keytech
 * @since 2026-07-28
 */
public interface ITankRealtimeService {

    /**
     * 按车间索引获取罐体实时数据
     * @param workshopIndex 车间索引（0~3）
     * @return 罐体实时数据列表包装
     */
    TankRealtimeListVO listByWorkshop(Integer workshopIndex);
}
