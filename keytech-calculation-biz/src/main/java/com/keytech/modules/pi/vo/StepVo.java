package com.keytech.modules.pi.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.keytech.modules.pi.entity.PiRaStep;
import com.keytech.modules.pi.entity.PiSistarRtSteps;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @FileName ColumnInfoVO
 * @Description 步骤
 * @Author HQ
 * @date 2025-02-04
 **/
@Data
public class StepVo {

    /**开始时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**结束时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**步骤id*/
    private String id;
    /**步骤名称*/
    private String eopName;
    /**步骤号*/
    private String eopNo;
    /**系统编码*/
    private String ropId;

    /**订单号*/
    private Integer orderNo;
    /**批次号*/
    private Integer batchNo;

    /**RA步骤序号*/
    private Integer stepNr;

}
