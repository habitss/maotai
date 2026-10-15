package com.keytech.modules.meeting.service;

import com.keytech.modules.meeting.entity.MeetingRecord;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: 会议审核
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
public interface IMeetingRecordService extends BaseService<MeetingRecord> {
    List<MeetingRecord> queryListMerge(MeetingRecord meetingRecord);
}
