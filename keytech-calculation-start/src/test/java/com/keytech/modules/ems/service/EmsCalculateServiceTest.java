package com.keytech.modules.ems.service;

import com.keytech.modules.calculation.KeytechCalculationApplication;
import com.keytech.modules.ems.constants.GrainTypeEnum;
import com.keytech.modules.ems.entity.CollectPointStatus;
import com.keytech.modules.ems.service.impl.EmsCalculateService;
import com.keytech.modules.ems.service.impl.RefValueUsageCalculator;
import com.keytech.modules.ems.vo.CalcUsageResult;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 能源计算模块 测试类
 * @author yepeng
 * @since 2025.05.06
 */
@SpringBootTest(classes = KeytechCalculationApplication.class)
@RunWith(SpringRunner.class)
class EmsCalculateServiceTest {

    @Autowired
    private EmsCalculateService emsCalculateService;
    @Autowired
    private RefValueUsageCalculator refValueUsageCalculator;
    @Autowired
    private ICollectPointStatusService collectPointStatusService;

    @Test
    void testCalculateUsage() {
        List<Double> values = Arrays.asList(17.1, 18.2, 19.4, 20.6, 999999.1, 21.4, 23.2, 25.1);
        CalcUsageResult result = refValueUsageCalculator.calculateUsage(values, new BigDecimal("20.0"));
        System.out.println(result);
        assertNotNull(result);
    }

    @Test
    void testQueryPendingForPeriodic() {
        List<CollectPointStatus> list = collectPointStatusService.queryPendingForPeriodic(GrainTypeEnum.HOUR.getVal());
        System.out.println(list);
        assertNotNull(list);
    }

    @Test
    void testQueryPendingForRetry() {
        List<CollectPointStatus> list = collectPointStatusService.queryPendingForRetry(3, null, 48);
        System.out.println(list);
        assertNotNull(list);
    }

    @Test
    void testQueryPendingForBackfill() {
        List<CollectPointStatus> list = collectPointStatusService.queryPendingForBackfill(null, null, LocalDateTime.now(), null);
        System.out.println(list);
        assertNotNull(list);
    }

}