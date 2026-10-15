package com.keytech.modules.plan.controller;

import com.keytech.modules.plan.entity.Plan;
import com.keytech.modules.plan.service.IPlanService;
import org.jeecg.common.api.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * --story=1055404 --user=孙金伟 15-一分现场优化改造 https://www.tapd.cn/63554897/s/1379440 单元测试类
 * --story=1057422 --user=孙金伟 27-检查项目维护调整，新增糖化、发酵类别 https://www.tapd.cn/63554897/s/1393843 单元测试
 */
class PlanControllerTest {

    @InjectMocks
    private PlanController planController;

    @Mock
    private IPlanService planService;

    /**
     * 初始化mock对象
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试add方法成功场景
     */
    @Test
    void testAddSuccess() {
        // 准备测试数据
        Plan plan = new Plan();
        plan.setId("testId");
        plan.setCreateBy("testUser");
        plan.setCreateTime(new Date());
        
        Result<String> expectedResult = Result.ok("保存成功");

        // mock服务层返回值
        when(planService.savePlan(plan)).thenReturn(expectedResult);

        // 执行测试方法
        Result<String> actualResult = planController.add(plan);

        // 验证结果和交互
        assertEquals(expectedResult, actualResult);
        verify(planService, times(1)).savePlan(plan);
    }

    /**
     * 测试add方法异常场景
     */
    @Test
    void testAddWithNullPlan() {
        // 准备测试数据
        Plan plan = null;

        // mock服务层抛出异常
        when(planService.savePlan(null)).thenThrow(new IllegalArgumentException("Plan cannot be null"));

        try {
            // 执行测试方法
            planController.add(plan);
        } catch (Exception e) {
            // 验证异常类型和消息
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("Plan cannot be null", e.getMessage());
        }

        // 验证交互
        verify(planService, times(1)).savePlan(null);
    }
}
