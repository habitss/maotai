package com.keytech.modules.opl.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @date 2025-09-04
 * 部门Opl诊断进度跟踪
 */
@Data
public class OplDepartScheduleTreeVo {
    /** 对应SysDepart中的id字段,前端数据树中的key*/
    private String key;
    //部门名称
    private String title;
    //部门code
    private String orgCode;
    //Opl数
    private Integer oplNumber;
    //已完成
    private Integer oplSuccess;
    //计划
    private Integer oplPlan;
    //延迟
    private Integer oplDelay;
    //完成率
    private String oplRate;

    //计划数
    private Integer planNumber;
    //已完成
    private Integer planSuccess;
    //计划
    private Integer plan;
    //延迟
    private Integer planDelay;
    //完成率
    private String planRate;

    //
    private List<OplDepartScheduleTreeVo> children = new ArrayList<>();

}
