package com.keytech.modules.custom.report.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.custom.report.entity.CustomReportField;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Description: 自定义报表字段
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */
public interface ICustomReportFieldService extends  BaseService<CustomReportField> {

    Result<String> saveField(CustomReportField customReportField);

    /**
     * 根据模板id获取表头
     * @param templateId 模版id
     * @return
     */
    List<CustomReportField> queryHeaderByTemplateId(String templateId);

    /**
     * 根据模板,表名获取对应数据
     * @param pageNo
     * @param pageSize
     * @param orderField
     * @param orderType
     * @param templateId
     * @param req
     * @return
     */
    Map<String, Object> queryData(Integer pageNo, Integer pageSize, String orderField, String orderType,
                                  String templateId, HttpServletRequest req);

    /**
     * 获取最大排序
     * @param tableId
     * @return
     */
    Double getMaxSort(String tableId);

    /**
     * 导入数据
     * @return
     */
    Result<?> importExcel(HttpServletRequest request, HttpServletResponse response,
                       Class<CustomReportField> customReportFieldClass) throws IOException;
}
