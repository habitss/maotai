package com.keytech.modules.line.efficiency.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.line.efficiency.entity.LineEfficiencyEhsRecords;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyEhsRecordsService;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * LineEfficiencyEhsRecordsController 单元测试类
 */
class LineEfficiencyEhsRecordsControllerTest {

    @InjectMocks
    private LineEfficiencyEhsRecordsController controller;

    @Mock
    private ILineEfficiencyEhsRecordsService lineEfficiencyEhsRecordsService;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试正常分页查询流程
     */
    @Test
    void testQueryPageList_Normal() {
        // 构造输入参数
        LineEfficiencyEhsRecords record = new LineEfficiencyEhsRecords();
        Integer pageNo = 1;
        Integer pageSize = 10;

        // 构造模拟请求参数
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("monitorValue", new String[]{"100"});
        when(request.getParameterMap()).thenReturn(parameterMap);

        // 构造 QueryWrapper（这里假设 QueryGenerator.initQueryWrapper 会正确构建）
        QueryWrapper<LineEfficiencyEhsRecords> queryWrapper = new QueryWrapper<>();

        // 构造 Page 对象
        Page<LineEfficiencyEhsRecords> page = new Page<>(pageNo, pageSize);

        // 构造 IPage 返回值
        IPage<LineEfficiencyEhsRecords> pageResult = mock(IPage.class);
        when(pageResult.getRecords()).thenReturn(java.util.Collections.singletonList(record));
        when(pageResult.getTotal()).thenReturn(1L);

        // 模拟 service 调用
        when(lineEfficiencyEhsRecordsService.page(page, queryWrapper)).thenReturn((Page<LineEfficiencyEhsRecords>) pageResult);

        // 调用被测方法
        Result<IPage<LineEfficiencyEhsRecords>> result = controller.queryPageList(
                record, pageNo, pageSize, null, null, null, request);

        // 断言
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getResult().getTotal());
        verify(lineEfficiencyEhsRecordsService, times(1)).page(page, queryWrapper);
    }

    /**
     * 测试默认分页参数
     */
    @Test
    void testQueryPageList_DefaultParams() {
        LineEfficiencyEhsRecords record = new LineEfficiencyEhsRecords();

        when(request.getParameterMap()).thenReturn(new HashMap<>());

        Page<LineEfficiencyEhsRecords> expectedPage = new Page<>(1, 10);
        QueryWrapper<LineEfficiencyEhsRecords> expectedWrapper = new QueryWrapper<>();

        IPage<LineEfficiencyEhsRecords> mockPage = mock(IPage.class);
        when(mockPage.getRecords()).thenReturn(java.util.Collections.emptyList());
        when(mockPage.getTotal()).thenReturn(0L);

        when(lineEfficiencyEhsRecordsService.page(expectedPage, expectedWrapper)).thenReturn((Page<LineEfficiencyEhsRecords>) mockPage);

        Result<IPage<LineEfficiencyEhsRecords>> result = controller.queryPageList(
                record, null, null, null, null, null, request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getResult().getTotal());
    }

    /**
     * 测试空查询结果
     */
    @Test
    void testQueryPageList_EmptyResult() {
        LineEfficiencyEhsRecords record = new LineEfficiencyEhsRecords();

        when(request.getParameterMap()).thenReturn(new HashMap<>());

        Page<LineEfficiencyEhsRecords> page = new Page<>(1, 10);
        QueryWrapper<LineEfficiencyEhsRecords> wrapper = new QueryWrapper<>();

        IPage<LineEfficiencyEhsRecords> emptyPage = mock(IPage.class);
        when(emptyPage.getRecords()).thenReturn(java.util.Collections.emptyList());
        when(emptyPage.getTotal()).thenReturn(0L);

        when(lineEfficiencyEhsRecordsService.page(page, wrapper)).thenReturn((Page<LineEfficiencyEhsRecords>) emptyPage);

        Result<IPage<LineEfficiencyEhsRecords>> result = controller.queryPageList(
                record, 1, 10, null, null, null, request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getResult().getTotal());
    }
}
