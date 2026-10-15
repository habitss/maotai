package com.keytech.modules.project.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.modules.project.management.entity.ProjectManagement;
import com.keytech.modules.project.management.service.IProjectManagementService;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * --story=1055310 --user=孙金伟 23-项目管理优化处理 https://www.tapd.cn/63554897/s/1381025 单元测试
 */
class ProjectManagementControllerTest {

    @InjectMocks
    private ProjectManagementController projectManagementController;

    @Mock
    private IProjectManagementService projectManagementService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * TC01: 正常输入，service返回非空数据
     */
    @Test
    void testQueryPageList_withValidData_returnsSuccessResult() {
        // Arrange
        ProjectManagement projectManagement = new ProjectManagement();
        List<ProjectManagement> dataList = new ArrayList<>();
        dataList.add(new ProjectManagement().setProjectName("项目A"));

        when(projectManagementService.queryAll(projectManagement, httpServletRequest)).thenReturn(dataList);

        // Act
        Result<IPage<ProjectManagement>> result = projectManagementController.queryPageList(
                projectManagement, 1, 10, null, null, null, httpServletRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertNotNull(result.getResult());
        assertEquals(1, result.getResult().getRecords().size());
        assertEquals("项目A", result.getResult().getRecords().get(0).getProjectName());

        verify(projectManagementService, times(1)).queryAll(projectManagement, httpServletRequest);
    }

    /**
     * TC02: service返回空数据
     */
    @Test
    void testQueryPageList_withEmptyData_returnsEmptyResult() {
        // Arrange
        ProjectManagement projectManagement = new ProjectManagement();

        when(projectManagementService.queryAll(projectManagement, httpServletRequest)).thenReturn(new ArrayList<>());

        // Act
        Result<IPage<ProjectManagement>> result = projectManagementController.queryPageList(
                projectManagement, 1, 10, null, null, null, httpServletRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertNotNull(result.getResult());
        assertTrue(result.getResult().getRecords().isEmpty());

        verify(projectManagementService, times(1)).queryAll(projectManagement, httpServletRequest);
    }

    /**
     * TC03: 参数为null或默认值，仍能构造分页对象
     */
    @Test
    void testQueryPageList_withDefaultParams_returnsSuccessResult() {
        // Arrange
        ProjectManagement projectManagement = null; // 允许为 null

        List<ProjectManagement> dataList = new ArrayList<>();
        dataList.add(new ProjectManagement().setProjectName("项目B"));

        when(projectManagementService.queryAll(null, httpServletRequest)).thenReturn(dataList);

        // Act
        Result<IPage<ProjectManagement>> result = projectManagementController.queryPageList(
                projectManagement, 1, 10, null, null, null, httpServletRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertNotNull(result.getResult());
        assertEquals(1, result.getResult().getRecords().size());
        assertEquals("项目B", result.getResult().getRecords().get(0).getProjectName());

        verify(projectManagementService, times(1)).queryAll(null, httpServletRequest);
    }
}
