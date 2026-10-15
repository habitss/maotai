package com.keytech.modules.home.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 首页-重点关注PI指标
 * @author hq
 * @date 2025/04/10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceEmphasisOnPiVo {

    private String piCode;
    private String piName;
    private String lineId;
    private String lineName;
    private int lv;        //绿区
    private int huang;     //黄区
    private int hong;      //红区
    private int sum;       //合计
    private double lvDlv;  //绿区率
}

