package com.keytech.modules.pi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.pi.entity.PiExtractTableField;
import com.keytech.modules.pi.service.IPiExtractTableFieldService;
import com.keytech.modules.pi.vo.ColumnInfoVo;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 *
 * @description PiExtractTableFieldController 单元测试
 */
@ExtendWith(MockitoExtension.class)
public class PiExtractTableFieldControllerTest {

    @InjectMocks
    private PiExtractTableFieldController piExtractTableFieldController;

    @Mock
    private IPiExtractTableFieldService piExtractTableFieldService;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    public void setUp() {
        // 初始化 mocks
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试用例1：正常分页查询，返回非空结果
     */
    @Test
    public void testQueryPageList_NormalCase() {
        // 准备测试数据
        PiExtractTableField piExtractTableField = new PiExtractTableField();
        Integer pageNo = 1;
        Integer pageSize = 10;
        String orderField = null;
        String orderType = null;
        String selectFields = null;

        // Mock HttpServletRequest 参数
        when(request.getParameterMap()).thenReturn(new java.util.HashMap<>());

        // 创建分页结果
        Page<PiExtractTableField> page = new Page<>(pageNo, pageSize);
        List<PiExtractTableField> records = new ArrayList<>();
        PiExtractTableField record = new PiExtractTableField();
        record.setId("1");
        record.setSourceField("test_field");
        records.add(record);
        page.setRecords(records);
        page.setTotal(1L);

        // Mock service 的 page 方法
        try (MockedStatic<QueryGenerator> mockedQueryGenerator = mockStatic(QueryGenerator.class)) {
            mockedQueryGenerator.when(() -> QueryGenerator.initQueryWrapper(any(), any(Map.class)))
                    .thenReturn(new QueryWrapper<>());
            
            when(piExtractTableFieldService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

            // 执行测试
            Result<IPage<PiExtractTableField>> result = piExtractTableFieldController.queryPageList(
                    piExtractTableField, pageNo, pageSize, orderField, orderType, selectFields, request);

            // 验证结果
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNotNull(result.getResult());
            assertEquals(1, result.getResult().getTotal());
            assertEquals(1, result.getResult().getRecords().size());
            assertEquals("test_field", result.getResult().getRecords().get(0).getSourceField());
        }
    }

    /**
     * 测试用例2：查询结果为空，sourceTable 为空，返回空结果
     */
    @Test
    public void testQueryPageList_EmptyResultWithoutSourceTable() {
        // 准备测试数据
        PiExtractTableField piExtractTableField = new PiExtractTableField();
        Integer pageNo = 1;
        Integer pageSize = 10;
        String orderField = null;
        String orderType = null;
        String selectFields = null;

        // Mock HttpServletRequest 参数
        when(request.getParameterMap()).thenReturn(new java.util.HashMap<>());

        // 创建空的分页结果
        Page<PiExtractTableField> page = new Page<>(pageNo, pageSize);
        page.setRecords(new ArrayList<>());
        page.setTotal(0L);

        // Mock service 的 page 方法
        try (MockedStatic<QueryGenerator> mockedQueryGenerator = mockStatic(QueryGenerator.class)) {
            mockedQueryGenerator.when(() -> QueryGenerator.initQueryWrapper(any(), any(Map.class)))
                    .thenReturn(new QueryWrapper<>());
            
            when(piExtractTableFieldService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

            // 执行测试
            Result<IPage<PiExtractTableField>> result = piExtractTableFieldController.queryPageList(
                    piExtractTableField, pageNo, pageSize, orderField, orderType, selectFields, request);

            // 验证结果
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNotNull(result.getResult());
            assertEquals(0, result.getResult().getTotal());
            assertEquals(0, result.getResult().getRecords().size());
        }
    }

    /**
     * 测试用例3：查询结果为空，sourceTable 不为空，调用 getTableField 并返回转换后的数据
     */
    @Test
    public void testQueryPageList_EmptyResultWithSourceTable() {
        // 准备测试数据
        PiExtractTableField piExtractTableField = new PiExtractTableField();
        piExtractTableField.setSourceTable("test_table");
        Integer pageNo = 1;
        Integer pageSize = 10;
        String orderField = null;
        String orderType = null;
        String selectFields = null;

        // Mock HttpServletRequest 参数
        when(request.getParameterMap()).thenReturn(new java.util.HashMap<>());

        // 创建空的分页结果
        Page<PiExtractTableField> page = new Page<>(pageNo, pageSize);
        page.setRecords(new ArrayList<>());
        page.setTotal(0L);

        // 准备字段信息
        List<ColumnInfoVo> fieldList = new ArrayList<>();
        ColumnInfoVo columnInfoVO = new ColumnInfoVo();
        columnInfoVO.setColumnName("column1");
        columnInfoVO.setColumnComment("测试列1");
        fieldList.add(columnInfoVO);

        // Mock service 方法
        try (MockedStatic<QueryGenerator> mockedQueryGenerator = mockStatic(QueryGenerator.class)) {
            mockedQueryGenerator.when(() -> QueryGenerator.initQueryWrapper(any(), any(Map.class)))
                    .thenReturn(new QueryWrapper<>());
            
            when(piExtractTableFieldService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(page);
            when(piExtractTableFieldService.getTableField("test_table")).thenReturn(fieldList);

            // 执行测试
            Result<IPage<PiExtractTableField>> result = piExtractTableFieldController.queryPageList(
                    piExtractTableField, pageNo, pageSize, orderField, orderType, selectFields, request);

            // 验证结果
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNotNull(result.getResult());
            assertEquals(0, result.getResult().getTotal()); // 总数仍为0
            assertEquals(1, result.getResult().getRecords().size()); // 但记录列表被替换
            assertEquals("column1", result.getResult().getRecords().get(0).getSourceField());
            assertEquals("测试列1", result.getResult().getRecords().get(0).getSourceFieldName());

            // 验证方法调用
            verify(piExtractTableFieldService, times(1)).page(any(Page.class), any(QueryWrapper.class));
            verify(piExtractTableFieldService, times(1)).getTableField("test_table");
        }
    }
}
