package com.keytech.modules.kpi.vo;

import lombok.Data;

/**
 * @author jacksparrow
 * @version v1.0.0
 * @date 2023/3/28 14:40
 */
@Data
public class Kpi5wAnalyseVo {
    private String id;
    private String recordId;
    /**
     * kpi编码
     */
    private String kpiCode;
    /**
     * 问题
     */
    private String why;
    /**
     * 原因
     */
    private String reason;
    //差距贡献率
    private String diffContribute;
    //回答
    private String answer;
    //提出人
    private String presenter;
    //负责人
    private String charge;
    //计划时间
    private String planTime;
    //实际时间
    private String actualTime;
    //确认人
    private String confirmed;
    //现状
    private String status;

    //工厂", required = true)
    private String factory;
    /**
     * 部门
     */
    //部门", required = true)
    private String dept;
    /**
     * 纠正/correct/预防性措施/prevent
     */
    //回答
    private String answerType;
}
