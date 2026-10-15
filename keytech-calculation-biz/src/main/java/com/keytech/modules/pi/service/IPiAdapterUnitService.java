package com.keytech.modules.pi.service;

import com.keytech.modules.pi.entity.PiAdapterUnit;
import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.entity.PiCurveStandard;

/**
 * @Description: Adapter-设备
 * @Author: jeecg-boot
 * @Date:   2025-09-03
 * @Version: V1.0
 */
public interface IPiAdapterUnitService extends BaseService<PiAdapterUnit> {

    Object getUnitByPcuNo(PiCurveStandard piCurveStandard);
}
