package com.keytech.modules.pi.controller;

import com.keytech.modules.pi.dto.PiGreenDataDto;
import com.keytech.modules.pi.service.IPiStandardDataService;
import org.jeecg.common.api.vo.Result;
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
 * --story=1060125 --user=孙金伟 22-绿区率异常分析分类统计 https://www.tapd.cn/63554897/s/1400481 单元测试
 * --story=1060124 --user=孙金伟 21-绿区率统计柱状图 https://www.tapd.cn/63554897/s/1400690 单元测试
 */
class PiConsistencyIndexControllerTest {

    @InjectMocks
    private PiConsistencyIndexController controller; // 被测控制器实例

    @Mock
    private IPiStandardDataService piStandardDataService; // Mock的服务层依赖

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // 初始化Mockito注解
    }

    /**
     * --story=1061519 --user=孙金伟 31-功能-绿区率统计调整为按部门、产线、PI来查询 https://www.tapd.cn/63554897/s/1404503 单元测试
     */
    @Test
    void testGetPayCloseAttentionPi_BothNull_EmptyList() {
        // 模拟服务层返回空列表
        when(piStandardDataService.getPayCloseAttentionPi(null, null, null, anyString(), anyString()))
                .thenReturn(new ArrayList<>());

        // 调用被测方法
        Result<List<PiGreenDataDto>> result = controller.getPayCloseAttentionPi(null, null);

        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertTrue(result.getResult().isEmpty());

        // 验证服务层方法被调用了一次
        verify(piStandardDataService, times(1))
                .getPayCloseAttentionPi(null, null, null, anyString(), anyString());
    }

    /**
     * 测试用例：TC004 - factoryCode 和 deptCode 都有值，服务层返回空列表
     */
    @Test
    void testGetPayCloseAttentionPi_BothNotNull_EmptyList() {
        String factoryCode = "F001";
        String deptCode = "D001";

        when(piStandardDataService.getPayCloseAttentionPi(factoryCode, deptCode, null, anyString(), anyString()))
                .thenReturn(new ArrayList<>());

        Result<List<PiGreenDataDto>> result = controller.getPayCloseAttentionPi(factoryCode, deptCode);

        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertTrue(result.getResult().isEmpty());

        verify(piStandardDataService, times(1))
                .getPayCloseAttentionPi(factoryCode, deptCode, anyString(), anyString(), anyString());
    }

    /**
     * 测试用例：TC005 - factoryCode 和 deptCode 都有值，服务层返回非空列表
     */
    @Test
    void testGetPayCloseAttentionPi_BothNotNull_NonEmptyList() {
        String factoryCode = "F001";
        String deptCode = "D001";

        // 构造返回数据
        List<PiGreenDataDto> mockData = new ArrayList<>();
        PiGreenDataDto dto = new PiGreenDataDto();
        dto.setAll("100");
        dto.setGreen("80");
        mockData.add(dto);

        when(piStandardDataService.getPayCloseAttentionPi(factoryCode, deptCode, anyString(), anyString(), anyString()))
                .thenReturn(mockData);

        Result<List<PiGreenDataDto>> result = controller.getPayCloseAttentionPi(factoryCode, deptCode);

        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertFalse(result.getResult().isEmpty());
        assertEquals(1, result.getResult().size());
        assertEquals("100", result.getResult().get(0).getAll());
        assertEquals("80", result.getResult().get(0).getGreen());

        verify(piStandardDataService, times(1))
                .getPayCloseAttentionPi(factoryCode, deptCode, anyString(), anyString(), anyString());
    }
}
