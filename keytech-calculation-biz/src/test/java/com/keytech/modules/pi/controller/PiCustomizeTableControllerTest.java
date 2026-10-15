package com.keytech.modules.pi.controller;

import com.keytech.modules.pi.dto.PointDiagramDto;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import org.jeecg.common.api.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * --story=1055781 --user=孙金伟 24-PI点图曲线图开发 https://www.tapd.cn/63554897/s/1384076 单元测试
 * --story=1057867 --user=孙金伟 24-PI点图多标准开发 https://www.tapd.cn/63554897/s/1393029 单元测试
 */
class PiCustomizeTableControllerTest {

    @InjectMocks
    private PiCustomizeTableController controller; // 被测类

    @Mock
    private IPiCustomizeTableService piCustomizeTableService; // 需要 mock 的 service

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // 初始化 mock
    }

    /**
     * 测试正常流程：service 返回正常数据
     */
    @Test
    void testGetPointDiagramData_Normal() {
        // 准备输入参数
        PointDiagramDto dto = new PointDiagramDto();
        dto.setFieldName("testField");
        dto.setSearchTimeStart(new Date());
        dto.setSearchTimeEnd(new Date());
        dto.setStandardId("standard123");
        dto.setStrSql("SELECT * FROM test");
        dto.setTableName("testTable");
        dto.setXFieldName("xField");

        // 准备 service 返回值
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("data", "mockChartData");

        // mock service 方法
        when(piCustomizeTableService.getPointDiagramData(dto)).thenReturn(mockData);

        // 调用被测方法
        Result<?> result = controller.getPointDiagramData(dto);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertEquals(mockData, result.getResult());

        // 验证 service 被调用一次
        verify(piCustomizeTableService, times(1)).getPointDiagramData(dto);
    }

    /**
     * 测试 service 返回 null 的情况
     */
    @Test
    void testGetPointDiagramData_ServiceReturnsNull() {
        PointDiagramDto dto = new PointDiagramDto();
        dto.setFieldName("testField");

        when(piCustomizeTableService.getPointDiagramData(dto)).thenReturn(null);

        Result<?> result = controller.getPointDiagramData(dto);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertNull(result.getResult());

        verify(piCustomizeTableService, times(1)).getPointDiagramData(dto);
    }

    /**
     * 测试 service 抛出异常的情况
     */
    @Test
    void testGetPointDiagramData_ServiceThrowsException() {
        PointDiagramDto dto = new PointDiagramDto();
        dto.setFieldName("testField");

        // 模拟抛出异常
        when(piCustomizeTableService.getPointDiagramData(dto)).thenThrow(new RuntimeException("Service Error"));

        // 断言抛出异常
        assertThrows(RuntimeException.class, () -> {
            controller.getPointDiagramData(dto);
        });

        verify(piCustomizeTableService, times(1)).getPointDiagramData(dto);
    }

    /**
     * --story=1055786 --user=孙金伟 24-PI点图箱线图开发 https://www.tapd.cn/63554897/s/1384954 单元测试
     */
    @Test
    void testGetBoxDiagramData_Normal() {
        // 准备输入参数
        PointDiagramDto dto = new PointDiagramDto();
        dto.setFieldName("boxField");
        dto.setSearchTimeStart(new Date());
        dto.setSearchTimeEnd(new Date());
        dto.setStandardId("standard123");
        dto.setStrSql("SELECT * FROM box_table");
        dto.setTableName("boxTable");
        dto.setXFieldName("xField");

        // 准备 service 返回值
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("boxData", "mockBoxChartData");

        // mock service 方法
        when(piCustomizeTableService.getBoxDiagramData(dto)).thenReturn(mockData);

        // 调用被测方法
        Result<?> result = controller.getBoxDiagramData(dto);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200, result.getCode());
        assertEquals(mockData, result.getResult());

        // 验证 service 被调用一次
        verify(piCustomizeTableService, times(1)).getBoxDiagramData(dto);
    }
}
