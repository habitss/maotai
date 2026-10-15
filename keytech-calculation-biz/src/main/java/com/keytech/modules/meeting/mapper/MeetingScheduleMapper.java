package com.keytech.modules.meeting.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.meeting.entity.MeetingSchedule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 会议排期
 * @Author: jeecg-boot
 * @Date:   2025-04-16
 * @Version: V1.0
 */
@Mapper
public interface MeetingScheduleMapper extends BaseMapper<MeetingSchedule> {

}
