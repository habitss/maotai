package com.keytech.modules.custom.report.service;

import com.keytech.modules.custom.report.dto.CustomReportFillInDto;

/**
 * @Description: 自定义报表填报
 * @Author: sjw
 * @Date: 2024/12/9 17:42
 */
public interface ICustomReportFillInService {
    /**
     * @Description:新增填报
     */
    void addData(CustomReportFillInDto customReportFillInDto);

    /**
     * @Description:修改填报
     */
    void updateData(CustomReportFillInDto customReportFillInDto);

    /**
     * @Description:删除填报
     */
    void removeById(CustomReportFillInDto customReportFillInDto);
}
