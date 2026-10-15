package com.keytech.modules.pi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据存储对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableSaveDto {

    /**
     * 存储字段集合
     */
    private List<TableFieldValueDto> fieldList;

    /**
     * 表名
     */
    private String table;

    /**
     * 约束条件
     */
    private List<TableConditionDto> conditionList;
}
