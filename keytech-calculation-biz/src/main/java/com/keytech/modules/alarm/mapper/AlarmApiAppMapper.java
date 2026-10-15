package com.keytech.modules.alarm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.alarm.entity.AlarmApiApp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 报警应用表Mapper
 * @author yepeng
 * @since 2026.01.29
 */
@Mapper
public interface AlarmApiAppMapper extends BaseMapper<AlarmApiApp> {

}
