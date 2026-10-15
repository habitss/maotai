package com.keytech.modules.custom.report.mapper;

import java.util.Map;

/**
 * @Description: 自定义报表填报
 * @Author: sjw
 * @Date: 2024/12/9 17:42
 */
public interface CustomReportFillInMapper {

    /**
     * @Description:新增填报
     * @param tableName 表名
     * @param params Sql所需数据，key为列名，value为对应值
     */
    void addData(String tableName, Map<String, Object> params);

    /**
     * @Description:修改填报
     * @param tableName 表名
     * @param id 修改数据的主键
     * @param params Sql所需数据，key为列名，value为对应值
     */
    void updateData(String tableName, String id, Map<String, Object> params);

    /**
     * @Description:删除填报
     * @param tableName 表名
     * @param id 修改数据的主键
     */
    void removeById(String tableName, String id);
}
