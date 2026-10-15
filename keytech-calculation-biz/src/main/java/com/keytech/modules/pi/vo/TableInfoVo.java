package com.keytech.modules.pi.vo;

import lombok.Data;

/**
 * @FileName TableInfoVO
 * @Description 数据库表信息值对象
 * @Author HQ
 * @date 2025-08-26
 **/
@Data
public class TableInfoVo {

    /**
     * 源表名称（物理表名）
     */
    private String tableName;

    /**
     * 表备注/说明（逻辑表名）
     */
    private String tableComment;

    /**
     * 架构名
     */
    private String schemaName;

    /**
     * 创建时间
     */
    private String createDate;
}
