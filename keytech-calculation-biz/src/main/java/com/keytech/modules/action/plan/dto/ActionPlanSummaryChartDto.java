package com.keytech.modules.action.plan.dto;

import lombok.Data;

import java.util.List;

/**
 * @Description: TODO
 * @Author: sjw
 * @Date: 2025/2/6 16:51
 */
@Data
public class ActionPlanSummaryChartDto {
    /**
     * 饼图-已完成总数
     */
    private int finishedTotal;

    /**
     * 饼图-进行中总数
     */
    private int progressTotal;

    /**
     * 饼图-延迟总数
     */
    private int delayTotal;
    /**
     * 柱状图数据
     */
    private List<Object> objectList;
}
