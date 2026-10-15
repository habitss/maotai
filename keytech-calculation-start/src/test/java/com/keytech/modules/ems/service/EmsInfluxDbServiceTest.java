package com.keytech.modules.ems.service;

import com.keytech.modules.calculation.KeytechCalculationApplication;
import com.keytech.modules.ems.service.impl.EmsInfluxDbService;
import com.keytech.modules.ems.vo.FluxZone8Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 能源模块 InfluxDB 业务类 测试类
 * @author yepeng
 * @since 2025.05.07
 */
@SpringBootTest(classes = KeytechCalculationApplication.class)
@RunWith(SpringRunner.class)
class EmsInfluxDbServiceTest {

    @Autowired
    private EmsInfluxDbService emsInfluxDbService;

    //公共参数
    private List<String> pointList;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        this.pointList = Arrays.asList("1.13.102.80.3.1-1.0.18-59.233");
        this.startTime = LocalDate.now().atStartOfDay().withHour(8);
        this.startTime = LocalDate.now().atStartOfDay().withHour(9);
    }

    @Test
    void queryMultiPointsWithIntervalLast() {
        List<FluxZone8Data> resultList =emsInfluxDbService.queryMultiPointsWithIntervalLast(this.pointList, startTime, endTime, 300);
        System.out.println(resultList);
        assertNotNull(resultList);
    }

    @Test
    void queryMultiPointsWithIntervalLastMap() {
        Map<String, List<FluxZone8Data>> resultMap =emsInfluxDbService.queryMultiPointsWithIntervalLastMap(this.pointList, startTime, endTime, 300);
        System.out.println(resultMap);
        assertNotNull(resultMap);
    }

}