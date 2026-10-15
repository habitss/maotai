package com.keytech.modules.pi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @FileName PiCurveStepDto
 * @Description PI曲线步骤数据
 * @Author HQ
 * @date 2025-12-11
 **/
@Data
public class PiCurveStepDto {

    /**产线*/
    private String id;
    /**产线*/
    private String lineCode;
    /**曲线类型*/
    private String curveType;
    /**开始时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    /**结束时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**区域*/
    private String AreaNo;
    /**设备码*/
    private String deviceNo;


    /**设备编号*/
    private String unitNo;
    /**设备名称*/
    private String unitName;
    /**配方*/
    private String recipe;
    /**订单*/
    private String order;
    /**批次*/
    private String batch;
    /**设备名称集合*/
    private String unitNames;
    /**步骤名称*/
    private String eopName;
    /**步骤编码*/
    private String eopNo;
    /**订单配方*/
    private String orderAndBatch;
    /**配方号*/
    private String recipeNo;
    /**ropId*/
    private String ropId;
    /**订单号*/
    private String orderNo;
    /**批次号*/
    private String batchNo;
    /**pcuNo*/
//    private String pcuNo;
    /**数据类型 ra/adapter*/
    private String dataType;




}
