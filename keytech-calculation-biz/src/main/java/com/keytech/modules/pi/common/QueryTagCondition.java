package com.keytech.modules.pi.common;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author f'l
 * @date 2024/11/17 23:38
 */

@Data
public class QueryTagCondition {

    /**
     * 点位id
     */
    private  String tagId;

    /**
     * 点位值
     */
    private Float value;


    /**
     * 点位开始时间
     */
    private LocalDateTime start;


    /**
     * 点位结束时间
     */
    private LocalDateTime end;


    private Operator operator;

}
