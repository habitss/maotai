package com.keytech.modules.custom.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.custom.report.entity.CustomReportField;

import java.util.List;
import java.util.Map;

/**
 * @Description: 自定义报表字段
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
public interface CustomReportFieldMapper extends BaseMapper<CustomReportField> {

    void addTableField(CustomReportField customReportField);

    List<CustomReportField> getFieldByTableId(CustomReportField customReportField);

    Double getMaxSort(String tableId);

    int getDynamicDataTotal(Map<String, Object> paramMap);

    List<Map<String, Object>> getDynamicDataPage(Map<String, Object> paramMap);

}
