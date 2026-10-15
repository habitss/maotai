package com.keytech.modules.sop.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hq
 * @date 2025/04/21/
 * 部门SOP诊断进度跟踪
 */
@Data
public class SopDepartScheduleTreeVo {
    /** 对应SysDepart中的id字段,前端数据树中的key*/
    private String key;
    //部门名称
    private String title;
    //部门code
    private String orgCode;
    //SOP数
    private Integer sopNumber;
    //已完成
    private Integer sopSuccess;
    //计划
    private Integer sopPlan;
    //延迟
    private Integer sopDelay;
    //完成率
    private String sopRate;

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
    private List<SopDepartScheduleTreeVo> children = new ArrayList<>();

}
