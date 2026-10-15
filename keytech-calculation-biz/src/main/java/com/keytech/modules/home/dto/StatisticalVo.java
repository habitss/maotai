package com.keytech.modules.home.dto;


import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hq
 * @date 2025年4月9日
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticalVo {
    private String name;
    private int size;
    private double lv;

    private int totalSize;

    private JSONArray data;

    public StatisticalVo(String name, int size, double lv,JSONArray data) {
        this.name = name;
        this.size = size;
        this.lv = lv;
        this.data = data;
    }

    public StatisticalVo(String name, int size, double lv,int totalSize) {
        this.name = name;
        this.size = size;
        this.lv = lv;
        this.totalSize = totalSize;
    }


}
