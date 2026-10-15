package com.keytech.modules.cop.dto;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * @ClassName: CopScheduleJson
 * @Description:
 * @Author: Jun
 * @Date: 2025/2/20 14:32
 **/
@Data
public class CopScheduleJson {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CopScheduleJson scheduleJson = (CopScheduleJson) o;
        return Objects.equals(state, scheduleJson.state) && Objects.equals(data, scheduleJson.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, data);
    }
}
