package com.keytech.modules.kpi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceEmphasisVo {

    private String id;
    private String name;
    private String biaoZhun;
    private String yue;
    private String zhou;
    private String curve;
    private String yueColor;
    private String zhouColor;
}
