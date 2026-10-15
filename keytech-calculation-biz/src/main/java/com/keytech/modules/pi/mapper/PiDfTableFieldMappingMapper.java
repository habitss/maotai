package com.keytech.modules.pi.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.pi.entity.PiDfTableFieldMapping;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: PI字段与DF字段映射
 * @Author: jeecg-boot
 * @Date:   2026-01-15
 * @Version: V1.0
 */
@Mapper
public interface PiDfTableFieldMappingMapper extends BaseMapper<PiDfTableFieldMapping> {

}
