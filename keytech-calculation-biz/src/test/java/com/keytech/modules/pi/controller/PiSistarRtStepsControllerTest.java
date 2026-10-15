package com.keytech.modules.pi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.pi.entity.PiExtractTableField;
import com.keytech.modules.pi.entity.PiSistarRtSteps;
import com.keytech.modules.pi.service.IPiSistarRtStepsService;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * PiSistarRtStepsController 单元测试类
 */
@ExtendWith(MockitoExtension.class)
public class PiSistarRtStepsControllerTest {

    /**
     * 被测试的控制器实例，其中的依赖会被mock对象自动注入
     */
    @InjectMocks
    private PiSistarRtStepsController piSistarRtStepsController;

    /**
     * mock的服务层接口
     */
    @Mock
    private IPiSistarRtStepsService piSistarRtStepsService;

    /**
     * mock的HTTP请求对象
     */
    @Mock
    private HttpServletRequest request;

    /**
     * 测试前准备数据
     */
    @BeforeEach
    public void setUp() {
        // 初始化测试数据可以在每个测试方法之前进行
    }

    /**
     * 测试正常分页查询场景
     */
    @Test
    public void testQueryPageList_NormalCase() {
        // 准备测试数据
        PiSistarRtSteps piSistarRtSteps = new PiSistarRtSteps();
        piSistarRtSteps.setId("1");
        piSistarRtSteps.setStepStatus("RUNNING");

        Integer pageNo = 1;
        Integer pageSize = 10;
        String orderField = "id";
        String orderType = "asc";
        String selectFields = "id,stepStatus";

        // 模拟HTTP请求参数
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("stepStatus", new String[]{"RUNNING"});
        when(request.getParameterMap()).thenReturn(parameterMap);

        // 创建模拟的分页结果
        Page<PiExtractTableField> page = new Page<>(pageNo, pageSize);
        List<PiExtractTableField> records = new ArrayList<>();
        PiExtractTableField record = new PiExtractTableField();
        record.setId("1");
        record.setSourceField("test_field");
        records.add(record);
        page.setRecords(records);
        page.setTotal(1L);

        // 当调用service的page方法时，返回模拟的分页结果
        when(piSistarRtStepsService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        // 执行被测试的方法
        Result<IPage<PiSistarRtSteps>> result = piSistarRtStepsController.queryPageList(
                piSistarRtSteps, pageNo, pageSize, orderField, orderType, selectFields, request);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertNotNull(result.getResult());
        assertEquals(1, result.getResult().getRecords().size());
        assertEquals(1, result.getResult().getTotal());
        assertEquals(piSistarRtSteps.getId(), result.getResult().getRecords().get(0).getId());
        assertEquals(piSistarRtSteps.getStepStatus(), result.getResult().getRecords().get(0).getStepStatus());
    }

    /**
     * 测试使用默认分页参数的场景
     */
    @Test
    public void testQueryPageList_DefaultPageParams() {
        // 准备测试数据
        PiSistarRtSteps piSistarRtSteps = new PiSistarRtSteps();

        // 使用默认的pageNo和pageSize
        Integer pageNo = 1;  // 默认值
        Integer pageSize = 10;  // 默认值

        // 模拟HTTP请求参数
        when(request.getParameterMap()).thenReturn(new HashMap<>());

        // 创建模拟的分页结果
        Page<PiExtractTableField> page = new Page<>(pageNo, pageSize);
        page.setRecords(new ArrayList<>());
        page.setTotal(0L);

        // 当调用service的page方法时，返回模拟的分页结果
        when(piSistarRtStepsService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        // 执行被测试的方法（使用默认参数）
        Result<IPage<PiSistarRtSteps>> result = piSistarRtStepsController.queryPageList(
                piSistarRtSteps, pageNo, pageSize, null, null, null, request);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertNotNull(result.getResult());
        assertEquals(0, result.getResult().getRecords().size());
        assertEquals(0, result.getResult().getTotal());
    }

    /**
     * 测试查询结果为空的场景
     */
    @Test
    public void testQueryPageList_EmptyResult() {
        // 准备测试数据
        PiSistarRtSteps piSistarRtSteps = new PiSistarRtSteps();
        piSistarRtSteps.setStepStatus("UNKNOWN");

        Integer pageNo = 2;
        Integer pageSize = 5;

        // 模拟HTTP请求参数
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("stepStatus", new String[]{"UNKNOWN"});
        when(request.getParameterMap()).thenReturn(parameterMap);

        // 创建模拟的空分页结果
        Page<PiExtractTableField> page = new Page<>(pageNo, pageSize);
        page.setRecords(new ArrayList<>());
        page.setTotal(0L);

        // 当调用service的page方法时，返回模拟的分页结果
        when(piSistarRtStepsService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        // 执行被测试的方法
        Result<IPage<PiSistarRtSteps>> result = piSistarRtStepsController.queryPageList(
                piSistarRtSteps, pageNo, pageSize, null, null, null, request);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertNotNull(result.getResult());
        assertEquals(0, result.getResult().getRecords().size());
        assertEquals(0, result.getResult().getTotal());
    }
}
