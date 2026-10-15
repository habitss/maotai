package com.keytech.modules.meeting.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.meeting.dto.MeetingCheckDto;
import com.keytech.modules.meeting.dto.MeetingScheduleVO;
import com.keytech.modules.meeting.dto.MeetingTotalDTO;
import com.keytech.modules.meeting.dto.MeetingVO;
import com.keytech.modules.meeting.entity.Meeting;
import com.keytech.base.service.BaseService;
import com.keytech.modules.meeting.entity.MeetingConfig;
import org.jeecg.modules.system.entity.SysDepart;

import java.util.List;

/**
 * @Description: 会议信息
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
public interface IMeetingService extends BaseService<Meeting> {
   IPage<Meeting> pageList(Page<Meeting> page, LambdaQueryWrapper<Meeting> queryWrapper, String year, String month);

   List<Meeting> queryList(LambdaQueryWrapper<Meeting> queryWrapper, String year, String month);

   void setMeetingRecursively(SysDepart sysDepart, int orgType, Meeting meeting);

   void scheduleMeeting(MeetingScheduleVO meeting,String scheduleId);

   String getMeetingCode(MeetingCheckDto meetingCheckDto);

   void setTemplate(Meeting template);

   List<MeetingTotalDTO> meetingStatistics(MeetingVO meetingVO);
}
