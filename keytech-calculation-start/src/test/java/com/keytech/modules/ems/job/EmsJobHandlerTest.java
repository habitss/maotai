package com.keytech.modules.ems.job;

import com.keytech.modules.calculation.KeytechCalculationApplication;
import com.xxl.job.core.biz.model.ReturnT;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * EMS任务调度 测试类
 * @author yepeng
 * @since 2025.05.06
 */
@SpringBootTest(classes = KeytechCalculationApplication.class)
@RunWith(SpringRunner.class)
class EmsJobHandlerTest {

    @Autowired
    private InitCollectPointStatusJob initCollectPointStatusJob;
    @Autowired
    private CollectorJobHandler collectorJobHandler;
    @Autowired
    private RetryJobHandler retryJobHandler;
    @Autowired
    private ManualBackfillJobHandler manualBackfillHandler;

    /**
     * 初始化点位状态
     * @since 2025.05.06
     */
    @Test
    void testInitHourCollectPointStatus() {
        ReturnT<String> result = initCollectPointStatusJob.initCollectPointStatus("1");
        System.out.println(result);
        assertNotNull(result);
    }

    /**
     * 采集小时点位
     * @since 2025.05.06
     */
    @Test
    void testCollectHourPointJob() {
        ReturnT<String> result = collectorJobHandler.collectHourPointJob("");
        System.out.println(result);
        assertNotNull(result);
    }

    /**
     * 自动重试采集
     * @since 2025.05.06
     */
    @Test
    void testRetryFailedCollectJob() {
        ReturnT<String> result = retryJobHandler.retryFailedCollectJob("");
        System.out.println(result);
        assertNotNull(result);
    }

    /**
     * 补采人工设置点位
     * @since 2025.05.06
     */
    @Test
    void testManualBackfillCollectJob() {
        ReturnT<String> result = manualBackfillHandler.manualBackfillCollectJob("");
        System.out.println(result);
        assertNotNull(result);
    }

}