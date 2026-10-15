package com.keytech.modules.custom.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.custom.report.entity.CustomReport;

import java.util.List;
import java.util.Map;

/**
 * @Description: 自定义报表主表
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
public interface CustomReportMapper extends BaseMapper<CustomReport> {
    Integer getTableTotal();

    void createNewTable(CustomReport customReport);

    List<Map<String, Object>> getTableData(String field, String table, String condition);

    void tableSave(String fieldStr, String fieldValStr, String table);

    void tableUpdate(String fieldValStr, String table, String condition);
}
