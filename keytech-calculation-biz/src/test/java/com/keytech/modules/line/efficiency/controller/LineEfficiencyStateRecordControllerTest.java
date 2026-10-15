package com.keytech.modules.line.efficiency.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.line.efficiency.entity.LineEfficiencyStateRecord;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyStateRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 单元测试：LineEfficiencyStateRecordController.queryPageList()
 */
class LineEfficiencyStateRecordControllerTest {

    @InjectMocks
    private LineEfficiencyStateRecordController controller;

    @Mock
    private ILineEfficiencyStateRecordService lineEfficiencyStateRecordService;

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
    void testQueryPageList_NormalCase() {
        // 准备参数
        LineEfficiencyStateRecord record = new LineEfficiencyStateRecord();
        Integer pageNo = 1;
        Integer pageSize = 10;

        // 模拟 request.getParameterMap()
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("someField", new String[]{"someValue"});
        when(request.getParameterMap()).thenReturn(parameterMap);

        // 构造分页结果
        IPage<LineEfficiencyStateRecord> pageResult = new Page<>();
        pageResult.setRecords(java.util.Collections.singletonList(new LineEfficiencyStateRecord()));
        pageResult.setTotal(1L);

        // 模拟 service.page(...)
        when(lineEfficiencyStateRecordService.page(any(Page.class), any(QueryWrapper.class)))
                .thenReturn((Page) pageResult);

        // 调用被测方法
        Result<IPage<LineEfficiencyStateRecord>> result = controller.queryPageList(
                record, pageNo, pageSize, null, null, null, request);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getResult().getRecords().size());
    }

    /**
     * 测试空查询条件
     */
    @Test
    void testQueryPageList_EmptyQueryCondition() {
        LineEfficiencyStateRecord record = null; // 表示无查询条件
        Integer pageNo = 1;
        Integer pageSize = 10;

        // 模拟 request.getParameterMap()
        when(request.getParameterMap()).thenReturn(new HashMap<>());

        // 构造分页结果
        IPage<LineEfficiencyStateRecord> pageResult = new Page<>();
        pageResult.setRecords(java.util.Collections.emptyList());
        pageResult.setTotal(0L);

        // 模拟 service.page(...)
        when(lineEfficiencyStateRecordService.page(any(Page.class), any(QueryWrapper.class)))
                .thenReturn((Page) pageResult);

        // 调用被测方法
        Result<IPage<LineEfficiencyStateRecord>> result = controller.queryPageList(
                record, pageNo, pageSize, null, null, null, request);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getResult().getRecords().size());
    }

    /**
     * 测试自定义分页参数
     */
    @Test
    void testQueryPageList_CustomPageSize() {
        LineEfficiencyStateRecord record = new LineEfficiencyStateRecord();
        Integer pageNo = 3;
        Integer pageSize = 5;

        // 模拟 request.getParameterMap()
        when(request.getParameterMap()).thenReturn(new HashMap<>());

        // 构造分页结果
        IPage<LineEfficiencyStateRecord> pageResult = new Page<>();
        pageResult.setCurrent(pageNo);
        pageResult.setSize(pageSize);

        // 模拟 service.page(...)
        when(lineEfficiencyStateRecordService.page(any(Page.class), any(QueryWrapper.class)))
                .thenReturn((Page) pageResult);

        // 调用被测方法
        Result<IPage<LineEfficiencyStateRecord>> result = controller.queryPageList(
                record, pageNo, pageSize, null, null, null, request);

        // 验证分页参数是否正确应用
        assertNotNull(result);
        assertEquals(Optional.of(pageNo), result.getResult().getCurrent());
        assertEquals(Optional.of(pageSize), result.getResult().getSize());
    }

    /**
     * 测试 service 层抛出异常的情况
     */
    @Test
    void testQueryPageList_ServiceThrowsException() {
        LineEfficiencyStateRecord record = new LineEfficiencyStateRecord();

        when(request.getParameterMap()).thenReturn(new HashMap<>());
        doThrow(new RuntimeException("Database error"))
                .when(lineEfficiencyStateRecordService)
                .page(any(Page.class), any(QueryWrapper.class));

        // 调用并验证异常处理（假设 controller 未捕获异常）
        assertThrows(RuntimeException.class, () -> {
            controller.queryPageList(record, 1, 10, null, null, null, request);
        });
    }
}
