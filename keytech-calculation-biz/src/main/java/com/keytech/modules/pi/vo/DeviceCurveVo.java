package com.keytech.modules.pi.vo;

import lombok.Data;

/**
 * @FileName DeviceInfoVo
 * @Description 曲线设备
 * @Author HQ
 * @date 2026-01-30
 **/
@Data
public class DeviceCurveVo {
    //设备 id
    private Integer id;
    //设备名称
    private String deviceName;;
    //设备编码
    private String deviceCode;
    //曲线数据类型 1adapter 2ra 3pi
    private Integer curveDataType;
}
