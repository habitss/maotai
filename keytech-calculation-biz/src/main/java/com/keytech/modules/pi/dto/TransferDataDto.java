package com.keytech.modules.pi.dto;


import lombok.Data;

/**
 * Transfer 穿梭框数据封装
 *
 */
@Data
public class TransferDataDto {

    /**key*/
    private String key;
    /**名称*/
    private String title;
    /**描述*/
    private String description;
    /**是否禁用*/
    private Boolean disabled;
    /**是否选择*/
    private Boolean chosen;
    /**排序*/
    private Double sort;
    /**字段*/
    private String field;
    /**表名*/
    private String tableName;

}
