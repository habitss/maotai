package com.keytech.modules.project.management.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.GlobalErrorEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.keytech.modules.project.management.entity.ProjectManagementAction;
import com.keytech.modules.project.management.service.IProjectManagementActionService;

/**
 * --bug=1025033 --user=孙金伟 燕京-中京-项目管理-要想关闭一条项目节点行动，太慢了，几乎没反应。 https://www.tapd.cn/63554897/s/1379590 单元测试类
 */
class ProjectManagementActionControllerTest {

    @InjectMocks
    private ProjectManagementActionController projectManagementActionController;

    @Mock
    private IProjectManagementActionService projectManagementActionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试正常情况下的 add 方法调用
     */
    @Test
    void testAdd_withValidList_returnsOkResult() {
        // Arrange
        List<ProjectManagementAction> actionList = List.of(new ProjectManagementAction());

        doNothing().when(projectManagementActionService).saveAction(actionList);

        // Act
        Result<String> result = projectManagementActionController.add(actionList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getResult());
        verify(projectManagementActionService, times(1)).saveAction(actionList);
    }

    /**
     * 测试传入 null 参数时的行为
     */
    @Test
    void testAdd_withNullList_returnsOkResult() {
        // Arrange
        doNothing().when(projectManagementActionService).saveAction(null);

        // Act
        Result<String> result = projectManagementActionController.add(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getResult());
        verify(projectManagementActionService, times(1)).saveAction(null);
    }
}
