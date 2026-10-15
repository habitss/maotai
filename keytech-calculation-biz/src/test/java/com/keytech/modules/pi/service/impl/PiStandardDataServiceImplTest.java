package com.keytech.modules.pi.service.impl;

import com.keytech.modules.pi.dto.PiGreenDataDto;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * --story=1056123 --user=孙金伟 24-PI绿区率统计表按照新的表样进行开发 https://www.tapd.cn/63554897/s/1388203 单元测试
 */
class PiStandardDataServiceImplTest {

    private PiStandardDataServiceImpl piStandardDataService;

    @BeforeEach
    void setUp() {
        piStandardDataService = new PiStandardDataServiceImpl();
    }

    /**
     * 反射调用私有方法 calcPercentile
     */
    private void invokeCalcPercentile(PiGreenDataDto fieldVo, String fieldCode, List<Map<String, Object>> tableData) throws Exception {
        Method method = PiStandardDataServiceImpl.class.getDeclaredMethod("calcPercentile", PiGreenDataDto.class, String.class, List.class);
        method.setAccessible(true);
        method.invoke(piStandardDataService, fieldVo, fieldCode, tableData);
    }

    @Test
    @DisplayName("测试 tableData 为空的情况")
    void testCalcPercentile_EmptyTableData() throws Exception {
        PiGreenDataDto dto = new PiGreenDataDto();
        invokeCalcPercentile(dto, "value", new ArrayList<>());

        assertNotNull(dto.getFivePercentile());
        assertEquals("0.00", dto.getFivePercentile());
        assertEquals("0.00", dto.getTenPercentile());
        assertEquals("0.00", dto.getFiftyPercentile());
        assertEquals("0.00", dto.getNinetyPercentile());
        assertEquals("0.00", dto.getNinetyFivePercentile());
    }

    @Test
    @DisplayName("测试 tableData 中包含 null 值")
    void testCalcPercentile_WithNullValues() throws Exception {
        PiGreenDataDto dto = new PiGreenDataDto();
        List<Map<String, Object>> tableData = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("value", null);
        tableData.add(row);

        invokeCalcPercentile(dto, "value", tableData);

        assertEquals("0.00", dto.getFivePercentile());
    }

    @Test
    @DisplayName("测试 tableData 中包含字符串数字")
    void testCalcPercentile_WithStringNumber() throws Exception {
        PiGreenDataDto dto = new PiGreenDataDto();
        List<Map<String, Object>> tableData = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("value", "10.5");
        tableData.add(row);

        invokeCalcPercentile(dto, "value", tableData);

        assertEquals("10.50", dto.getFivePercentile());
    }

    @Test
    @DisplayName("测试正常数据输入")
    void testCalcPercentile_NormalData() throws Exception {
        PiGreenDataDto dto = new PiGreenDataDto();
        List<Map<String, Object>> tableData = new ArrayList<>();
        for (double i = 1; i <= 100; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("value", i);
            tableData.add(row);
        }

        invokeCalcPercentile(dto, "value", tableData);

        assertEquals("5.95", dto.getFivePercentile()); // 5% of 100 = 5.95 (interpolated)
        assertEquals("10.90", dto.getTenPercentile());
        assertEquals("50.50", dto.getFiftyPercentile());
        assertEquals("90.10", dto.getNinetyPercentile());
        assertEquals("95.05", dto.getNinetyFivePercentile());
    }
}
