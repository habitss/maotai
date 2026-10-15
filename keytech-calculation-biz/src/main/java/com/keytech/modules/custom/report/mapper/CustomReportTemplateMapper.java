package com.keytech.modules.custom.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.custom.report.dto.SysUserDto;
import com.keytech.modules.custom.report.entity.CustomReportTemplateEntity;

import java.util.LinkedHashMap;
import java.util.List;

/**
* @Description: 针对表【mes3_custom_report_template(自定义报表模板配置)】的数据库操作Mapper
* @Author: sjw
* @CreateDate: 2024/12/03 14:49:00
* @Entity: generator.domain.Mes3CustomReportTemplate
*/
public interface CustomReportTemplateMapper extends BaseMapper<CustomReportTemplateEntity> {
    List<SysUserDto> getUserAll();

    List<LinkedHashMap<String, Object>> queryDataBySql(String sql);

    List<Double> queryDoubleBySql(String sql);
}




