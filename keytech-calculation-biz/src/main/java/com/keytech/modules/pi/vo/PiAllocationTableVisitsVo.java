package com.keytech.modules.pi.vo;

import lombok.Data;

import java.util.Date;

/**
 * PI分配报表访问统计
 **/
@Data
public class PiAllocationTableVisitsVo {

    /**
     * 报表名称
     */
    private String tableName;

    /**
     * 访问地址
     */
    private String url;

    /**
     * 访问次数
     */
    private String visitsCount;

    /**
     * 月份
     */
    private String mon1;
    private String mon2;
    private String mon3;
    private String mon4;
    private String mon5;
    private String mon6;
    private String mon7;
    private String mon8;
    private String mon9;
    private String mon10;
    private String mon11;
    private String mon12;
    //查询年份
    private Integer year;
    //查询年份
    private Date startTime;
    //查询年份
    private Date endTime;
    //部门查询范围
    private String deptScope;
}
