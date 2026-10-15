package com.keytech.modules.meeting.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.meeting.entity.MeetingCheck;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 会议签到
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
@Mapper
public interface MeetingCheckMapper extends BaseMapper<MeetingCheck> {

}
