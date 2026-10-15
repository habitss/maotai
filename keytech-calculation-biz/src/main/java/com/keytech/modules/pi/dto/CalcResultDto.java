package com.keytech.modules.pi.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 计算结果辅助类
 * 用于管理同一计算分组的值累加和计算
 */
@Data
public class CalcResultDto {

    /** 计算结果写入的目标字段 */
    private String resultField;

    /** 计算类型：SUM/AVG/MAX/MIN */
    private String calcType;

    /** 参与计算的值列表 */
    private List<Double> values = new ArrayList<>();

    public CalcResultDto(String resultField, String calcType) {
        this.resultField = resultField;
        this.calcType = calcType;
    }

    /**
     * 添加一个值到计算列表
     */
    public void addValue(String val) {
        if (val == null || val.isEmpty()) {
            return;
        }
        try {
            values.add(Double.parseDouble(val));
        } catch (NumberFormatException e) {
            // 忽略无法转换的值
        }
    }

    /**
     * 计算最终结果
     */
    public String calculate() {
        if (values.isEmpty()) {
            return "0.00";
        }

        double result = 0.0;
        switch (calcType) {
            case "SUM":
                result = values.stream().mapToDouble(Double::doubleValue).sum();
                break;
            case "AVG":
                result = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                break;
            case "MAX":
                result = values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                break;
            case "MIN":
                result = values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                break;
            default:
                result = values.stream().mapToDouble(Double::doubleValue).sum();
        }
        return String.format("%.2f", result);
    }
}
