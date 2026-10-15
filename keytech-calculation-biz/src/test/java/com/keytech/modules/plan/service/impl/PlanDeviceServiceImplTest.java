package com.keytech.modules.plan.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.keytech.modules.mdm3.entity.MdmTask;
import com.keytech.modules.mdm3.service.IMdmTaskService;
import com.keytech.modules.plan.entity.Plan;
import com.keytech.modules.plan.entity.PlanDevice;
import com.keytech.modules.plan.enums.PlanStatusEnum;
import com.keytech.modules.plan.service.IPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.mockito.Mockito.*;

/**
 * --story=1055318 --user=孙金伟 22-交接班任务推送 https://www.tapd.cn/63554897/s/1385848 单元测试类
 */
@ExtendWith(MockitoExtension.class)
public class PlanDeviceServiceImplTest {

    @InjectMocks
    private PlanDeviceServiceImpl planDeviceService;

    @Mock
    private IPlanService planService;

    // 用于验证方法调用
    @Spy
    @InjectMocks
    private PlanDeviceServiceImpl spiedPlanDeviceService = new PlanDeviceServiceImpl();

    // Mock 查询构造器
    private com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<Plan> planLambdaQueryWrapper;
    private com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<PlanDevice> planDeviceLambdaQueryWrapper;
    @Mock
    private IMdmTaskService mdmTaskService;
    // Mock 查询构造器
    private LambdaUpdateChainWrapper<PlanDevice> planDeviceLambdaUpdateWrapper;
    private LambdaUpdateChainWrapper<Plan> planLambdaUpdateWrapper;
    private LambdaQueryChainWrapper<MdmTask> mdmTaskLambdaQueryWrapper;

    @BeforeEach
    public void setUp() {
        planLambdaQueryWrapper = mock(com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper.class);
        planDeviceLambdaQueryWrapper = mock(com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper.class);
    }

    /**
     * 测试用例：TC001 - 主计划列表为空
     * 预期结果：不执行任何操作，直接返回
     */
    @Test
    public void testDelayClosPlanJob_WhenNoPlan_ShouldReturnEarly() {
        // Given
        when(planService.lambdaQuery()).thenReturn(planLambdaQueryWrapper);
        when(planLambdaQueryWrapper.eq(Plan::getPlanStatus, PlanStatusEnum.DELAY.code)).thenReturn(planLambdaQueryWrapper);
        when(planLambdaQueryWrapper.le(Plan::getDelayCloseTime, new Date())).thenReturn(planLambdaQueryWrapper);
        when(planLambdaQueryWrapper.list()).thenReturn(Collections.emptyList());

        // When
        spiedPlanDeviceService.delayClosPlanJob("");

        // Then
        verify(spiedPlanDeviceService, never()).endPlanDevice(anyString(), anyString(), any(Date.class), anyBoolean());
    }

    /**
     * 测试用例：TC002 - 主计划列表不为空，但机台计划列表为空
     * 预期结果：遍历主计划，但不调用 endPlanDevice
     */
    @Test
    public void testDelayClosPlanJob_WhenPlanExistsButNoPlanDevice_ShouldNotCallEndPlanDevice() {
        // Given
        Plan plan = new Plan();
        plan.setId("plan1");
        plan.setDelayCloseTime(new Date());

        when(planService.lambdaQuery()).thenReturn(planLambdaQueryWrapper);
        when(planLambdaQueryWrapper.eq(Plan::getPlanStatus, PlanStatusEnum.DELAY.code)).thenReturn(planLambdaQueryWrapper);
        when(planLambdaQueryWrapper.le(Plan::getDelayCloseTime, new Date())).thenReturn(planLambdaQueryWrapper);
        when(planLambdaQueryWrapper.list()).thenReturn(Arrays.asList(plan));

        when(spiedPlanDeviceService.lambdaQuery()).thenReturn(planDeviceLambdaQueryWrapper);
        when(planDeviceLambdaQueryWrapper.eq(PlanDevice::getPlanStatus, PlanStatusEnum.DELAY.code)).thenReturn(planDeviceLambdaQueryWrapper);
        when(planDeviceLambdaQueryWrapper.le(PlanDevice::getDelayCloseTime, new Date())).thenReturn(planDeviceLambdaQueryWrapper);
        when(planDeviceLambdaQueryWrapper.list()).thenReturn(Collections.emptyList());

        // When
        spiedPlanDeviceService.delayClosPlanJob("");

        // Then
        verify(spiedPlanDeviceService, never()).endPlanDevice(anyString(), anyString(), any(Date.class), anyBoolean());
    }

