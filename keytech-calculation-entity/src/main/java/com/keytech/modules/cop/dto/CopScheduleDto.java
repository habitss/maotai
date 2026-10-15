package com.keytech.modules.cop.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.keytech.modules.cop.entity.CopSchedule;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: CopScheduleDto
 * @Description: TODO
 * @Author: Jun
 * @Date: 2025/2/20 14:29
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class CopScheduleDto extends CopSchedule {
    //这一条计划排期下面有多条时间的计划
    List<CopScheduleJson> scheduleJsonList = new ArrayList<>();

    private String rowKey;
}
