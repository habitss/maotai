package com.keytech.modules.pi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.fivewhy.entity.FiveWhyAnalyse;
import com.keytech.modules.fivewhy.service.IFiveWhyAnalyseService;
import com.keytech.modules.pi.entity.PiAllocationTableField;
import com.keytech.modules.pi.entity.PiCustomizeTable;
import com.keytech.modules.pi.entity.PiStandardData;
import com.keytech.modules.pi.entity.PiStandardMaintenance;
import com.keytech.modules.pi.mapper.PiAllocationTableFieldMapper;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import com.keytech.modules.pi.service.IPiStandardDataService;
import com.keytech.modules.pi.service.IPiStandardMaintenanceService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
//--story=1058410 --user=孙金伟 32-PI报表5w分析重复引用 https://www.tapd.cn/63554897/s/1393291 单元测试
class PiAllocationTableFieldServiceImplTest {

    @InjectMocks
    private PiAllocationTableFieldServiceImpl piAllocationTableFieldService;

    @Mock
    private IPiCustomizeTableService iPiCustomizeTableService;

    @Mock
    private PiAllocationTableFieldMapper piAllocationTableFieldMapper;

    @Mock
    private IPiStandardDataService iPiStandardDataService;

    @Mock
    private IPiStandardMaintenanceService iPiStandardMaintenanceService;

    @Mock
    private IFiveWhyAnalyseService fiveWhyAnalyseService;

