package com.keytech.modules.pi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.pi.entity.PiExtractTableField;
import com.keytech.modules.pi.entity.PiSistarRtStepParams;
import com.keytech.modules.pi.service.IPiSistarRtStepParamsService;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 单元测试类：PiSistarRtStepParamsController.queryPageList
 */
class PiSistarRtStepParamsControllerTest {

    @InjectMocks
    private PiSistarRtStepParamsController controller; // 被测控制器

    @Mock
    private IPiSistarRtStepParamsService piSistarRtStepParamsService; // 模拟服务层

    @Mock
    private HttpServletRequest request; // 模拟HTTP请求

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // 初始化Mockito注解
    }

    /**
     * 测试正常分页查询
     */
    @Test
    void testQueryPageList_Normal() {
        // 准备测试数据
        PiSistarRtStepParams params = new PiSistarRtStepParams();
        params.setParamName("testParam");

        Integer pageNo = 1;
        Integer pageSize = 10;
        String orderField = "id";
        String orderType = "asc";
        String selectFields = "id,paramName";

        // 模拟请求参数
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("paramName", new String[]{"testParam"});
        when(request.getParameterMap()).thenReturn(parameterMap);

        // 构造分页结果
        Page<PiExtractTableField> page = new Page<>(pageNo, pageSize);
        List<PiExtractTableField> records = new ArrayList<>();
        PiExtractTableField record = new PiExtractTableField();
        record.setId("1");
        record.setSourceField("test_field");
        records.add(record);
        page.setRecords(records);
        page.setTotal(1L);

        // 模拟服务层返回
        when(piSistarRtStepParamsService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        // 调用被测方法
        Result<IPage<PiSistarRtStepParams>> result = controller.queryPageList(
                params, pageNo, pageSize, orderField, orderType, selectFields, request);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertNotNull(result.getResult());
        assertEquals(1, result.getResult().getTotal());
        assertEquals("testParam", result.getResult().getRecords().get(0).getParamName());
    }

    /**
     * 测试查询条件为空
     */
    @Test
    void testQueryPageList_EmptyParams() {
        PiSistarRtStepParams params = new PiSistarRtStepParams();
        Integer pageNo = 1;
        Integer pageSize = 10;
        String orderField = null;
        String orderType = null;
        String selectFields = null;

        Map<String, String[]> parameterMap = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameterMap);

        Page<PiExtractTableField> page = new Page<>(pageNo, pageSize);
        List<PiExtractTableField> records = new ArrayList<>();
        PiExtractTableField record = new PiExtractTableField();
        record.setId("1");
        record.setSourceField("test_field");
        records.add(record);
        page.setRecords(records);
        page.setTotal(1L);

        when(piSistarRtStepParamsService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        Result<IPage<PiSistarRtStepParams>> result = controller.queryPageList(
                params, pageNo, pageSize, orderField, orderType, selectFields, request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getResult().getTotal());
    }

    /**
     * 测试服务层返回null
     */
    @Test
    void testQueryPageList_ServiceReturnsNull() {
        PiSistarRtStepParams params = new PiSistarRtStepParams();
        Integer pageNo = 1;
        Integer pageSize = 10;

        Map<String, String[]> parameterMap = new HashMap<>();
        when(request.getParameterMap()).thenReturn(parameterMap);

        when(piSistarRtStepParamsService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(null);

        Result<IPage<PiSistarRtStepParams>> result = controller.queryPageList(
                params, pageNo, pageSize, null, null, null, request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNull(result.getResult());
    }
}
