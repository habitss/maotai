package com.keytech.modules.meeting.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.meeting.entity.MeetingRecord;
import lombok.Data;

import java.util.List;

/**
 * @author qipengbo
 * @version 1.0.0
 * @ClassName MeetingRecordVO
 * @Description TODO
 * @createTime 2025年04月11日 14:23:00
 */
@Data
public class MeetingRecordVO {

    private String meetingId;

    private String recorder;

    private String leader;

    private List<MeetingRecord> content;

    private List<MeetingRecord> effect;

    @TableField(exist = false)
    private List<ActionPlanTrack> actionPlanTrackList;
}
