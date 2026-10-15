package com.keytech.modules.line.efficiency.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.line.efficiency.entity.ManualDevice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 上位机系统手动操作设备对照
 * @Author: jeecg-boot
 * @Date:   2025-12-08
 * @Version: V1.0
 */
@Mapper
public interface ManualDeviceMapper extends BaseMapper<ManualDevice> {

}
