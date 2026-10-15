package com.keytech.modules.pi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.keytech.modules.pi.entity.PiSyncConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * @Description: PI数据同步配置
 * @Author: jeecg-boot
 * @Date:   2026-03-31
 * @Version: V1.0
 */
@Mapper
public interface PiSyncConfigMapper extends BaseMapper<PiSyncConfig> {

}
