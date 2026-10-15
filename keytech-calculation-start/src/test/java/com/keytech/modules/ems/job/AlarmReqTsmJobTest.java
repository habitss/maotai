package com.keytech.modules.ems.job;

import com.keytech.modules.alarm.job.AlarmReqTsmJobHandler;
import com.keytech.modules.calculation.KeytechCalculationApplication;
import com.xxl.job.core.biz.model.ReturnT;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = KeytechCalculationApplication.class)
@RunWith(SpringRunner.class)
class AlarmReqTsmJobTest {

    @Autowired
    private AlarmReqTsmJobHandler alarmReqTsmJobHandler;

    /**
     * 给TSM发送报警事实请求
     * @since 2026.01.29
     */
    @Test
    void testPushAlarmToTsmJob() {
        ReturnT<String> result = alarmReqTsmJobHandler.handlePushAlarmToTsmJob(null);
        System.out.println(result);
        assertNotNull(result);
    }

    /**
     * 请求TSM查询报警事实的处理结果
     * @since 2026.01.29
     */
    @Test
    void testQueryAlarmResultFromTsmJob() {
        ReturnT<String> result = alarmReqTsmJobHandler.handleQueryAlarmTsmStateJob(null);
        System.out.println(result);
        assertNotNull(result);
    }

}
