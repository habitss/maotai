package com.keytech.modules.fivewhy.dto;

import lombok.Data;

import java.util.Map;

/**
 * @Description: 5why原因DTO
 * @Author: sjw
 * @Date: 2025/1/20 15:08
 */
@Data
public class FiveWhyResonDto {
    private String id;
    private String oneWhy;
    private String oneReason;
    private String twoWhy;
    private String twoReason;
    private String threeWhy;
    private String threeReason;
    private String fourWhy;
    private String fourReason;
    private String fiveWhy;
    private String fiveReason;
    private String sixWhy;
    private String sixReason;
    private String sevenWhy;
    private String sevenReason;
    private String eightWhy;
    private String eightReason;
    private String nineWhy;
    private String nineReason;
    private String tenWhy;
    private String tenReason;
    private String problem;
    private String problemDesc;
}
