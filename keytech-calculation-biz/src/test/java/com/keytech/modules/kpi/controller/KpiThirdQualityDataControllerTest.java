package com.keytech.modules.kpi.controller;

import com.keytech.modules.kpi.entity.KpiThirdQualityData;
import com.keytech.modules.kpi.service.IKpiThirdQualityDataService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.GlobalErrorEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * KpiThirdQualityDataController 单元测试类
 * --story=1057339 --user=贺强 26-接入到KPI模块 https://www.tapd.cn/63554897/s/1389033
 */
class KpiThirdQualityDataControllerTest {

    @InjectMocks
    private KpiThirdQualityDataController controller;

    @Mock
    private IKpiThirdQualityDataService kpiThirdQualityDataService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试用例 TC001：传入 null 参数
     * 预期：不调用 service 方法，返回成功状态码
     */
    @Test
    void testQualityData_NullInput() {
        // 执行
        Result<String> result = controller.qualityData(null);

        // 验证
        verify(kpiThirdQualityDataService, never()).qualityData(any());
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
    }

    /**
     * 测试用例 TC002：传入非空参数
     * 预期：调用 service 方法一次，返回成功状态码
     */
    @Test
    void testQualityData_ValidInput() {
        // 准备数据
        KpiThirdQualityData data = new KpiThirdQualityData();
        data.setIndexCode("1");
        data.setOrgId("YJ");
        List list = List.of(data);
        // 模拟 service 方法行为
        doNothing().when(kpiThirdQualityDataService).qualityData(any(List.class));

        // 执行
        Result<String> result = controller.qualityData(list);

        // 验证
        verify(kpiThirdQualityDataService, times(1)).qualityData(any(List.class));
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(GlobalErrorEnum.SUCCESS.getCode(), result.getCode());
    }
}
