package com.keytech.modules.meeting.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.meeting.entity.MeetingConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 有效会议类型配置
 * @Author: jeecg-boot
 * @Date:   2025-03-17
 * @Version: V1.0
 */
@Mapper
public interface MeetingConfigMapper extends BaseMapper<MeetingConfig> {

}