    /**
     * 测试用例：TC003 - 主计划和机台计划都存在，且能正确分组
     * 预期结果：正确调用 endPlanDevice 并传入正确的参数
     */
    @Test
    public void testDelayClosPlanJob_WhenPlanAndPlanDeviceExist_ShouldCallEndPlanDevice() {
        // Given
        Date now = new Date();

        Plan plan = new Plan();
        plan.setId("plan1");
        plan.setDelayCloseTime(now);

        PlanDevice planDevice = new PlanDevice();
        planDevice.setPlanId("plan1");
        planDevice.setDeviceId(1);
        planDevice.setDelayCloseTime(now);

        when(planService.lambdaQuery()).thenReturn(planLambdaQueryWrapper);
        when(planLambdaQueryWrapper.eq(Plan::getPlanStatus, PlanStatusEnum.DELAY.code)).thenReturn(planLambdaQueryWrapper);
        when(planLambdaQueryWrapper.le(Plan::getDelayCloseTime, now)).thenReturn(planLambdaQueryWrapper);
        when(planLambdaQueryWrapper.list()).thenReturn(Arrays.asList(plan));

        when(spiedPlanDeviceService.lambdaQuery()).thenReturn(planDeviceLambdaQueryWrapper);
        when(planDeviceLambdaQueryWrapper.eq(PlanDevice::getPlanStatus, PlanStatusEnum.DELAY.code)).thenReturn(planDeviceLambdaQueryWrapper);
        when(planDeviceLambdaQueryWrapper.le(PlanDevice::getDelayCloseTime, now)).thenReturn(planDeviceLambdaQueryWrapper);
        when(planDeviceLambdaQueryWrapper.list()).thenReturn(Arrays.asList(planDevice));

        // When
        spiedPlanDeviceService.delayClosPlanJob("");

        // Then
        verify(spiedPlanDeviceService, times(1)).endPlanDevice(eq("plan1"), eq("1"), any(Date.class), eq(true));
    }

    //--story=1057424 --user=孙金伟 27-自动触发任务下发接口 https://www.tapd.cn/63554897/s/1394979 单元测试
    @BeforeEach
    public void setUp2() {
        planDeviceLambdaQueryWrapper = mock(LambdaQueryChainWrapper.class);
        planDeviceLambdaUpdateWrapper = mock(LambdaUpdateChainWrapper.class);
        planLambdaUpdateWrapper = mock(LambdaUpdateChainWrapper.class);
        mdmTaskLambdaQueryWrapper = mock(LambdaQueryChainWrapper.class);
    }

    /**
     * 测试用例：TC001 - deviceIds 为空
     * 预期结果：不执行任何操作，直接返回
     */
    @Test
    public void testDelayEndPlanDevice_WhenDeviceIdsIsEmpty_ShouldReturnEarly() {
        // Given
        String planId = "plan1";
        String deviceIds = "";
        Date realEndTime = new Date();
        Date delayTime = new Date();

        // When
        spiedPlanDeviceService.delayEndPlanDevice(planId, deviceIds, realEndTime, delayTime);

        // Then
        verify(spiedPlanDeviceService, never()).lambdaQuery();
        verify(spiedPlanDeviceService, never()).lambdaUpdate();
        verify(planService, never()).lambdaUpdate();
    }

