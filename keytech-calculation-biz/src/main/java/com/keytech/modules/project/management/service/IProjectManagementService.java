package com.keytech.modules.project.management.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.project.management.dto.ProjectManagementSummaryDto;
import com.keytech.modules.project.management.entity.ProjectManagement;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @Description: 项目管理
 * @Author: jeecg-boot
 * @Date:   2025-03-28
 * @Version: V1.0
 */
public interface IProjectManagementService extends BaseService<ProjectManagement> {
    /**
     * 根据条件查询所有数据
     * @param projectManagement
     * @param req
     * @return
     */
    List<ProjectManagement> queryAll(ProjectManagement projectManagement, HttpServletRequest req);

    /**
     * 批量删除项目
     * @param projectIds 项目id
     * @return
     */
    void removeProjectByIds(String projectIds);

    /**
     * 项目个人激励-部门
     * @param projectManagement
     * @return
     */
    List<ProjectManagementSummaryDto> getDepartmentalProjectSummary(ProjectManagement projectManagement);

    /**
     * 项目个人激励-个人
     * @param projectManagement
     * @return
     */
    List<ProjectManagementSummaryDto> getPersonalProjectSummary(ProjectManagement projectManagement);
}
