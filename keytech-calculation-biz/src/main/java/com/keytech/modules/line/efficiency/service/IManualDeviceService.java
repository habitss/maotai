package com.keytech.modules.line.efficiency.service;

import com.keytech.modules.line.efficiency.entity.ManualDevice;
import com.keytech.base.service.BaseService;

import java.util.Map;

/**
 * @Description: 上位机系统手动操作设备对照
 * @Author: jeecg-boot
 * @Date:   2025-12-08
 * @Version: V1.0
 */
public interface IManualDeviceService extends BaseService<ManualDevice> {
    /**
     * 查询设备名称和编号
     * @return
     */
    Map<String, String> queryDeviceMap();
}