    @Mock
    private HttpServletRequest req;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testQueryData_NormalCase() {
        // 准备测试数据
        Integer pageNo = 1;
        Integer pageSize = 10;
        PiAllocationTableField piAllocationTableField = new PiAllocationTableField();
        piAllocationTableField.setTableId("tableId");
        piAllocationTableField.setAllocationTableId("allocationTableId");

        // 模拟 HttpServletRequest 参数
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("fieldName1", new String[]{"value1"});
        when(req.getParameterMap()).thenReturn(parameterMap);

        // 模拟自定义报表
        PiCustomizeTable piCustomizeTable = new PiCustomizeTable();
        piCustomizeTable.setTableName("test_table");
        when(iPiCustomizeTableService.getById("tableId")).thenReturn(piCustomizeTable);

        // 模拟字段列表
        List<PiAllocationTableField> fieldList = new ArrayList<>();
        PiAllocationTableField field1 = new PiAllocationTableField();
        field1.setFieldName("fieldName1");
        field1.setIsQuery(1);
        field1.setIsList(1);
        field1.setSort(1.0);
        fieldList.add(field1);
        when(piAllocationTableFieldService.list(any(LambdaQueryWrapper.class))).thenReturn(fieldList);

        // 模拟数据总数
        when(piAllocationTableFieldMapper.getTableDataCount(anyMap())).thenReturn(1);

        // 模拟分页数据
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", 1L);
        dataMap.put("fieldName1", "value1");
        dataList.add(dataMap);
        when(piAllocationTableFieldMapper.getTableData(anyMap())).thenReturn(dataList);

        // 模拟标准数据
        List<PiStandardData> standardDataList = new ArrayList<>();
        PiStandardData standardData = new PiStandardData();
        standardData.setDataId("1");
        standardData.setManageId("manageId1");
        standardData.setStandardJson("[{\"maintenance_id\":\"maintenanceId1\"}]");
        standardDataList.add(standardData);
        when(iPiStandardDataService.list(any(LambdaQueryWrapper.class))).thenReturn(standardDataList);

        // 模拟标准维护数据
        List<PiStandardMaintenance> maintenanceList = new ArrayList<>();
        PiStandardMaintenance maintenance = new PiStandardMaintenance();
        maintenance.setId("maintenanceId1");
        maintenance.setFieldName("fieldName1");
        maintenance.setStandardValue(100.0);
        maintenanceList.add(maintenance);
        when(iPiStandardMaintenanceService.list(any(LambdaQueryWrapper.class))).thenReturn(maintenanceList);

        // 模拟5W数据
        List<FiveWhyAnalyse> fiveList = new ArrayList<>();
        FiveWhyAnalyse fiveWhyAnalyse = new FiveWhyAnalyse();
        fiveWhyAnalyse.setDataId("1");
        fiveWhyAnalyse.setFieldCode("fieldName1");
        fiveList.add(fiveWhyAnalyse);
        when(fiveWhyAnalyseService.list(any(LambdaQueryWrapper.class))).thenReturn(fiveList);

        // 调用被测方法
        Map<String, Object> result = piAllocationTableFieldService.queryData(pageNo, pageSize, req, piAllocationTableField);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.get("total"));
        List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
        assertNotNull(records);
        assertEquals(1, records.size());
        Map<String, Object> record = records.get(0);
        assertEquals("value1", record.get("fieldName1"));
        assertEquals(100.0, record.get("fieldName1_2"));
        assertEquals(-99.0, record.get("fieldName1_3"));
        assertTrue((Boolean) record.get("fieldName1_f"));
    }

    @Test
    void testQueryData_TableIdIsNull() {
        // 准备测试数据
        Integer pageNo = 1;
        Integer pageSize = 10;
        PiAllocationTableField piAllocationTableField = new PiAllocationTableField();
        piAllocationTableField.setTableId(null);
        piAllocationTableField.setAllocationTableId("allocationTableId");

        // 调用被测方法
        Map<String, Object> result = piAllocationTableFieldService.queryData(pageNo, pageSize, req, piAllocationTableField);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.get("total"));
        List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    void testQueryData_AllocationTableIdIsNull() {
        // 准备测试数据
        Integer pageNo = 1;
        Integer pageSize = 10;
        PiAllocationTableField piAllocationTableField = new PiAllocationTableField();
        piAllocationTableField.setTableId("tableId");
        piAllocationTableField.setAllocationTableId(null);

        // 调用被测方法
        Map<String, Object> result = piAllocationTableFieldService.queryData(pageNo, pageSize, req, piAllocationTableField);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.get("total"));
        List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    void testQueryData_NoQueryConditions() {
        // 准备测试数据
        Integer pageNo = 1;
        Integer pageSize = 10;
        PiAllocationTableField piAllocationTableField = new PiAllocationTableField();
        piAllocationTableField.setTableId("tableId");
        piAllocationTableField.setAllocationTableId("allocationTableId");

        // 模拟 HttpServletRequest 参数为空
        when(req.getParameterMap()).thenReturn(new HashMap<>());

        // 模拟自定义报表
        PiCustomizeTable piCustomizeTable = new PiCustomizeTable();
        piCustomizeTable.setTableName("test_table");
        when(iPiCustomizeTableService.getById("tableId")).thenReturn(piCustomizeTable);

        // 模拟字段列表
        List<PiAllocationTableField> fieldList = new ArrayList<>();
        PiAllocationTableField field1 = new PiAllocationTableField();
        field1.setFieldName("fieldName1");
        field1.setIsQuery(1);
        field1.setIsList(1);
        field1.setSort(1.0);
        fieldList.add(field1);
        when(piAllocationTableFieldService.list(any(LambdaQueryWrapper.class))).thenReturn(fieldList);

        // 模拟数据总数
        when(piAllocationTableFieldMapper.getTableDataCount(anyMap())).thenReturn(1);

        // 模拟分页数据
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", 1L);
        dataMap.put("fieldName1", "value1");
        dataList.add(dataMap);
        when(piAllocationTableFieldMapper.getTableData(anyMap())).thenReturn(dataList);

        // 模拟标准数据
        List<PiStandardData> standardDataList = new ArrayList<>();
        PiStandardData standardData = new PiStandardData();
        standardData.setDataId("1");
        standardData.setManageId("manageId1");
        standardData.setStandardJson("[{\"maintenance_id\":\"maintenanceId1\"}]");
        standardDataList.add(standardData);
        when(iPiStandardDataService.list(any(LambdaQueryWrapper.class))).thenReturn(standardDataList);

        // 模拟标准维护数据
        List<PiStandardMaintenance> maintenanceList = new ArrayList<>();
        PiStandardMaintenance maintenance = new PiStandardMaintenance();
        maintenance.setId("maintenanceId1");
        maintenance.setFieldName("fieldName1");
        maintenance.setStandardValue(100.0);
        maintenanceList.add(maintenance);
        when(iPiStandardMaintenanceService.list(any(LambdaQueryWrapper.class))).thenReturn(maintenanceList);

        // 模拟5W数据
        List<FiveWhyAnalyse> fiveList = new ArrayList<>();
        FiveWhyAnalyse fiveWhyAnalyse = new FiveWhyAnalyse();
        fiveWhyAnalyse.setDataId("1");
        fiveWhyAnalyse.setFieldCode("fieldName1");
        fiveList.add(fiveWhyAnalyse);
        when(fiveWhyAnalyseService.list(any(LambdaQueryWrapper.class))).thenReturn(fiveList);

        // 调用被测方法
        Map<String, Object> result = piAllocationTableFieldService.queryData(pageNo, pageSize, req, piAllocationTableField);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.get("total"));
        List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
        assertNotNull(records);
        assertEquals(1, records.size());
        Map<String, Object> record = records.get(0);
        assertEquals("value1", record.get("fieldName1"));
        assertEquals(100.0, record.get("fieldName1_2"));
        assertEquals(-99.0, record.get("fieldName1_3"));
        assertTrue((Boolean) record.get("fieldName1_f"));
    }

    @Test
    void testQueryData_WithQueryConditions() {
        // 准备测试数据
        Integer pageNo = 1;
        Integer pageSize = 10;
        PiAllocationTableField piAllocationTableField = new PiAllocationTableField();
        piAllocationTableField.setTableId("tableId");
        piAllocationTableField.setAllocationTableId("allocationTableId");

        // 模拟 HttpServletRequest 参数
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("fieldName1", new String[]{"value1"});
        when(req.getParameterMap()).thenReturn(parameterMap);

        // 模拟自定义报表
        PiCustomizeTable piCustomizeTable = new PiCustomizeTable();
        piCustomizeTable.setTableName("test_table");
        when(iPiCustomizeTableService.getById("tableId")).thenReturn(piCustomizeTable);

        // 模拟字段列表
        List<PiAllocationTableField> fieldList = new ArrayList<>();
        PiAllocationTableField field1 = new PiAllocationTableField();
        field1.setFieldName("fieldName1");
        field1.setIsQuery(1);
        field1.setIsList(1);
        field1.setSort(1.0);
        fieldList.add(field1);
        when(piAllocationTableFieldService.list(any(LambdaQueryWrapper.class))).thenReturn(fieldList);

        // 模拟数据总数
        when(piAllocationTableFieldMapper.getTableDataCount(anyMap())).thenReturn(1);

        // 模拟分页数据
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", 1L);
        dataMap.put("fieldName1", "value1");
        dataList.add(dataMap);
        when(piAllocationTableFieldMapper.getTableData(anyMap())).thenReturn(dataList);

        // 模拟标准数据
        List<PiStandardData> standardDataList = new ArrayList<>();
        PiStandardData standardData = new PiStandardData();
        standardData.setDataId("1");
        standardData.setManageId("manageId1");
        standardData.setStandardJson("[{\"maintenance_id\":\"maintenanceId1\"}]");
        standardDataList.add(standardData);
        when(iPiStandardDataService.list(any(LambdaQueryWrapper.class))).thenReturn(standardDataList);

        // 模拟标准维护数据
        List<PiStandardMaintenance> maintenanceList = new ArrayList<>();
        PiStandardMaintenance maintenance = new PiStandardMaintenance();
        maintenance.setId("maintenanceId1");
        maintenance.setFieldName("fieldName1");
        maintenance.setStandardValue(100.0);
        maintenanceList.add(maintenance);
        when(iPiStandardMaintenanceService.list(any(LambdaQueryWrapper.class))).thenReturn(maintenanceList);

        // 模拟5W数据
        List<FiveWhyAnalyse> fiveList = new ArrayList<>();
        FiveWhyAnalyse fiveWhyAnalyse = new FiveWhyAnalyse();
        fiveWhyAnalyse.setDataId("1");
        fiveWhyAnalyse.setFieldCode("fieldName1");
        fiveList.add(fiveWhyAnalyse);
        when(fiveWhyAnalyseService.list(any(LambdaQueryWrapper.class))).thenReturn(fiveList);

        // 调用被测方法
        Map<String, Object> result = piAllocationTableFieldService.queryData(pageNo, pageSize, req, piAllocationTableField);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.get("total"));
        List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
        assertNotNull(records);
        assertEquals(1, records.size());
        Map<String, Object> record = records.get(0);
        assertEquals("value1", record.get("fieldName1"));
        assertEquals(100.0, record.get("fieldName1_2"));
        assertEquals(-99.0, record.get("fieldName1_3"));
        assertTrue((Boolean) record.get("fieldName1_f"));
    }

    @Test
    void testQueryData_DataFormatting() {
        // 准备测试数据
        Integer pageNo = 1;
        Integer pageSize = 10;
        PiAllocationTableField piAllocationTableField = new PiAllocationTableField();
        piAllocationTableField.setTableId("tableId");
        piAllocationTableField.setAllocationTableId("allocationTableId");

        // 模拟 HttpServletRequest 参数
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("fieldName1", new String[]{"123.456"});
        when(req.getParameterMap()).thenReturn(parameterMap);

        // 模拟自定义报表
        PiCustomizeTable piCustomizeTable = new PiCustomizeTable();
        piCustomizeTable.setTableName("test_table");
        when(iPiCustomizeTableService.getById("tableId")).thenReturn(piCustomizeTable);

        // 模拟字段列表
        List<PiAllocationTableField> fieldList = new ArrayList<>();
        PiAllocationTableField field1 = new PiAllocationTableField();
        field1.setFieldName("fieldName1");
        field1.setIsQuery(1);
        field1.setIsList(1);
        field1.setSort(1.0);
        field1.setTakingValue("3"); // 四舍五入
        field1.setKeepDecimals(2); // 保留两位小数
        fieldList.add(field1);
        when(piAllocationTableFieldService.list(any(LambdaQueryWrapper.class))).thenReturn(fieldList);

        // 模拟数据总数
        when(piAllocationTableFieldMapper.getTableDataCount(anyMap())).thenReturn(1);

        // 模拟分页数据
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", 1L);
        dataMap.put("fieldName1", "123.456");
        dataList.add(dataMap);
        when(piAllocationTableFieldMapper.getTableData(anyMap())).thenReturn(dataList);

        // 模拟标准数据
        List<PiStandardData> standardDataList = new ArrayList<>();
        PiStandardData standardData = new PiStandardData();
        standardData.setDataId("1");
        standardData.setManageId("manageId1");
        standardData.setStandardJson("[{\"maintenance_id\":\"maintenanceId1\"}]");
        standardDataList.add(standardData);
        when(iPiStandardDataService.list(any(LambdaQueryWrapper.class))).thenReturn(standardDataList);

        // 模拟标准维护数据
        List<PiStandardMaintenance> maintenanceList = new ArrayList<>();
        PiStandardMaintenance maintenance = new PiStandardMaintenance();
        maintenance.setId("maintenanceId1");
        maintenance.setFieldName("fieldName1");
        maintenance.setStandardValue(100.0);
        maintenanceList.add(maintenance);
        when(iPiStandardMaintenanceService.list(any(LambdaQueryWrapper.class))).thenReturn(maintenanceList);

        // 模拟5W数据
        List<FiveWhyAnalyse> fiveList = new ArrayList<>();
        FiveWhyAnalyse fiveWhyAnalyse = new FiveWhyAnalyse();
        fiveWhyAnalyse.setDataId("1");
        fiveWhyAnalyse.setFieldCode("fieldName1");
        fiveList.add(fiveWhyAnalyse);
        when(fiveWhyAnalyseService.list(any(LambdaQueryWrapper.class))).thenReturn(fiveList);

        // 调用被测方法
        Map<String, Object> result = piAllocationTableFieldService.queryData(pageNo, pageSize, req, piAllocationTableField);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.get("total"));
        List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
        assertNotNull(records);
        assertEquals(1, records.size());
        Map<String, Object> record = records.get(0);
        assertEquals("123.46", record.get("fieldName1")); // 四舍五入保留两位小数
        assertEquals(100.0, record.get("fieldName1_2"));
        assertEquals(23.46, record.get("fieldName1_3"));
        assertTrue((Boolean) record.get("fieldName1_f"));
    }

    @Test
    void testQueryData_FiveWComparison() {
        // 准备测试数据
        Integer pageNo = 1;
        Integer pageSize = 10;
        PiAllocationTableField piAllocationTableField = new PiAllocationTableField();
        piAllocationTableField.setTableId("tableId");
        piAllocationTableField.setAllocationTableId("allocationTableId");

        // 模拟 HttpServletRequest 参数
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("fieldName1", new String[]{"value1"});
        when(req.getParameterMap()).thenReturn(parameterMap);

        // 模拟自定义报表
        PiCustomizeTable piCustomizeTable = new PiCustomizeTable();
        piCustomizeTable.setTableName("test_table");
        when(iPiCustomizeTableService.getById("tableId")).thenReturn(piCustomizeTable);

        // 模拟字段列表
        List<PiAllocationTableField> fieldList = new ArrayList<>();
        PiAllocationTableField field1 = new PiAllocationTableField();
        field1.setFieldName("fieldName1");
        field1.setIsQuery(1);
        field1.setIsList(1);
        field1.setSort(1.0);
        fieldList.add(field1);
        when(piAllocationTableFieldService.list(any(LambdaQueryWrapper.class))).thenReturn(fieldList);

        // 模拟数据总数
        when(piAllocationTableFieldMapper.getTableDataCount(anyMap())).thenReturn(1);

        // 模拟分页数据
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", 1L);
        dataMap.put("fieldName1", "value1");
        dataList.add(dataMap);
        when(piAllocationTableFieldMapper.getTableData(anyMap())).thenReturn(dataList);

        // 模拟标准数据
        List<PiStandardData> standardDataList = new ArrayList<>();
        PiStandardData standardData = new PiStandardData();
        standardData.setDataId("1");
        standardData.setManageId("manageId1");
        standardData.setStandardJson("[{\"maintenance_id\":\"maintenanceId1\"}]");
        standardDataList.add(standardData);
        when(iPiStandardDataService.list(any(LambdaQueryWrapper.class))).thenReturn(standardDataList);

        // 模拟标准维护数据
        List<PiStandardMaintenance> maintenanceList = new ArrayList<>();
        PiStandardMaintenance maintenance = new PiStandardMaintenance();
        maintenance.setId("maintenanceId1");
        maintenance.setFieldName("fieldName1");
        maintenance.setStandardValue(100.0);
        maintenanceList.add(maintenance);
        when(iPiStandardMaintenanceService.list(any(LambdaQueryWrapper.class))).thenReturn(maintenanceList);

        // 模拟5W数据
        List<FiveWhyAnalyse> fiveList = new ArrayList<>();
        FiveWhyAnalyse fiveWhyAnalyse = new FiveWhyAnalyse();
        fiveWhyAnalyse.setDataId("1");
        fiveWhyAnalyse.setFieldCode("fieldName1");
        fiveList.add(fiveWhyAnalyse);
        when(fiveWhyAnalyseService.list(any(LambdaQueryWrapper.class))).thenReturn(fiveList);

        // 调用被测方法
        Map<String, Object> result = piAllocationTableFieldService.queryData(pageNo, pageSize, req, piAllocationTableField);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.get("total"));
        List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
        assertNotNull(records);
        assertEquals(1, records.size());
        Map<String, Object> record = records.get(0);
        assertEquals("value1", record.get("fieldName1"));
        assertEquals(100.0, record.get("fieldName1_2"));
        assertEquals(-99.0, record.get("fieldName1_3"));
        assertTrue((Boolean) record.get("fieldName1_f"));
    }
}
