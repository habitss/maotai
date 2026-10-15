package com.keytech.modules.pi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据存储 字段与与值对照
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableFieldValueDto {

    /**
     * 字段名
     */
    private String field;
    /**
     * 参数(值)
     */
    private String fieldVal;
}
