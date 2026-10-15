package com.keytech.modules.pi.dto;

import lombok.Data;

import java.util.List;

/**
 * 报表查询
 * 自定义查询条件(多个条件用 and 拼接)
 */
@Data
public class TableConditionQueryDto {

    /**
     * 查询字段集合
     * 当集合为空查询所有
     */
    private List<String> fieldList;

    /**
     * 表名
     */
    private String table;

    /**
     * 自定义查询条件(条件1=val1 and 条件2=val2)
     */
    private String condition;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方式（DESC、ASC）
     *
     */
    private String sortMode;

    /**
     * 获取行数
     */
    private Integer rowsNumber;

    /**
     * 是否首页统计查询
     */
    private Boolean isIndex;

}
