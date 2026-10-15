package com.keytech.modules.pi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.BaseService;
import com.keytech.modules.pi.dto.*;
import com.keytech.modules.pi.entity.PiCustomizeTable;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.system.entity.SysUser;

import java.util.List;
import java.util.Map;

/**
 * @Description: pi自定义报表主表
 * @Author: jeecg-boot
 * @Date:   2024-11-13
 * @Version: V1.0
 */

public interface IPiCustomizeTableService extends BaseService<PiCustomizeTable> {
    /**
     * 存储自定义表
     * @param piCustomizeTable
     * @return
     */
    Result<String> saveTable(PiCustomizeTable piCustomizeTable);

    /**
     * 表查询,查询指定表,指定字段,指定条件
     * @param tableQueryDto
     * @return
     */
    List<Map<String,Object>> getTableData(TableQueryDto tableQueryDto);

    /**
     * 数据存储, 指定表,指定字段,指定约束条件
     * @param tableSaveDto
     * @return
     */
    String tableSave(TableSaveDto tableSaveDto);

    /**
     * 获取报表集合
     * @param piCustomizeTable
     * @return
     */
    List<PiCustomizeTable> getTableList(PiCustomizeTable piCustomizeTable);


    /**
     * 报表数据查询(自定义查询条件)
     * @param tableConditionQueryDto
     * table 表名,
     * fieldList 指定字段集合,为空查询所有(*)
     * condition 指定义条件(条件1=val1 and 条件2=val2)
     * @return
     */
    List<Map<String,Object>> getTableByCondition(TableConditionQueryDto tableConditionQueryDto);

    /**
     * 数据处理
     * @param list
     * @return
     */
    IPage<PiCustomizeTable> statisticsData(IPage<PiCustomizeTable> list);


    /**
     * PI点位上图查询
     * @param dto
     * @return
     */
    Map getPointChartData(PointChartDto dto);

    /**
     * PI点图查询
     * @param dto 查询参数
     * @return 结果集
     */
    Map<String, Object> getPointDiagramData(PointDiagramDto dto);

    /**
     * PI箱图查询
     * @param dto 查询参数
     * @return 结果集
     */
    Map<String, Object> getBoxDiagramData(PointDiagramDto dto);

    /**
     * 获取用户
     */
    SysUser getAdminUser();
}
