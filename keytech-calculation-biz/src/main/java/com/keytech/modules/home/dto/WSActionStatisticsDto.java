package com.keytech.modules.home.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 参数接收
 * @date 2025年4月5日
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WSActionStatisticsDto {

    private String workShopCode;//车间

    private String factory;    //工厂

    private String dutUserId;  //责任人ID

    private String deptId;   //部门ID

    private String filterTime; //时间筛选

    private String opTime;   //开始时间

    private String edTime;   //结束时间
    /**时间类型1上周 2上月 3本月*/
    private int dateType;

    private String factoryName;

    private String functionName;
    //是否统计周
    private Boolean isWeekly =false;
    //产线
    private String lineId;
}
