package com.keytech.modules.action.plan.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * --story=1055295 --user=孙金伟 19-行动跟踪页面编号查询调整为范围查询 https://www.tapd.cn/63554897/s/1380224 单元测试
 */
class ActionPlanTrackControllerTest {

    @InjectMocks
    private ActionPlanTrackController actionPlanTrackController;

    @Mock
    private IActionPlanTrackService actionPlanTrackService;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * --story=1055313 --user=孙金伟 23-行动计划优化处理 https://www.tapd.cn/63554897/s/1382848 单元测试
     */
    @Test
    void testQueryPageList_Basic() {
        // 构造测试数据
        ActionPlanTrack actionPlanTrack = new ActionPlanTrack();
        
        // 模拟request参数
        Map<String, String[]> parameterMap = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameterMap);

        // 模拟service返回结果
        Page<ActionPlanTrack> page = new Page<>(1, 10);
        when(actionPlanTrackService.page(page, any(QueryWrapper.class))).thenReturn(page);

        // 执行测试
        Result<IPage<ActionPlanTrack>> result = actionPlanTrackController.queryPageList(
                actionPlanTrack, 1, 10, null, null, null, request);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getResult().getCurrent());
        verify(actionPlanTrackService, times(1)).page(page, any(QueryWrapper.class));
    }

    /**
     * 测试surplusDay字段处理逻辑
     */
    @Test
    void testQueryPageList_WithSurplusDay() {
        // 构造测试数据
        ActionPlanTrack actionPlanTrack = new ActionPlanTrack();
        actionPlanTrack.setSurplusDay("30");
        
        // 模拟request参数
        Map<String, String[]> parameterMap = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameterMap);

        // 模拟service返回结果
        Page<ActionPlanTrack> page = new Page<>(1, 10);
        when(actionPlanTrackService.page(page, any(QueryWrapper.class))).thenReturn(page);

        // 执行测试
        Result<IPage<ActionPlanTrack>> result = actionPlanTrackController.queryPageList(
                actionPlanTrack, 1, 10, null, null, null, request);

        // 验证结果
        assertNotNull(result);
        assertEquals("le 30", actionPlanTrack.getSurplusDay());
        verify(actionPlanTrackService, times(1)).page(page, any(QueryWrapper.class));
    }

    /**
     * 测试部门搜索功能
     */
    @Test
    void testQueryPageList_WithDeptIds() {
        // 构造测试数据
        ActionPlanTrack actionPlanTrack = new ActionPlanTrack();
        actionPlanTrack.setDeptIds("dept1,dept2");
        
        // 模拟request参数
        Map<String, String[]> parameterMap = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameterMap);

        // 模拟service返回结果
        Page<ActionPlanTrack> page = new Page<>(1, 10);
        when(actionPlanTrackService.page(page, any(QueryWrapper.class))).thenReturn(page);

        // 执行测试
        Result<IPage<ActionPlanTrack>> result = actionPlanTrackController.queryPageList(
                actionPlanTrack, 1, 10, null, null, null, request);

        // 验证查询条件
        assertNotNull(result);
        verify(actionPlanTrackService, times(1)).page(page, any(QueryWrapper.class));
    }

    /**
     * 测试用户搜索功能
     */
    @Test
    void testQueryPageList_WithUserIds() {
        // 构造测试数据
        ActionPlanTrack actionPlanTrack = new ActionPlanTrack();
        actionPlanTrack.setUserIds("user1,user2");
        
        // 模拟request参数
        Map<String, String[]> parameterMap = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameterMap);

        // 模拟service返回结果
        Page<ActionPlanTrack> page = new Page<>(1, 10);
        when(actionPlanTrackService.page(page, any(QueryWrapper.class))).thenReturn(page);

        // 执行测试
        Result<IPage<ActionPlanTrack>> result = actionPlanTrackController.queryPageList(
                actionPlanTrack, 1, 10, null, null, null, request);

        // 验证查询条件
        assertNotNull(result);
        verify(actionPlanTrackService, times(1)).page(page, any(QueryWrapper.class));
    }

    /**
     * 测试服务调用失败的情况
     */
    @Test
    void testQueryPageList_ServiceFailure() {
        // 构造测试数据
        ActionPlanTrack actionPlanTrack = new ActionPlanTrack();
        
        // 模拟request参数
        Map<String, String[]> parameterMap = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameterMap);

        // 模拟service抛出异常
        when(actionPlanTrackService.page(any(Page.class), any(QueryWrapper.class)))
                .thenThrow(new RuntimeException("Database error"));

        // 执行测试
        Result<IPage<ActionPlanTrack>> result = actionPlanTrackController.queryPageList(
                actionPlanTrack, 1, 10, null, null, null, request);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("系统异常，请联系管理员！", result.getMessage());
    }
}
