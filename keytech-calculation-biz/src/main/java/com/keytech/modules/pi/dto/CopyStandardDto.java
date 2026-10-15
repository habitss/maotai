package com.keytech.modules.pi.dto;

import lombok.Data;

/**
 * 标准复制参数
 */
@Data
public class CopyStandardDto {


    /**
     * 报表id(自定义表)
     */
    private String tableId;
    /**
     * 源字段
     */
    private String sourceField;
    /**
     * 目标字段
     */
    private String targetField;
    /**
     * 目标字段名称
     */
    private String targetFieldName;
}
