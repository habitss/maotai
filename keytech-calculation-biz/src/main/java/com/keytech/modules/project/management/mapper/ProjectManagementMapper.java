package com.keytech.modules.project.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.project.management.dto.ProjectManagementSummaryDto;
import com.keytech.modules.project.management.entity.ProjectManagement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: 项目管理
 * @Author: jeecg-boot
 * @Date:   2025-03-28
 * @Version: V1.0
 */
@Mapper
public interface ProjectManagementMapper extends BaseMapper<ProjectManagement> {
    /**
     * 查询项目列表
     * @param projectManagement
     * @return
     */
    List<ProjectManagement> queryAll(ProjectManagement projectManagement);

    /**
     * 项目个人激励-部门
     * @param projectManagement
     * @return
     */
    List<ProjectManagementSummaryDto> getDepartmentalProjectSummary(ProjectManagement projectManagement);

    /**
     * 项目个人激励-个人
     * @param projectIds
     * @return
     */
    List<ProjectManagementSummaryDto> getPersonalProjectSummary(@Param("projectIds") String projectIds, @Param("userId") String userId, @Param("year") String year, @Param("monthList") List<String> monthList, @Param("isFinishNode") String isFinishNode);
}
