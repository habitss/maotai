package com.keytech.modules.maotai.workorder;

import com.keytech.modules.maotai.workorder.controller.WorkOrderController;
import com.keytech.modules.maotai.workorder.entity.WorkOrder;
import com.keytech.modules.maotai.workorder.service.WorkOrderService;
import org.jeecg.common.api.vo.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 工单控制器单元测试:batchAdd 请求体(Map<String,Object>)→ WorkOrder 的 Jackson 转换。
 * 回归点:BODY_MAPPER 必须注册 JSR310,否则 LocalDate planDate 反序列化直接报
 * "Java 8 date/time type not supported by default"(2026-09-28 现网事故)。
 * 纯 Mockito 单测不起 Spring 容器;Shiro 无上下文时操作人回退 "system"。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("工单控制器 WorkOrderController.batchAdd")
class WorkOrderControllerTest {

    @Mock
    private WorkOrderService workOrderService;

    @InjectMocks
    private WorkOrderController controller;

    /** 前端 WorkOrderBatchForm 真实 payload 形状:planDate 为 YYYY-MM-DD 字符串 */
    private Map<String, Object> batchBody() {
        Map<String, Object> common = new HashMap<>();
        common.put("workshopCode", "DG");
        common.put("processName", "倒罐工艺");
        common.put("planDate", "2026-09-29");
        common.put("planStartTime", "09:00");
        common.put("planEndTime", "10:00");
        // rows 原样上送时带 _key/pathOptions(实体无此字段),不能因未知字段报错
        Map<String, Object> row = new HashMap<>();
        row.put("_key", 1);
        row.put("pathOptions", List.of(Map.of("label", "1#成品进酒线", "value", "C300DX01")));
        row.put("sourceTank", "ZTK201");
        row.put("targetTank", "ZTK301");
        row.put("transferPath", "ZTK201→ZTK301");
        row.put("amount", 30);
        Map<String, Object> body = new HashMap<>();
        body.put("common", common);
        body.put("rows", List.of(row));
        return body;
    }

    @Test
    @DisplayName("批量创建:planDate ISO 字符串正确解析为 LocalDate,明细逐行转换")
    void batchAddParsesLocalDate() {
        when(workOrderService.createBatch(any(WorkOrder.class), anyList(), anyString()))
                .thenReturn(List.of(new WorkOrder()));

        Result<List<WorkOrder>> result = controller.batchAdd(batchBody());

        assertTrue(result.isSuccess());
        ArgumentCaptor<WorkOrder> commonCap = ArgumentCaptor.forClass(WorkOrder.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WorkOrder>> rowsCap = ArgumentCaptor.forClass(List.class);
        verify(workOrderService).createBatch(commonCap.capture(), rowsCap.capture(), anyString());
        assertEquals(LocalDate.of(2026, 9, 29), commonCap.getValue().getPlanDate());
        assertEquals("DG", commonCap.getValue().getWorkshopCode());
        List<WorkOrder> rows = rowsCap.getValue();
        assertEquals(1, rows.size());
        assertEquals("ZTK201", rows.get(0).getSourceTank());
        assertEquals("ZTK301", rows.get(0).getTargetTank());
    }

    @Test
    @DisplayName("批量创建:无 Shiro 上下文时操作人回退 system")
    void batchAddOperatorFallsBackToSystem() {
        when(workOrderService.createBatch(any(WorkOrder.class), anyList(), anyString()))
                .thenReturn(List.of(new WorkOrder()));

        controller.batchAdd(batchBody());

        verify(workOrderService).createBatch(any(WorkOrder.class), anyList(), anyString());
        ArgumentCaptor<String> opCap = ArgumentCaptor.forClass(String.class);
        verify(workOrderService).createBatch(any(WorkOrder.class), anyList(), opCap.capture());
        assertEquals("system", opCap.getValue());
    }
}
