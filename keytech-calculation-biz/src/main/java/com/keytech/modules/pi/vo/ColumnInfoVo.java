package com.keytech.modules.pi.vo;

import lombok.Data;

/**
 * @FileName ColumnInfoVo
 * @Description 表字段信息值对象
 * @Author HQ
 * @date 2025-08-26
 **/
@Data
public class ColumnInfoVo {

    /**
     * 字段名称
     */
    private String columnName;

    /**
     * 字段备注/说明
     */
    private String columnComment;

    /**
     * 字段类型
     */
    private String dataType;

    /**
     * 字段长度
     */
    private Integer maxLength;

    /**
     * 是否可为空
     */
    private Boolean nullable;

    /**
     * 字段顺序
     */
    private Integer columnOrder;
}
