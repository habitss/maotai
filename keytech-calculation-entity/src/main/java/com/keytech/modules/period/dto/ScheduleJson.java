package com.keytech.modules.period.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author qipengbo
 * @version 1.0.0
 * @ClassName ScheduleJson
 * @Description 排期计划构造json对象
 * @createTime 2025年03月11日 10:34:00
 */
@Data
public class ScheduleJson {

    private Integer num;
    private Integer year;
    private Integer month;
    private String data;
    //        '1': 'green', //已完成
    //        '2': 'red', //延迟
    //        '3': 'red', //延迟完成
    //        '4': 'bc', //待审核
    //        '5': 'red', //未执行
    //        '0': 'red', //不合格
    //        '-1': 'yc', //计划
    //        '6': 'grey' //未执行
    private String state;
    private Date dateStart;
    private Date dateEnd;
    private Integer quarter;
    //0上半年 1 下半年
    private Integer halfYear;

}
