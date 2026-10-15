package com.keytech.modules.pi.dto;

import lombok.Data;

import java.util.Date;

/**
 * 订单批次-点位
 */
@Data
public class OrderPointDto {


    private Date endDate;
    private String orderBatch;
    private Date startDate;
    private String pointCode;
    private String standardids;
}
