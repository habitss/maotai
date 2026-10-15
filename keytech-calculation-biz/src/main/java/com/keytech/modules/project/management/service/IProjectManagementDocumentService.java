package com.keytech.modules.project.management.service;

import cn.hutool.json.JSONObject;
import com.keytech.base.service.BaseService;
import com.keytech.modules.project.management.dto.ProjectManagementDocumentDto;
import com.keytech.modules.project.management.entity.ProjectManagement;
import com.keytech.modules.project.management.entity.ProjectManagementDocument;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

/**
 * @Description: 项目清单
 * @Author: jeecg-boot
 * @Date:   2025-03-28
 * @Version: V1.0
 */
public interface IProjectManagementDocumentService extends BaseService<ProjectManagementDocument> {
    /**
     * 查询项目清单数据
     * @param projectManagement
     * @param req
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    List<JSONObject> queryAllList(ProjectManagement projectManagement, HttpServletRequest req) throws NoSuchFieldException, IllegalAccessException;

    /**
     * 保存或修改清单数据
     * @param documentDto
     */
    void saveOrUpdateData(ProjectManagementDocumentDto documentDto);

    /**
     * 自定义导出
     */
    ModelAndView exportXlsBy(HttpServletRequest request, ProjectManagement projectManagement) throws NoSuchFieldException, IllegalAccessException;
}
