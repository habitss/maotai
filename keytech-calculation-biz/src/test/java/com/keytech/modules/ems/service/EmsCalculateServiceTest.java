package com.keytech.modules.ems.service;

import com.alibaba.fastjson.JSON;
import com.keytech.modules.ems.constants.GrainTypeEnum;
import com.keytech.modules.ems.entity.CollectPointStatus;
import com.keytech.modules.ems.mapper.CollectPointStatusMapper;
import com.keytech.modules.ems.service.impl.CollectPointStatusServiceImpl;
import com.keytech.modules.ems.service.impl.RefValueUsageCalculator;
import com.keytech.modules.ems.vo.CalcUsageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 能源模块 计算业务测试类
 * @author yepeng
 * @since 2025.05.11
 */
@ExtendWith(MockitoExtension.class)
class EmsCalculateServiceTest {

    @Mock
    private CollectPointStatusMapper collectPointStatusMapper;

    @InjectMocks
    private RefValueUsageCalculator refValueUsageCalculator;

    @InjectMocks
    private CollectPointStatusServiceImpl collectPointStatusService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(collectPointStatusService, "baseMapper", collectPointStatusMapper);
    }

    @Test
    void testCalculateUsage() {
        List<Double> values = Arrays.asList(17.1, 18.2, 19.4, 20.6, 999999.1, 21.4, 23.2, 25.1);
        CalcUsageResult result = refValueUsageCalculator.calculateUsage(values, BigDecimal.valueOf(10.0));
        System.out.println(JSON.toJSONString(result));
        assertNotNull(result);
    }

    @Test
    void testQueryPageList() {
        List<CollectPointStatus> list = collectPointStatusService.queryPendingForPeriodic(GrainTypeEnum.HOUR.getVal());
        System.out.println(list);
        assertNotNull(list);
    }

}