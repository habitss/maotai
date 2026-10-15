package com.keytech.modules.plan.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.plan.entity.PlanPause;
import com.keytech.modules.plan.service.IPlanPauseService;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * --story=1054964 --user=孙金伟 15-一分工厂需求讨论及问题梳理 https://www.tapd.cn/63554897/s/1379930 单元测试
 */
class PlanPauseControllerTest {

    private PlanPauseController planPauseController;

    private IPlanPauseService planPauseServiceMock;

    private HttpServletRequest requestMock;

    /**
     * 初始化测试环境
     */
    @BeforeEach
    void setUp() {
        planPauseController = new PlanPauseController();
        planPauseServiceMock = mock(IPlanPauseService.class);
        requestMock = new MockHttpServletRequest();
    }

    /**
     * 测试正常分页查询
     */
    @Test
    void testQueryPageList_Success() {
        // 准备测试数据
        PlanPause planPause = new PlanPause();
        Integer pageNo = 1;
        Integer pageSize = 10;

        // 模拟 QueryWrapper
        QueryWrapper<PlanPause> queryWrapper = new QueryWrapper<>();
        try (MockedStatic<QueryGenerator> mockedStatic = mockStatic(QueryGenerator.class)) {
            mockedStatic.when(() -> QueryGenerator.initQueryWrapper(planPause, requestMock.getParameterMap()))
                    .thenReturn(queryWrapper);

            // 模拟分页结果
            IPage<PlanPause> pageResult = new Page<>();
            when(planPauseServiceMock.page(any(Page.class), eq(queryWrapper))).thenReturn((Page) pageResult);

            // 执行被测方法
            Result<IPage<PlanPause>> result = planPauseController.queryPageList(
                    planPause, pageNo, pageSize, null, null, null, requestMock);

            // 验证结果
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(pageResult, result.getResult());
        }
    }

    /**
     * 测试空请求参数
     */
    @Test
    void testQueryPageList_NullRequest() {
        // 准备测试数据
        PlanPause planPause = new PlanPause();
        Integer pageNo = 1;
        Integer pageSize = 10;

        // 执行被测方法
        Exception exception = assertThrows(NullPointerException.class, () ->
                planPauseController.queryPageList(planPause, pageNo, pageSize, null, null, null, null));

        // 验证异常
        assertNotNull(exception);
    }

    /**
     * 测试 QueryGenerator 初始化失败
     */
    @Test
    void testQueryPageList_QueryGeneratorException() {
        // 准备测试数据
        PlanPause planPause = new PlanPause();
        Integer pageNo = 1;
        Integer pageSize = 10;

        // 模拟 QueryGenerator 抛出异常
        try (MockedStatic<QueryGenerator> mockedStatic = mockStatic(QueryGenerator.class)) {
            mockedStatic.when(() -> QueryGenerator.initQueryWrapper(planPause, requestMock.getParameterMap()))
                    .thenThrow(new RuntimeException("QueryGenerator error"));

            // 执行被测方法
            Exception exception = assertThrows(RuntimeException.class, () ->
                    planPauseController.queryPageList(planPause, pageNo, pageSize, null, null, null, requestMock));

            // 验证异常
            assertNotNull(exception);
            assertEquals("QueryGenerator error", exception.getMessage());
        }
    }

    /**
     * 测试自定义参数映射
     */
    @Test
    void testQueryPageList_WithCustomParameters() {
        // 准备测试数据
        PlanPause planPause = new PlanPause();
        Integer pageNo = 2;
        Integer pageSize = 5;
        String orderField = "createTime";
        String orderType = "desc";
        String selectFields = "id,createBy";

        // 模拟请求参数
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("orderField", new String[]{orderField});
        parameterMap.put("orderType", new String[]{orderType});
        parameterMap.put("selectFields", new String[]{selectFields});
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameters(parameterMap);

        // 模拟 QueryWrapper
        QueryWrapper<PlanPause> queryWrapper = new QueryWrapper<>();
        try (MockedStatic<QueryGenerator> mockedStatic = mockStatic(QueryGenerator.class)) {
            mockedStatic.when(() -> QueryGenerator.initQueryWrapper(planPause, request.getParameterMap()))
                    .thenReturn(queryWrapper);

            // 模拟分页结果
            IPage<PlanPause> pageResult = new Page<>();
            when(planPauseServiceMock.page(any(Page.class), eq(queryWrapper))).thenReturn((Page) pageResult);

            // 执行被测方法
            Result<IPage<PlanPause>> result = planPauseController.queryPageList(
                    planPause, pageNo, pageSize, orderField, orderType, selectFields, request);

            // 验证结果
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(pageResult, result.getResult());
        }
    }
}
