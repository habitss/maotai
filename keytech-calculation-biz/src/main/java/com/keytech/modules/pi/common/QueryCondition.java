package com.keytech.modules.pi.common;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author f'l
 * @date 2024/11/15 10:28
 */
@Data
public class QueryCondition {

    /**
     * 开始时间
     */
    LocalDateTime start;

    /**
     * 结束时间
     */
    LocalDateTime end;

    /**
     * 桶
     */
    String bucket;

    /**
     * 表
     */
    String measurement;

    /**
     * 取值字段
     */
    String field;

    Integer n;

    /**
     * 点位集合
     */
    List<QueryTagCondition> queryTagConditionList;

    /**
     * 查询函数
     */
    QueryType queryType;

    /**
     * 排序
     */
    SortOrder sortOrder;

    /**
     * 秒级
     */
    Integer frequency;
    /**
     * 排序方式枚举
     */
    public enum SortOrder {
        ASC, DESC
    }

}
