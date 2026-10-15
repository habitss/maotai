package com.keytech.modules.project.management.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.project.management.entity.ProjectManagementAction;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.io.IOException;
import java.util.List;

/**
 * @Description: 项目行动计划
 * @Author: jeecg-boot
 * @Date:   2025-03-28
 * @Version: V1.0
 */
public interface IProjectManagementActionService extends BaseService<ProjectManagementAction> {
    /**
     * 获取行动数据
     * @param projectManagementAction
     */
    List<ProjectManagementAction> queryList(ProjectManagementAction projectManagementAction);
    /**
     * 保存项目行动计划
     * @param list
     */
    Result<String> saveAction(List<ProjectManagementAction> list);

    /**
     * 删除行动数据
     * @param id 行动主键
     * @param nodeId 节点主键
     */
    void deleteAction(String id, String nodeId);

    /**
     * 导入
     */
    Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException;

    void updateDataByJob();
}
