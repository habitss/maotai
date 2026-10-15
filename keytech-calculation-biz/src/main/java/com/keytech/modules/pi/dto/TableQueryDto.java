package com.keytech.modules.pi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 报表查询
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableQueryDto {

    /**
     * 查询字段集合
     */
    private List<String> fieldList;

    /**
     * 表名
     */
    private String table;

    /**
     * 查询条件集合(条件等号拼接 key=val)
     */
    private List<TableConditionDto> conditionList;

}
