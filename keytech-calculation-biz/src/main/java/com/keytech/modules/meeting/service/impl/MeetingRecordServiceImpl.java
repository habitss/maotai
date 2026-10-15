package com.keytech.modules.meeting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.meeting.entity.MeetingRecord;
import com.keytech.modules.meeting.entity.MeetingRecordTemplate;
import com.keytech.modules.meeting.mapper.MeetingRecordMapper;
import com.keytech.modules.meeting.service.IMeetingRecordService;
import com.keytech.modules.meeting.service.IMeetingRecordTemplateService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 会议审核
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
@Service
public class MeetingRecordServiceImpl extends BaseServiceImpl<MeetingRecordMapper, MeetingRecord> implements IMeetingRecordService {

    @Autowired
    private IMeetingRecordTemplateService meetingRecordTemplateService;

    @Override
    public List<MeetingRecord> queryListMerge(MeetingRecord meetingRecord) {
        //查询会议评审记录
        List<MeetingRecord> recordList = this.list(new LambdaQueryWrapper<MeetingRecord>().eq(MeetingRecord::getMeetingId, meetingRecord.getMeetingId()).eq(MeetingRecord::getRecordType, meetingRecord.getRecordType()));
        //查询会议评审模版
        List<MeetingRecord> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(recordList)){
            List<MeetingRecordTemplate> templateList = meetingRecordTemplateService.list(new LambdaQueryWrapper<MeetingRecordTemplate>()
                    .eq(MeetingRecordTemplate::getRecordType, meetingRecord.getRecordType()));

            if(CollectionUtils.isNotEmpty(templateList)){
                //合并评审记录分数和模版
                for(MeetingRecordTemplate template : templateList){
                    MeetingRecord record = new MeetingRecord();
                    if(CollectionUtils.isNotEmpty(recordList)){
                        for(MeetingRecord records : recordList){
                           if(records.getRecordTemplateId().equals(String.valueOf(template.getId()))){
                               record.setScore(records.getScore());
                               record.setScoreReason(records.getScoreReason());
                               record.setRecorder(records.getRecorder());
                               record.setLeader(records.getLeader());
                               record.setId(records.getId());
                           }
                        }
                    }
                    record.setMeetingId(meetingRecord.getMeetingId());
                    record.setMeetingType(meetingRecord.getMeetingType());
                    record.setRecordTemplateId(String.valueOf(template.getId()));
                    record.setMeetingType(template.getMeetingType());
                    record.setRecordType(template.getRecordType());
                    record.setContent(template.getContent());
                    record.setStandard(template.getStandard());
                    record.setWeight(template.getWeight());
                    resultList.add(record);
                }
            }
        } else {
            return recordList;
        }
        return resultList;
    }
}
