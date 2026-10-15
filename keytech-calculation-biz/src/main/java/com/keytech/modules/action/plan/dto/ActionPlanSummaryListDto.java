package com.keytech.modules.action.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: TODO
 * @Author: sjw
 * @Date: 2025/2/6 16:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActionPlanSummaryListDto {
    private String id;
    /**
     * 部门/人员ID
     */
    private String title;
    /**
     * 行动总数
     */
    private int actionTotal;
    /**
     * 完成状态数
     */
    private int finishedStateTotal;
    /**
     * 完成状态率
     */
    private double finishedStateRatio;
    /**
     * 进行中状态
     */
    private int progressStateTotal;
    /**
     * 取消状态数
     */
    private int closeStateTotal;
    /**
     * 延迟状态数
     */
    private int delayStateTotal;
    /**
     * 延迟状态率
     */
    private double delayStateRatio;
    /**
     * 延迟完成状态数
     */
    private int delayFinishStateTotal;
    /**
     * 延迟完成状态率
     */
    private double delayFinishStateRatio;

    /**
     * 第一次延期期限统计数
     */
    private int oneDelayTotal;
    /**
     * 第二次延期期限统计数
     */
    private int twoDelayTotal;
    /**
     * 第三次延期期限统计数
     */
    private int threeDelayTotal;
    /**
     * 当前延期期限总次数
     */
    private int delayTotal;
    /**
     * 当前延期期限率
     */
    private double delayRatio;
    /**
     * 提出人激励
     */
    private String putKindSum;
    /**
     * 责任人激励
     */
    private String dutKindSum;
    private List<ActionPlanSummaryListDto> children;
}
