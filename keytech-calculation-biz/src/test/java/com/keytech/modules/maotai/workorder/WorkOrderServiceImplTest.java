package com.keytech.modules.maotai.workorder;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.keytech.modules.maotai.equipment.mapper.EquipmentLedgerMapper;
import com.keytech.modules.maotai.scada.service.ScadaWorkOrderService;
import com.keytech.modules.maotai.workorder.entity.TransferRoute;
import com.keytech.modules.maotai.workorder.entity.WorkOrder;
import com.keytech.modules.maotai.workorder.mapper.TransferRouteMapper;
import com.keytech.modules.maotai.workorder.mapper.WorkOrderLogMapper;
import com.keytech.modules.maotai.workorder.mapper.WorkOrderMapper;
import com.keytech.modules.maotai.workorder.service.impl.WorkOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * 工单服务单元测试:工单号/批次号生成规则、创建校验与撞号重试、状态机流转。
 * 纯 Mockito 单测不起 Spring 容器;baseMapper 为父类字段,反射注入。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("工单服务 WorkOrderServiceImpl")
class WorkOrderServiceImplTest {

    @Mock
    private WorkOrderMapper workOrderMapper;
    @Mock
    private WorkOrderLogMapper logMapper;
    @Mock
    private TransferRouteMapper transferRouteMapper;
    @Mock
    private EquipmentLedgerMapper equipmentLedgerMapper;
    @Mock
    private ScadaWorkOrderService scadaService;

    @InjectMocks
    private WorkOrderServiceImpl service;

    @BeforeEach
    void injectBaseMapper() {
        // ServiceImpl.baseMapper 是父类 @Autowired 字段,@InjectMocks 不注入父类字段
        ReflectionTestUtils.setField(service, "baseMapper", workOrderMapper);
    }


    /** MP 3.5.3.2 的 selectOne 有 default 单参与双参两个重载,ServiceImpl.getOne 走双参——两条链分别完整 stub */
    @SafeVarargs
    final void stubSelectOne(WorkOrder... thenReturn) {
        org.mockito.stubbing.OngoingStubbing<WorkOrder> s1 = when(workOrderMapper.selectOne(any()));
        if (thenReturn.length == 0) {
            s1.thenReturn(null);
        } else {
            for (WorkOrder v : thenReturn) s1 = s1.thenReturn(v);
        }
        org.mockito.stubbing.OngoingStubbing<WorkOrder> s2 = when(workOrderMapper.selectOne(any(), anyBoolean()));
        if (thenReturn.length == 0) {
            s2.thenReturn(null);
        } else {
            for (WorkOrder v : thenReturn) s2 = s2.thenReturn(v);
        }
    }

    // ===== 取号:反射调用私有方法(白盒) =====

    private String invokeNextOrderNo(String processName) throws Exception {
        var m = WorkOrderServiceImpl.class.getDeclaredMethod("nextOrderNo", String.class);
        m.setAccessible(true);
        return (String) m.invoke(service, processName);
    }

    private String invokeNextBatchNo(WorkOrder order) throws Exception {
        var m = WorkOrderServiceImpl.class.getDeclaredMethod("nextBatchNo", WorkOrder.class);
        m.setAccessible(true);
        return (String) m.invoke(service, order);
    }

    @Test
    @DisplayName("工单号:无历史序列从 0001 开始,前缀=工艺码+当前年月")
    void nextOrderNo_startsFromOneWhenEmpty() throws Exception {
        stubSelectOne();
        String no = invokeNextOrderNo("倒罐工艺");
        assertTrue(no.matches("WO-DG\\d{4}-0001"), "实际: " + no);
    }

    @Test
    @DisplayName("工单号:按工艺×月独立续号(最新 0005 → 生成 0006)")
    void nextOrderNo_continuesSequence() throws Exception {
        WorkOrder latest = new WorkOrder().setOrderNo("WO-DG2609-0005");
        stubSelectOne(latest);
        assertEquals("WO-DG2609-0006", invokeNextOrderNo("倒罐工艺"));
    }

