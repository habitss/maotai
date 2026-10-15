package com.keytech.modules.line.efficiency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.cop.entity.CopDevice;
import com.keytech.modules.line.efficiency.common.enums.LineConstants;
import com.keytech.modules.line.efficiency.entity.ManualDevice;
import com.keytech.modules.line.efficiency.mapper.ManualDeviceMapper;
import com.keytech.modules.line.efficiency.service.IManualDeviceService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 上位机系统手动操作设备对照
 * @Author: jeecg-boot
 * @Date:   2025-12-08
 * @Version: V1.0
 */
@Service
public class ManualDeviceServiceImpl extends BaseServiceImpl<ManualDeviceMapper, ManualDevice> implements IManualDeviceService {

    /**
     * 查询设备名称和编号
     * @return
     */
    @Override
    public Map<String, String> queryDeviceMap() {
        //获取设备对照
        LambdaQueryWrapper<ManualDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ManualDevice::getIsEnabled, LineConstants.ENABLED);
        queryWrapper.isNotNull(ManualDevice::getDeviceName);
        List<ManualDevice> list = list(queryWrapper);
        //得到map key为设备编号 value为设备名称
        Map<String, String> map = list.stream()
            .collect(Collectors.toMap(
                ManualDevice::getDeviceCode,
                ManualDevice::getDeviceName,
                (existing, replacement) -> existing  // 遇到重复时保留现有的值（第一个）
            ));
        return map;
    }
}
