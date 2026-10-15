package com.keytech.modules.project.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.project.management.entity.ProjectManagement;
import com.keytech.modules.project.management.entity.ProjectManagementAction;
import com.keytech.modules.project.management.entity.ProjectManagementActionSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: 项目行动计划
 * @Author: jeecg-boot
 * @Date:   2025-03-28
 * @Version: V1.0
 */
@Mapper
public interface ProjectManagementActionMapper extends BaseMapper<ProjectManagementAction> {
    /**
     * 获取行动数据
     * @param projectManagementAction 参数
     */
    List<ProjectManagementAction> queryList(ProjectManagementAction projectManagementAction);

    /**
     * 获取行动统计数据
     * @param action 参数
     */
    List<ProjectManagementActionSummary> querySummaryData(ProjectManagement action);

    /**
     * 获取行动周计划统计数据
     * @param year 年份
     * @param months 月份
     * @param column 查询字段
     * @return 统计值集合
     */
    List<ProjectManagementActionSummary> queryWeekSummaryData(@Param("year") String year, @Param("months") List<String> months, @Param("column") String column);

    /**
     * 获取行动周计划统计数据
     * @param year 年份
     * @param column 查询字段
     * @return 统计值集合
     */
    List<ProjectManagementActionSummary> queryMonthSummaryData(@Param("year") String year, @Param("column") String column);
}