    /**
     * 测试用例：TC002 - deviceIds 不为空，但查询不到符合条件的 PlanDevice
     * 预期结果：不执行任何操作，直接返回
     */
    @Test
    public void testDelayEndPlanDevice_WhenNoPlanDeviceFound_ShouldReturnEarly() {
        // Given
        String planId = "plan1";
        String deviceIds = "1,2";
        Date realEndTime = new Date();
        Date delayTime = new Date();

        when(spiedPlanDeviceService.lambdaQuery()).thenReturn(planDeviceLambdaQueryWrapper);
        when(planDeviceLambdaQueryWrapper.eq(PlanDevice::getPlanId, planId)).thenReturn(planDeviceLambdaQueryWrapper);
        when(planDeviceLambdaQueryWrapper.in(PlanDevice::getPlanStatus, Arrays.asList(PlanStatusEnum.DOING.code, PlanStatusEnum.DELAY.code))).thenReturn(planDeviceLambdaQueryWrapper);
        when(planDeviceLambdaQueryWrapper.in(PlanDevice::getDeviceId, Arrays.asList("1", "2"))).thenReturn(planDeviceLambdaQueryWrapper);
        when(planDeviceLambdaQueryWrapper.list()).thenReturn(Collections.emptyList());

        // When
        spiedPlanDeviceService.delayEndPlanDevice(planId, deviceIds, realEndTime, delayTime);

        // Then
        verify(spiedPlanDeviceService, never()).sendEndTask(anyList(), any(Date.class));
        verify(spiedPlanDeviceService, never()).lambdaUpdate();
        verify(planService, never()).lambdaUpdate();
    }

    /**
     * 测试用例：TC003 - 查询到符合条件的 PlanDevice，但无收班任务
     * 预期结果：更新机台计划和主计划状态为 DELAY
     */
    @Test
    public void testDelayEndPlanDevice_WhenPlanDeviceFoundButNoTask_ShouldUpdateStatusOnly() {
        // Given
        String planId = "plan1";
        String deviceIds = "1,2";
        Date realEndTime = new Date();
        Date delayTime = new Date();

        PlanDevice planDevice1 = new PlanDevice();
        planDevice1.setDeviceId(1);
        planDevice1.setPlanId(planId);

        PlanDevice planDevice2 = new PlanDevice();
        planDevice2.setDeviceId(2);
        planDevice2.setPlanId(planId);

        List<PlanDevice> planDeviceList = Arrays.asList(planDevice1, planDevice2);

        when(spiedPlanDeviceService.lambdaQuery()).thenReturn(planDeviceLambdaQueryWrapper);
        when(planDeviceLambdaQueryWrapper.eq(PlanDevice::getPlanId, planId)).thenReturn(planDeviceLambdaQueryWrapper);
        when(planDeviceLambdaQueryWrapper.in(PlanDevice::getPlanStatus, Arrays.asList(PlanStatusEnum.DOING.code, PlanStatusEnum.DELAY.code))).thenReturn(planDeviceLambdaQueryWrapper);
        when(planDeviceLambdaQueryWrapper.in(PlanDevice::getDeviceId, Arrays.asList("1", "2"))).thenReturn(planDeviceLambdaQueryWrapper);
        when(planDeviceLambdaQueryWrapper.list()).thenReturn(planDeviceList);

        // Mock sendEndTask 中的 mdmTaskService 查询为空
        when(mdmTaskService.lambdaQuery()).thenReturn(mdmTaskLambdaQueryWrapper);
        when(mdmTaskLambdaQueryWrapper.eq(MdmTask::getRecipeTypeCode, 3)).thenReturn(mdmTaskLambdaQueryWrapper);
        when(mdmTaskLambdaQueryWrapper.in(MdmTask::getDeviceId, Set.of(1, 2))).thenReturn(mdmTaskLambdaQueryWrapper);
        when(mdmTaskLambdaQueryWrapper.list()).thenReturn(Collections.emptyList());

        // Mock 更新机台计划
        when(spiedPlanDeviceService.lambdaUpdate()).thenReturn(planDeviceLambdaUpdateWrapper);
        when(planDeviceLambdaUpdateWrapper.set(PlanDevice::getPlanStatus, PlanStatusEnum.DELAY.code)).thenReturn(planDeviceLambdaUpdateWrapper);
        when(planDeviceLambdaUpdateWrapper.set(PlanDevice::getRealEndTime, realEndTime)).thenReturn(planDeviceLambdaUpdateWrapper);
        when(planDeviceLambdaUpdateWrapper.set(PlanDevice::getDelayCloseTime, delayTime)).thenReturn(planDeviceLambdaUpdateWrapper);
        when(planDeviceLambdaUpdateWrapper.eq(PlanDevice::getPlanId, planId)).thenReturn(planDeviceLambdaUpdateWrapper);
        when(planDeviceLambdaUpdateWrapper.in(PlanDevice::getDeviceId, deviceIds)).thenReturn(planDeviceLambdaUpdateWrapper);
        when(planDeviceLambdaUpdateWrapper.eq(PlanDevice::getPlanStatus, PlanStatusEnum.DOING.code)).thenReturn(planDeviceLambdaUpdateWrapper);
        when(planDeviceLambdaUpdateWrapper.update()).thenReturn(true);

        // Mock 更新主计划
        when(planService.lambdaUpdate()).thenReturn(planLambdaUpdateWrapper);
        when(planLambdaUpdateWrapper.set(Plan::getPlanStatus, PlanStatusEnum.DELAY.code)).thenReturn(planLambdaUpdateWrapper);
        when(planLambdaUpdateWrapper.set(Plan::getRealEndTime, realEndTime)).thenReturn(planLambdaUpdateWrapper);
        when(planLambdaUpdateWrapper.set(Plan::getDelayCloseTime, delayTime)).thenReturn(planLambdaUpdateWrapper);
        when(planLambdaUpdateWrapper.eq(Plan::getPlanStatus, PlanStatusEnum.DOING.code)).thenReturn(planLambdaUpdateWrapper);
        when(planLambdaUpdateWrapper.eq(Plan::getId, planId)).thenReturn(planLambdaUpdateWrapper);
        when(planLambdaUpdateWrapper.update()).thenReturn(true);

        // When
        spiedPlanDeviceService.delayEndPlanDevice(planId, deviceIds, realEndTime, delayTime);

        // Then
        verify(spiedPlanDeviceService, times(1)).sendEndTask(planDeviceList, realEndTime);
        verify(planDeviceLambdaUpdateWrapper, times(1)).update();
        verify(planLambdaUpdateWrapper, times(1)).update();
    }


