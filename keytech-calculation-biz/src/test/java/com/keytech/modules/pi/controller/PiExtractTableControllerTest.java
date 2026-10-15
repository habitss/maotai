package com.keytech.modules.pi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.pi.entity.PiExtractTable;
import com.keytech.modules.pi.service.IPiExtractTableService;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @description PiExtractTableController 单元测试
 */
@ExtendWith(MockitoExtension.class)
public class PiExtractTableControllerTest {

    @InjectMocks
    private PiExtractTableController piExtractTableController;

    @Mock
    private IPiExtractTableService piExtractTableService;

    @Mock
    private HttpServletRequest request;

    private PiExtractTable piExtractTable;
    private Integer pageNo;
    private Integer pageSize;
    private String orderField;
    private String orderType;
    private String selectFields;

    @BeforeEach
    public void setUp() {
        piExtractTable = new PiExtractTable();
        pageNo = 1;
        pageSize = 10;
        orderField = "id";
        orderType = "asc";
        selectFields = "id,sourceTable";
    }

    @Test
    public void testQueryPageList_Success() {
        // 准备数据
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("sourceTable", new String[]{"test_table"});

        IPage<PiExtractTable> mockPage = new Page<>();
        mockPage.setRecords(java.util.Collections.singletonList(piExtractTable));
        mockPage.setTotal(1L);

        // Mock HttpServletRequest
        when(request.getParameterMap()).thenReturn(parameterMap);

        // Mock QueryGenerator 静态方法
        try (MockedStatic<QueryGenerator> mockedQueryGenerator = mockStatic(QueryGenerator.class)) {
            QueryWrapper<PiExtractTable> queryWrapper = new QueryWrapper<>();
            mockedQueryGenerator.when(() -> QueryGenerator.initQueryWrapper(any(), anyMap())).thenReturn(queryWrapper);

            // Mock Service
            when(piExtractTableService.page(any(Page.class), any(QueryWrapper.class))).thenReturn((Page) mockPage);

            // 调用被测方法
            Result<IPage<PiExtractTable>> result = piExtractTableController.queryPageList(
                    piExtractTable, pageNo, pageSize, orderField, orderType, selectFields, request);

            // 断言
            assertTrue(result.isSuccess());
            assertNotNull(result.getResult());
            assertEquals(1, result.getResult().getRecords().size());
        }
    }

    @Test
    public void testQueryPageList_ServiceThrowsException() {
        // 准备数据
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("sourceTable", new String[]{"test_table"});

        // Mock HttpServletRequest
        when(request.getParameterMap()).thenReturn(parameterMap);

        // Mock QueryGenerator 静态方法
        try (MockedStatic<QueryGenerator> mockedQueryGenerator = mockStatic(QueryGenerator.class)) {
            QueryWrapper<PiExtractTable> queryWrapper = new QueryWrapper<>();
            mockedQueryGenerator.when(() -> QueryGenerator.initQueryWrapper(any(), anyMap())).thenReturn(queryWrapper);

            // Mock Service 抛异常
            when(piExtractTableService.page(any(Page.class), any(QueryWrapper.class))).thenThrow(new RuntimeException("数据库异常"));

            // 断言抛出异常
            assertThrows(RuntimeException.class, () -> {
                piExtractTableController.queryPageList(
                        piExtractTable, pageNo, pageSize, orderField, orderType, selectFields, request);
            });
        }
    }

    @Test
    public void testQueryPageList_EmptyParameters() {
        // 准备数据
        Map<String, String[]> parameterMap = new HashMap<>();

        IPage<PiExtractTable> mockPage = new Page<>();
        mockPage.setRecords(java.util.Collections.emptyList());
        mockPage.setTotal(0L);

        // Mock HttpServletRequest
        when(request.getParameterMap()).thenReturn(parameterMap);

        // Mock QueryGenerator 静态方法
        try (MockedStatic<QueryGenerator> mockedQueryGenerator = mockStatic(QueryGenerator.class)) {
            QueryWrapper<PiExtractTable> queryWrapper = new QueryWrapper<>();
            mockedQueryGenerator.when(() -> QueryGenerator.initQueryWrapper(any(), anyMap())).thenReturn(queryWrapper);

            // Mock Service
            when(piExtractTableService.page(any(Page.class), any(QueryWrapper.class))).thenReturn((Page) mockPage);

            // 调用被测方法
            Result<IPage<PiExtractTable>> result = piExtractTableController.queryPageList(
                    null, pageNo, pageSize, null, null, null, request);

            // 断言
            assertTrue(result.isSuccess());
            assertNotNull(result.getResult());
            assertEquals(0, result.getResult().getRecords().size());
        }
    }
}