    @Test
    @DisplayName("批次号阶段码:卸XC/装ZC/灌GZ;倒罐按车间 ZZ(中转)/GD(勾调)/CP(成品)/DG(兜底);未知工艺GN")
    void nextBatchNo_stageCodeMatrix() throws Exception {
        stubSelectOne();
        assertEquals("XC-2609-01", invokeNextBatchNo(new WorkOrder().setProcessName("卸车工艺").setWorkshopCode("ZZG").setPlanDate(LocalDate.of(2026, 9, 14))));
        assertEquals("ZC-2609-01", invokeNextBatchNo(new WorkOrder().setProcessName("装车工艺").setWorkshopCode("ZZG").setPlanDate(LocalDate.of(2026, 9, 14))));
        assertEquals("GZ-2609-01", invokeNextBatchNo(new WorkOrder().setProcessName("灌装工艺").setWorkshopCode("CPG").setPlanDate(LocalDate.of(2026, 9, 14))));
        assertEquals("ZZ-2609-01", invokeNextBatchNo(new WorkOrder().setProcessName("倒罐工艺").setWorkshopCode("ZZG").setPlanDate(LocalDate.of(2026, 9, 14))));
        assertEquals("GD-2609-01", invokeNextBatchNo(new WorkOrder().setProcessName("倒罐工艺").setWorkshopCode("XLG").setPlanDate(LocalDate.of(2026, 9, 14))));
        assertEquals("CP-2609-01", invokeNextBatchNo(new WorkOrder().setProcessName("倒罐工艺").setWorkshopCode("CPG").setPlanDate(LocalDate.of(2026, 9, 14))));
        assertEquals("DG-2609-01", invokeNextBatchNo(new WorkOrder().setProcessName("倒罐工艺").setWorkshopCode("GWG").setPlanDate(LocalDate.of(2026, 9, 14))));
        assertEquals("GN-2609-01", invokeNextBatchNo(new WorkOrder().setProcessName("新工艺").setWorkshopCode("ZZG").setPlanDate(LocalDate.of(2026, 9, 14))));
    }

    @Test
    @DisplayName("批次号:年月取工单日期而非当前日期(planDate=2026-10 → -2610-)")
    void nextBatchNo_usesPlanDateMonth() throws Exception {
        stubSelectOne();
        String no = invokeNextBatchNo(new WorkOrder().setProcessName("卸车工艺").setWorkshopCode("ZZG").setPlanDate(LocalDate.of(2026, 10, 5)));
        assertTrue(no.startsWith("XC-2610-"), "实际: " + no);
    }

    // ===== 创建工单 =====

    /** 一张字段完整、可过 validateForCreate 的倒罐工单 */
    private WorkOrder validOrder() {
        return new WorkOrder()
                .setWorkshopCode("ZZG").setProcessName("倒罐工艺")
                .setPlanDate(LocalDate.of(2026, 9, 14))
                .setSourceTank("ZTK101").setTargetTank("ZTK105")
                .setAmount(28.0).setAlcohol(53.0).setCurrentVehicleQty("10000")
                .setTransferPath("Z100CX02");
    }

