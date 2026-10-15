package com.keytech.modules.pi.dto;

import lombok.Data;

/**
 * 报表参数
 */
@Data
public class TableDataDto {


    /**
     * 查询类型 1.基础数据 2.pi报表
     */
    private String type;

    /**
     * 值(编码)
     */
    private String tableId;


}
