package com.keytech.modules.plan.controller;

import com.keytech.modules.mdm3.entity.MdmTask;
import com.keytech.modules.plan.service.IPlanDeviceService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.GlobalErrorEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * --story=1056963 --user=孙金伟 15-过程质量检查计划-计划结束逻辑调整 https://www.tapd.cn/63554897/s/1385493 单元测试
 */
class PlanDeviceControllerTest {

    // 自动注入被测类，并自动注入其依赖的 Mock 对象
    @InjectMocks
    private PlanDeviceController planDeviceController;

    // Mock IPlanDeviceService 实例
    @Mock
    private IPlanDeviceService planDeviceService;

    // 用于管理 Mockito 的初始化和清理
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        // 初始化 Mockito 注解
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        // 关闭 Mockito 上下文，释放资源
        closeable.close();
    }

    /**
     * 测试 planJob 接口正常调用的情况
     */
    @Test
    void testPlanJob_NormalCase() {
        // 准备测试数据
        String paramJson = "{\"taskId\": \"123\", \"action\": \"start\"}";

        // 设置 Mock 行为：planJob 不执行任何操作
        doNothing().when(planDeviceService).planJob(paramJson);

        // 执行被测方法
        Result<String> result = planDeviceController.planJob(paramJson);

        // 验证 service 方法是否被正确调用一次
        verify(planDeviceService, times(1)).planJob(paramJson);

        // 验证返回结果是否符合预期
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
        assertNull(result.getResult()); // 因为只返回 code，result 为 null
    }

    /**
     * 测试 planJob 接口传入空字符串参数的情况
     */
    @Test
    void testPlanJob_EmptyParamJson() {
        String paramJson = "";

        doNothing().when(planDeviceService).planJob(paramJson);

        Result<String> result = planDeviceController.planJob(paramJson);

        verify(planDeviceService, times(1)).planJob(paramJson);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
    }

    /**
     * 测试 planJob 接口传入 null 参数的情况
     */
    @Test
    void testPlanJob_NullParamJson() {
        String paramJson = null;

        doNothing().when(planDeviceService).planJob(paramJson);

        Result<String> result = planDeviceController.planJob(paramJson);

        verify(planDeviceService, times(1)).planJob(paramJson);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
    }



    /**
     * --story=1059857 --user=孙金伟 27-手动触发任务开发 https://www.tapd.cn/63554897/s/1399579 单元测试
     * 测试 sendManualTrigger 接口正常调用的情况
     */
    @Test
    void testSendManualTrigger_NormalCase() {
        // 准备测试数据
        List<MdmTask> taskList = new ArrayList<>();
        taskList.add(new MdmTask());

        // 设置 Mock 行为：sendManualTrigger 不执行任何操作
        doNothing().when(planDeviceService).sendManualTrigger(anyList(), any(Date.class));

        // 执行被测方法
        Result<String> result = planDeviceController.sendManualTrigger(taskList);

        // 验证 service 方法是否被正确调用一次
        verify(planDeviceService, times(1)).sendManualTrigger(eq(taskList), any(Date.class));

        // 验证返回结果是否符合预期
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
        assertNull(result.getResult()); // 因为只返回 code，result 为 null
    }

    /**
     * --story=1059857 --user=孙金伟 27-手动触发任务开发 https://www.tapd.cn/63554897/s/1399579 单元测试
     * 测试 sendManualTrigger 接口传入空列表的情况
     */
    @Test
    void testSendManualTrigger_EmptyList() {
        List<MdmTask> taskList = new ArrayList<>();

        doNothing().when(planDeviceService).sendManualTrigger(anyList(), any(Date.class));

        Result<String> result = planDeviceController.sendManualTrigger(taskList);

        verify(planDeviceService, times(1)).sendManualTrigger(eq(taskList), any(Date.class));

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
    }

    /**
     * --story=1059857 --user=孙金伟 27-手动触发任务开发 https://www.tapd.cn/63554897/s/1399579 单元测试
     * 测试 sendManualTrigger 接口传入 null 列表的情况
     */
    @Test
    void testSendManualTrigger_NullList() {
        List<MdmTask> taskList = null;

        doNothing().when(planDeviceService).sendManualTrigger(anyList(), any(Date.class));

        Result<String> result = planDeviceController.sendManualTrigger(taskList);

        verify(planDeviceService, times(1)).sendManualTrigger(isNull(), any(Date.class));

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
    }
}
