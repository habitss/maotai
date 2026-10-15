package com.keytech.modules.custom.report.dto;

import lombok.Data;

/**
 * 自定义报表查询字段
 */
@Data
public class CustomQueryFieldDto {
    //查询别名
    private String conditionField;
    //显示文本
    private String conditionText;
    //字段类型
    private String conditionType;
}
