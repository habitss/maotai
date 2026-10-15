package com.keytech.modules.pi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报表查询 查询条件
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableConditionDto {

    /**
     * 字段名
     */
    private String key;
    /**
     * 参数(值)
     */
    private String val;

}
