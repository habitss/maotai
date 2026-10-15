package com.keytech.modules.manage.check.dto;

import lombok.Data;

/**
 * @Description: 分层检查汇总统计数据dto
 * @Author: sjw
 * @Date: 2025/4/7 15:03
 */
@Data
public class ManageCheckListSummaryDto {
    //动态文字
    private String dynamicText;

    //问题汇总
    private Integer problemCount;

    //5S问题汇总(问题条数/总问题数)
    private Integer fivesCount;

    //非5S问题汇总(问题条数/总问题数)
    private Integer noFivesCount;

    //5S问题完成数
    private Integer fivesFinishCount;

    //非5S问题完成数
    private Integer noFivesFinishCount;

    //5S问题完成率
    private String fivesFinishRate;

    //非5S问题完成率
    private String noFivesFinishRate;

    //完成总数
    private Integer finishCount;

    //完成率
    private String finishRate;

    private static final long serialVersionUID = 1L;
}
