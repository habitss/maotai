package com.keytech.modules.plan.controller;

import com.keytech.modules.plan.dto.PlanOrderResultDto;
import com.keytech.modules.plan.service.IPlanOrderResultService;
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
 * --story=1056938 --user=孙金伟 15-过程质量检查计划-检查项调整 https://www.tapd.cn/63554897/s/1385231 单元测试
 * --story=1056969 --user=孙金伟 15-过程质量检查计划-循环任务填报 https://www.tapd.cn/63554897/s/1387586 单元测试
 * --story=1060367 --user=孙金伟 27-14-计划录入可以按检查项查询 https://www.tapd.cn/63554897/s/1401467 单元测试
 */
class PlanOrderResultControllerTest {

    @InjectMocks
    private PlanOrderResultController planOrderResultController; // 被测控制器实例

    @Mock
    private IPlanOrderResultService planOrderResultService; // 模拟的服务层实例

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // 初始化Mockito注解
    }

    /**
     * 测试正常情况：服务层返回非空列表
     */
    @Test
    void testQueryByOrderId_SuccessWithNonEmptyList() {
        // 准备测试数据
        String orderId = "123";
        String resultConfigId = "456";
        List<PlanOrderResultDto> mockList = new ArrayList<>();
        PlanOrderResultDto dto = new PlanOrderResultDto();
        dto.setId("789");
        mockList.add(dto);

        // 模拟服务层行为
        when(planOrderResultService.queryByOrderId(orderId, resultConfigId)).thenReturn(mockList);

        // 调用被测方法
        Result<List<PlanOrderResultDto>> result = planOrderResultController.queryByOrderId(orderId, resultConfigId);

        // 断言结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertNotNull(result.getResult());
        assertEquals(1, result.getResult().size());
        assertEquals("789", result.getResult().get(0).getId());

        // 验证服务层方法被调用了一次
        verify(planOrderResultService, times(1)).queryByOrderId(orderId, resultConfigId);
    }

    /**
     * --story=1056968 --user=孙金伟 15-过程质量检查计划-循环任务配置 https://www.tapd.cn/63554897/s/1387395 单元测试
     */
    @Test
    void testQueryByOrderId_SuccessWithEmptyList() {
        // 准备测试数据
        String orderId = "123";
        String resultConfigId = "456";
        List<PlanOrderResultDto> mockList = new ArrayList<>();

        // 模拟服务层行为
        when(planOrderResultService.queryByOrderId(orderId, resultConfigId)).thenReturn(mockList);

        // 调用被测方法
        Result<List<PlanOrderResultDto>> result = planOrderResultController.queryByOrderId(orderId, resultConfigId);

        // 断言结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertNotNull(result.getResult());
        assertTrue(result.getResult().isEmpty());

        // 验证服务层方法被调用了一次
        verify(planOrderResultService, times(1)).queryByOrderId(orderId, resultConfigId);
    }

    /**
     * 测试异常情况：服务层抛出异常
     */
    @Test
    void testQueryByOrderId_ServiceThrowsException() {
        // 准备测试数据
        String orderId = "123";
        String resultConfigId = "456";

        // 模拟服务层抛出异常
        when(planOrderResultService.queryByOrderId(orderId, resultConfigId)).thenThrow(new RuntimeException("数据库异常"));

        // 调用被测方法
        Result<List<PlanOrderResultDto>> result = planOrderResultController.queryByOrderId(orderId, resultConfigId);

        // 断言结果（由于未捕获异常，默认返回500）
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(500, result.getCode());
        assertEquals("数据库异常", result.getMessage());

        // 验证服务层方法被调用了一次
        verify(planOrderResultService, times(1)).queryByOrderId(orderId, resultConfigId);
    }
}
