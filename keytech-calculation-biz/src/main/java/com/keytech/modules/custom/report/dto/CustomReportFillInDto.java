package com.keytech.modules.custom.report.dto;

import lombok.Data;

/**
 * 自定义报表填报
 */
@Data
public class CustomReportFillInDto {
    //模板id
    private String templateId;
    //记录id
    private String recordId;
    //插入数据表名
    private String tableName;
    //填报项List(json)
    private String fillInList;
}
