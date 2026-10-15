package com.keytech.modules.kpi.controller;

import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.service.IKpiReportActualWeekValueService;
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
 * --story=1058459 --user=孙金伟 33-KPI填报5w分析重复引用 https://www.tapd.cn/63554897/s/1393506 单元测试类
 */
class KpiReportActualWeekValueControllerTest {

    @InjectMocks
    private KpiReportActualWeekValueController controller;

    @Mock
    private IKpiReportActualWeekValueService kpiReportActualWeekValueService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试正常情况：服务层返回数据
     */
    @Test
    void testListKpiReportData_NormalCase() {
        // 准备数据
        KpiReportValueDto dto = new KpiReportValueDto();
        dto.setKpiCode("KPI001");
        List<Map<String, Object>> mockData = new ArrayList<>();
        mockData.add(new HashMap<>() {{
            put("week", 1);
            put("value", 100.0);
        }});

        // Mock 服务层返回
        doReturn(mockData).when(kpiReportActualWeekValueService).listKpiReportData(dto);

        // 调用被测方法
        Result<?> result = controller.listKpiReportData(dto);

        // 断言
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertNotNull(result.getResult());
        assertEquals(mockData, result.getResult());
    }

    /**
     * 测试边界情况：服务层返回 null
     */
    @Test
    void testListKpiReportData_ServiceReturnsNull() {
        KpiReportValueDto dto = new KpiReportValueDto();
        dto.setKpiCode("KPI002");

        when(kpiReportActualWeekValueService.listKpiReportData(dto)).thenReturn(null);

        Result<?> result = controller.listKpiReportData(dto);

        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertNull(result.getResult());
    }

    /**
     * 测试异常情况：服务层抛出异常
     */
    @Test
    void testListKpiReportData_ServiceThrowsException() {
        KpiReportValueDto dto = new KpiReportValueDto();
        dto.setKpiCode("KPI003");

        when(kpiReportActualWeekValueService.listKpiReportData(dto)).thenThrow(new RuntimeException("服务异常"));

        assertThrows(RuntimeException.class, () -> {
            controller.listKpiReportData(dto);
        });
    }

    /**
     * 测试边界情况：输入参数为 null
     */
    @Test
    void testListKpiReportData_DtoIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            controller.listKpiReportData(null);
        });
    }
}
