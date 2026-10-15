package com.keytech.modules.custom.report.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.custom.report.dto.SysUserDto;
import com.keytech.modules.custom.report.entity.CustomReportTemplateEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
* @Description: 针对表【mes3_custom_report_template(自定义报表模板配置)】的数据库操作Service
* @Author: sjw
* @CreateDate: 2024/12/03 14:49:00
*/
public interface ICustomReportTemplateService extends BaseService<CustomReportTemplateEntity> {

    List<SysUserDto> getUserAll();

    /**
     * 通过配置查询数据
     * @param params 参数
     * @return 结果
     */
    List<LinkedHashMap<String, Object>> queryByTemplateConfig(Map<String, String> params);
}
