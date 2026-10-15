package com.keytech.modules.pi.vo;

import com.keytech.modules.pi.common.MeasurementData;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @FileName DevicePointVO
 * @Description 点位数据
 * @Author HQ
 * @date 2025-10-05
 **/
@Data
public class PointDataVo {

    /**
     * 点位数据集合
     */
    private Map<String, List<MeasurementData>> pointData;

    /**
     * 时间数组集合(分钟)
     */
    private List<Integer> intervals;

    /**
     * 点位 时间-值集合
     */
    private Map<String, List<Object[]>> pointTimeData;

    /**
     * 点位-名称
     */
     private Map<String, String> pointName;

    /**
     * 点位-单位
     */
    private Map<String, String> pointUnit;

}
