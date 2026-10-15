package com.keytech.modules.manage.check.dto;

import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.manage.check.entity.ManageCheckList;
import lombok.Data;

import java.util.List;

/**
 * @Description: 分层检查清单dto
 * @Author: sjw
 * @Date: 2025/4/1 18:19
 */
@Data
public class ManageCheckListDto extends ManageCheckList {
    /**
     * 行动计划列表
     */
    private List<ActionPlanTrack> actionPlanTrackList;
}
