package com.keytech.modules.pi.service.impl;

import com.keytech.modules.pi.entity.PiSistarRtStepParams;
import com.keytech.modules.pi.entity.SourceSistarRtStepParams;
import com.keytech.modules.pi.service.IPiSistarRtStepLogService;
import com.keytech.modules.pi.service.IPiSistarRtStepsService;
import com.keytech.modules.pi.service.ISourceSistarRtStepParamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PiSistarRtStepParamsServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(PiSistarRtStepParamsServiceImplTest.class);

    @InjectMocks
    private PiSistarRtStepParamsServiceImpl piSistarRtStepParamsService;

    @Mock
    private IPiSistarRtStepLogService iPiSistarRtStepLogService;

    @Mock
    private ISourceSistarRtStepParamsService iSourceSistarRtStepParamsService;

    @Mock
    private IPiSistarRtStepsService iPiSistarRtStepsService;

    @BeforeEach
    void setUp() {
        // 设置tenantId字段值
        try {
            java.lang.reflect.Field tenantIdField = piSistarRtStepParamsService.getClass().getDeclaredField("tenantId");
            tenantIdField.setAccessible(true);
            tenantIdField.set(piSistarRtStepParamsService, 1);
        } catch (Exception e) {
            log.error("Failed to set tenantId field", e);
        }
    }

    @Test
    void testExtractData_NoLastLoadTime() {
        // Given
        when(iPiSistarRtStepLogService.getLastLoadTime("1")).thenReturn(null);

        // When
        piSistarRtStepParamsService.extractData();

        // Then
        verify(iPiSistarRtStepLogService).getLastLoadTime("1");
        verifyNoMoreInteractions(iSourceSistarRtStepParamsService);
        verifyNoMoreInteractions(iPiSistarRtStepsService);
    }

    @Test
    void testExtractData_NoData() {
        // Given
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        Timestamp lastLoadTime = new Timestamp(System.currentTimeMillis() - 3600000); // 1小时前
        when(iPiSistarRtStepLogService.getLastLoadTime("1")).thenReturn(lastLoadTime);
        when(iSourceSistarRtStepParamsService.selectIncrementalDataWithPaging(any(), any(), anyLong(), anyLong()))
                .thenReturn(new ArrayList<>());

        // When
        piSistarRtStepParamsService.extractData();

        // Then
        verify(iPiSistarRtStepLogService).getLastLoadTime("1");
        verify(iSourceSistarRtStepParamsService).selectIncrementalDataWithPaging(
                eq(lastLoadTime), any(), eq(0L), eq(5000L));
        verifyNoMoreInteractions(iPiSistarRtStepsService);
    }

    @Test
    void testExtractData_SinglePageData() {
        // Given
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        Timestamp lastLoadTime = new Timestamp(System.currentTimeMillis() - 3600000); // 1小时前
        when(iPiSistarRtStepLogService.getLastLoadTime("1")).thenReturn(lastLoadTime);

        // 创建测试数据
        List<SourceSistarRtStepParams> dataList = createTestData(100);
        when(iSourceSistarRtStepParamsService.selectIncrementalDataWithPaging(any(), any(), anyLong(), anyLong()))
                .thenReturn(dataList)
                .thenReturn(new ArrayList<>()); // 第二次调用返回空列表结束循环

        // Mock saveBatch方法
        try (MockedConstruction<PiSistarRtStepParams> ignored = Mockito.mockConstruction(PiSistarRtStepParams.class)) {
            doNothing().when(piSistarRtStepParamsService).saveBatch(anyList());
        }

        // When
        piSistarRtStepParamsService.extractData();

        // Then
        verify(iPiSistarRtStepLogService).getLastLoadTime("1");
        verify(iSourceSistarRtStepParamsService).selectIncrementalDataWithPaging(
                eq(lastLoadTime), any(), eq(0L), eq(5000L));
        verify(piSistarRtStepParamsService).saveBatch(anyList());
        verify(iPiSistarRtStepLogService).updateLastLoadTime(eq("1"), any(), eq(1));
        verifyNoMoreInteractions(iPiSistarRtStepsService);
    }

    @Test
    void testExtractData_MultiplePagesData() {
        // Given
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        Timestamp lastLoadTime = new Timestamp(System.currentTimeMillis() - 3600000); // 1小时前
        when(iPiSistarRtStepLogService.getLastLoadTime("1")).thenReturn(lastLoadTime);

        // 创建测试数据
        List<SourceSistarRtStepParams> page1Data = createTestData(5000);
        List<SourceSistarRtStepParams> page2Data = createTestData(3000);
        when(iSourceSistarRtStepParamsService.selectIncrementalDataWithPaging(any(), any(), eq(0L), anyLong()))
                .thenReturn(page1Data);
        when(iSourceSistarRtStepParamsService.selectIncrementalDataWithPaging(any(), any(), eq(5000L), anyLong()))
                .thenReturn(page2Data);
        when(iSourceSistarRtStepParamsService.selectIncrementalDataWithPaging(any(), any(), eq(10000L), anyLong()))
                .thenReturn(new ArrayList<>()); // 第三次调用返回空列表结束循环

        // Mock saveBatch方法
        try (MockedConstruction<PiSistarRtStepParams> ignored = Mockito.mockConstruction(PiSistarRtStepParams.class)) {
            doNothing().when(piSistarRtStepParamsService).saveBatch(anyList());
        }

        // When
        piSistarRtStepParamsService.extractData();

        // Then
        verify(iPiSistarRtStepLogService).getLastLoadTime("1");
        verify(iSourceSistarRtStepParamsService).selectIncrementalDataWithPaging(
                eq(lastLoadTime), any(), eq(0L), eq(5000L));
        verify(iSourceSistarRtStepParamsService).selectIncrementalDataWithPaging(
                eq(lastLoadTime), any(), eq(5000L), eq(5000L));
        verify(iSourceSistarRtStepParamsService).selectIncrementalDataWithPaging(
                eq(lastLoadTime), any(), eq(10000L), eq(5000L));
        verify(piSistarRtStepParamsService, times(2)).saveBatch(anyList());
        verify(iPiSistarRtStepLogService, times(2)).updateLastLoadTime(eq("1"), any(), eq(1));
        verifyNoMoreInteractions(iPiSistarRtStepsService);
    }

    @Test
    void testExtractData_ClearCache() {
        // Given
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        Timestamp lastLoadTime = new Timestamp(System.currentTimeMillis() - 3600000); // 1小时前
        when(iPiSistarRtStepLogService.getLastLoadTime("1")).thenReturn(lastLoadTime);

        // 创建测试数据，确保有至少10页数据触发缓存清理
        List<SourceSistarRtStepParams> pageData = createTestData(5000);
        when(iSourceSistarRtStepParamsService.selectIncrementalDataWithPaging(any(), any(), anyLong(), anyLong()))
                .thenReturn(pageData);
        // 第11次调用返回空列表结束循环
        when(iSourceSistarRtStepParamsService.selectIncrementalDataWithPaging(any(), any(), eq(50000L), anyLong()))
                .thenReturn(new ArrayList<>());

        // Mock saveBatch方法
        try (MockedConstruction<PiSistarRtStepParams> ignored = Mockito.mockConstruction(PiSistarRtStepParams.class)) {
            doNothing().when(piSistarRtStepParamsService).saveBatch(anyList());
        }

        // When
        piSistarRtStepParamsService.extractData();

        // Then
        verify(iPiSistarRtStepLogService).getLastLoadTime("1");
        // 验证调用了10次数据查询
        verify(iSourceSistarRtStepParamsService, times(11)).selectIncrementalDataWithPaging(any(), any(), anyLong(), anyLong());
        // 验证调用了10次保存
        verify(piSistarRtStepParamsService, times(10)).saveBatch(anyList());
        // 验证调用了缓存清理方法
        verify(iPiSistarRtStepsService).clearMyBatisCache();
        // 验证更新时间被调用10次
        verify(iPiSistarRtStepLogService, times(10)).updateLastLoadTime(eq("1"), any(), eq(1));
    }

    /**
     * 创建测试数据
     *
     * @param count 数据条数
     * @return 测试数据列表
     */
    private List<SourceSistarRtStepParams> createTestData(int count) {
        List<SourceSistarRtStepParams> dataList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            SourceSistarRtStepParams params = new SourceSistarRtStepParams();
            params.setId(i + 1);
            params.setParamName("param" + i);
            params.setInsertTimestamp(new Date(System.currentTimeMillis() - 3600000 + i * 1000)); // 1小时前加i秒
            dataList.add(params);
        }
        return dataList;
    }
}