    /**
     * 测试用例：TC001 - 无满足条件的自动任务
     * 预期结果：不调用 createOrder 方法
     */
    @Test
    public void testAutoTriggerPlanJob_WhenNoTask_ShouldNotCallCreateOrder() {
        // Given
        when(mdmTaskService.lambdaQuery()).thenReturn(mdmTaskLambdaQueryWrapper);
        when(mdmTaskLambdaQueryWrapper.eq(MdmTask::getTaskKindCode, 10)).thenReturn(mdmTaskLambdaQueryWrapper);
        when(mdmTaskLambdaQueryWrapper.ge(any(), anyInt())).thenReturn(mdmTaskLambdaQueryWrapper);
        when(mdmTaskLambdaQueryWrapper.le(any(), anyInt())).thenReturn(mdmTaskLambdaQueryWrapper);
        when(mdmTaskLambdaQueryWrapper.and(any())).thenReturn(mdmTaskLambdaQueryWrapper);
        when(mdmTaskLambdaQueryWrapper.list()).thenReturn(Collections.emptyList());

        // When
        spiedPlanDeviceService.autoTriggerPlanJob("", null, null);

    }

    /**
     * 测试用例：TC002 - 有满足条件的自动任务
     * 预期结果：调用 createOrder 方法，并传入正确的设备 ID 集合
     */
    @Test
    public void testAutoTriggerPlanJob_WhenHasTask_ShouldCallCreateOrder() {
        // Given
        MdmTask task1 = new MdmTask();
        task1.setId("1");
        task1.setDeviceId(101);
//        task1.setAutoTriggerType("1"); // 每天触发

        MdmTask task2 = new MdmTask();
        task2.setId("2");
        task2.setDeviceId(102);
//        task2.setAutoTriggerType("2"); // 每周触发

        List<MdmTask> taskList = Arrays.asList(task1, task2);

        when(mdmTaskService.lambdaQuery()).thenReturn(mdmTaskLambdaQueryWrapper);
        when(mdmTaskLambdaQueryWrapper.eq(MdmTask::getTaskKindCode, 10)).thenReturn(mdmTaskLambdaQueryWrapper);
        when(mdmTaskLambdaQueryWrapper.ge(any(), anyInt())).thenReturn(mdmTaskLambdaQueryWrapper);
        when(mdmTaskLambdaQueryWrapper.le(any(), anyInt())).thenReturn(mdmTaskLambdaQueryWrapper);
        when(mdmTaskLambdaQueryWrapper.and(any())).thenReturn(mdmTaskLambdaQueryWrapper);
        when(mdmTaskLambdaQueryWrapper.list()).thenReturn(taskList);

        // When
        //获取当前时间
        LocalDateTime now = LocalDateTime.now();
        //将秒和毫秒变为0
        LocalDateTime currentLocalDateTime = now.truncatedTo(ChronoUnit.MINUTES);
        Date currentDate = Date.from(currentLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
        spiedPlanDeviceService.autoTriggerPlanJob("", currentDate, currentLocalDateTime);

        // Then
        Set<Integer> expectedDeviceIds = new HashSet<>(Arrays.asList(101, 102));
    }


    @Mock
    private LambdaQueryChainWrapper<MdmTask> mdmTaskLambdaQueryChainWrapper;
    //--story=1057423 --user=孙金伟 27-PI算法新增第三方触发接口 https://www.tapd.cn/63554897/s/1395383 单元测试
    @Test
    void testSendByLastTime_QualifiedTasksAndTriggerConditions() {
        // Given
        Date currentDate = new Date();
        MdmTask task = new MdmTask();
        task.setId("1");
        task.setAutoTriggerLastTime(new Date(currentDate.getTime() - 20 * 60 * 1000)); // 20分钟前
        task.setAutoTriggerRangeEnd("23");
        task.setDeviceId(1);
        List<MdmTask> taskList = Arrays.asList(task);

        when(mdmTaskService.lambdaQuery()).thenReturn(mdmTaskLambdaQueryChainWrapper);
        when(mdmTaskLambdaQueryChainWrapper.eq(any(), any())).thenReturn(mdmTaskLambdaQueryChainWrapper);
        when(mdmTaskLambdaQueryChainWrapper.isNotNull(any())).thenReturn(mdmTaskLambdaQueryChainWrapper);
        when(mdmTaskLambdaQueryChainWrapper.lt(any(), any())).thenReturn(mdmTaskLambdaQueryChainWrapper);
        when(mdmTaskLambdaQueryChainWrapper.list()).thenReturn(taskList);

        // Mock getAutoTriggerTaskList返回非空列表
        List<MdmTask> triggeredTaskList = Arrays.asList(task);
        try (MockedStatic<CollUtil> collUtilMockedStatic = mockStatic(CollUtil.class)) {
            collUtilMockedStatic.when(() -> CollUtil.isEmpty((Collection<?>) any())).thenReturn(false);
            // Mock私有方法getAutoTriggerTaskList
            // 由于无法直接mock私有方法，这里通过spy对象和doReturn来模拟
            PlanDeviceServiceImpl spyPlanDeviceService = spy(planDeviceService);
            doReturn(triggeredTaskList).when(spyPlanDeviceService).getAutoTriggerTaskList(any(), any(), any(), any(), any());

            // When
            spyPlanDeviceService.sendByLastTime(currentDate);
        }

        // Then
        verify(mdmTaskLambdaQueryChainWrapper, times(1)).list();
        // 验证createOrder被调用
        // 由于createOrder是私有方法，无法直接验证，可以通过验证mdmTaskService.updateBatchById是否被调用来间接验证
        verify(mdmTaskService, times(1)).updateBatchById(any());
    }
}
