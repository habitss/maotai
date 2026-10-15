package com.keytech.modules.custom.report.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.custom.report.entity.CustomReport;
import com.keytech.modules.pi.dto.TableQueryDto;
import com.keytech.modules.pi.dto.TableSaveDto;
import org.jeecg.common.api.vo.Result;

import java.util.List;
import java.util.Map;

/**
 * @Description: 自定义报表主表
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */

public interface ICustomReportService extends BaseService<CustomReport> {
    /**
     * 存储自定义表
     * @param customReport
     * @return
     */
    Result<String> saveTable(CustomReport customReport);

    /**
     * 表查询,查询指定表,指定字段,指定义条件
     * @param tableQueryDto
     * @return
     */
    List<Map<String,Object>> getTableData(TableQueryDto tableQueryDto);

    /**
     * 数据存储, 指定表,指定字段,指定义条件
     * @param tableSaveDto
     * @return
     */
    String tableSave(TableSaveDto tableSaveDto);

    /**
     * 获取报表集合
     * @param customReport
     * @return
     */
    List<CustomReport> getTableList(CustomReport customReport);
}