    @BeforeEach
    void stubRouteAndCount() {
        // 配置表返回匹配路径 + 无重复单号
        TransferRoute route = new TransferRoute();
        route.setRouteCode("Z100CX02");
        route.setRouteName("2#罐内倒酒线");
        route.setEnabled(1);
        when(transferRouteMapper.selectList(any(Wrapper.class))).thenReturn(List.of(route));
        when(workOrderMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(workOrderMapper.insert(any(WorkOrder.class))).thenReturn(1);
    }

    @Test
    @DisplayName("创建:批次号留空自动生成(一工单一批次),状态置待执行,写创建日志")
    void createOrder_generatesBatchNoAndPending() {
        WorkOrder created = service.createOrder(validOrder(), "管理员");
        assertNotNull(created.getBatchNo(), "批次号应自动生成");
        assertTrue(created.getBatchNo().matches("ZZ-2609-\\d{2}"), "实际: " + created.getBatchNo());
        assertEquals(WorkOrder.STATUS_PENDING, created.getStatus());
        assertEquals("管理员", created.getCreateBy());
        assertEquals("2#罐内倒酒线", created.getTransferPathName(), "路径名称应从配置表带出");
        verify(logMapper, times(1)).insert(any());
    }

    @Test
    @DisplayName("创建:人工指定批次号尊重原值不覆盖")
    void createOrder_keepsManualBatchNo() {
        WorkOrder created = service.createOrder(validOrder().setBatchNo("ZZ-2609-99"), "管理员");
        assertEquals("ZZ-2609-99", created.getBatchNo());
    }

    @Test
    @DisplayName("创建:自动取号撞号时(DuplicateKeyException)自动重取一次并成功")
    void createOrder_retriesOnceOnDuplicate() {
        WorkOrder latest = new WorkOrder().setOrderNo("WO-DG2609-0005");
        // 首次 selectOne(nextOrderNo) 返回 0005;重试后的取号返回 0006
        stubSelectOne(latest, new WorkOrder().setOrderNo("WO-DG2609-0006"));
        when(workOrderMapper.insert(any(WorkOrder.class)))
                .thenThrow(new DuplicateKeyException("uk_order_no"))
                .thenReturn(1);
        WorkOrder created = service.createOrder(validOrder(), "管理员");
        assertEquals("WO-DG2609-0007", created.getOrderNo(), "撞号后重取:首次读0005生成0006撞号,重取读到已提交的0006续号0007");
        verify(workOrderMapper, times(2)).insert(any(WorkOrder.class));
    }

    @Test
    @DisplayName("创建校验:未配置的转酒路径被拒(防绕过前端提交)")
    void createOrder_rejectsUnknownRoute() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createOrder(validOrder().setTransferPath("NOT-EXIST"), "管理员"));
        assertTrue(ex.getMessage().contains("转酒路径无效"), ex.getMessage());
    }

    @Test
    @DisplayName("创建校验:倒罐缺源罐被拒")
    void createOrder_requiresSourceTank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createOrder(validOrder().setSourceTank(null), "管理员"));
        assertEquals("请填写源罐", ex.getMessage());
    }

    @Test
    @DisplayName("创建校验:人工指定工单号与存量重复被拒(不走重试)")
    void createOrder_rejectsDuplicateManualOrderNo() {
        when(workOrderMapper.selectCount(any(Wrapper.class))).thenReturn(1L);
        WorkOrder o = validOrder().setOrderNo("WO-DG2609-0005");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createOrder(o, "管理员"));
        assertTrue(ex.getMessage().contains("工单号已存在"), ex.getMessage());
    }

    // ===== 状态机 =====

    @Test
    @DisplayName("启动:待执行灌装单→执行中(灌装不推外部系统,操作人落库)")
    void transition_startPendingToRunning() {
        WorkOrder db = validOrder().setId("t1").setOrderNo("WO-GZ2609-0001")
                .setStatus(WorkOrder.STATUS_PENDING).setProcessName("灌装工艺").setDelFlag(0);
        when(workOrderMapper.selectById("t1")).thenReturn(db);
        when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);

        WorkOrder r = service.transition("t1", "start", "管理员");
        assertEquals(WorkOrder.STATUS_RUNNING, r.getStatus());
        assertNotNull(r.getStartTime());
        assertEquals("管理员", r.getOperator());
        verifyNoInteractions(scadaService);
        verify(logMapper, times(1)).insert(any());
    }

    @Test
    @DisplayName("启动:非待执行状态被拒(仅待执行工单可启动)")
    void transition_startRejectedWhenCompleted() {
        WorkOrder db = validOrder().setId("t2").setOrderNo("WO-GZ2609-0002")
                .setStatus(WorkOrder.STATUS_COMPLETED).setProcessName("灌装工艺").setDelFlag(0);
        when(workOrderMapper.selectById("t2")).thenReturn(db);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.transition("t2", "start", "管理员"));
        assertEquals("仅待执行工单可启动", ex.getMessage());
    }

    @Test
    @DisplayName("暂停:执行中→已暂停")
    void transition_pauseRunningToPaused() {
        WorkOrder db = validOrder().setId("t3").setOrderNo("WO-GZ2609-0003")
                .setStatus(WorkOrder.STATUS_RUNNING).setProcessName("灌装工艺").setDelFlag(0);
        when(workOrderMapper.selectById("t3")).thenReturn(db);
        when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);
        assertEquals(WorkOrder.STATUS_PAUSED, service.transition("t3", "pause", "管理员").getStatus());
    }

    @Test
    @DisplayName("状态机:不支持的操作被拒")
    void transition_rejectsUnknownAction() {
        WorkOrder db = validOrder().setId("t4").setOrderNo("WO-GZ2609-0004")
                .setStatus(WorkOrder.STATUS_PENDING).setProcessName("灌装工艺").setDelFlag(0);
        when(workOrderMapper.selectById("t4")).thenReturn(db);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.transition("t4", "fly", "管理员"));
        assertTrue(ex.getMessage().contains("不支持的操作"), ex.getMessage());
    }

    @Test
    @DisplayName("状态机:工单不存在被拒")
    void transition_rejectsMissingOrder() {
        when(workOrderMapper.selectById("nope")).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.transition("nope", "start", "管理员"));
        assertEquals("工单不存在", ex.getMessage());
    }

    // ===== 转酒路径候选 =====

    @Test
    @DisplayName("路径候选:名称=路径名(编码)拼接,撬编码带出")
    void transferPaths_mapsConfigRows() {
        TransferRoute r = new TransferRoute();
        r.setRouteCode("Z100CX02");
        r.setRouteName("2#罐内倒酒线");
        r.setSkidCode("HP-01");
        when(transferRouteMapper.selectList(any(Wrapper.class))).thenReturn(List.of(r));
        var paths = service.transferPaths("ZZG", "倒罐工艺", "ZTK101", "ZTK105");
        assertEquals(1, paths.size());
        assertEquals("Z100CX02", paths.get(0).getCode());
        assertEquals("2#罐内倒酒线（Z100CX02）", paths.get(0).getName());
        assertEquals("HP-01", paths.get(0).getSkidCode());
    }

    @Test
    @DisplayName("路径候选:配置表无匹配返回空(无代码兜底)")
    void transferPaths_emptyWhenNotConfigured() {
        when(transferRouteMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        assertTrue(service.transferPaths("ZZG", "倒罐工艺", "A", "B").isEmpty());
    }
}
