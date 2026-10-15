package com.keytech.modules.ems.job;

import com.keytech.modules.calculation.KeytechCalculationApplication;
import com.keytech.modules.df.mdm.job.SyncDfMdmDataJob;
import com.xxl.job.core.biz.model.ReturnT;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = KeytechCalculationApplication.class)
@RunWith(SpringRunner.class)
class SyncDfMdmDataJobTest {

    @Autowired
    private SyncDfMdmDataJob syncDfMdmDataJob;

    /**
     * 同步指标数据
     */
    @Test
    void testSyncMdmIndicatorJob() {
        ReturnT<String> result = syncDfMdmDataJob.doSyncMdmIndicatorJob("1006,INST_IND,500,2025-11-01");
        System.out.println(result);
        assertNotNull(result);
    }

    /**
     * 同步配方数据
     */
    @Test
    void testSyncMdmRecipeJob() {
        ReturnT<String> result = syncDfMdmDataJob.doSyncMdmRecipeJob("1006,FAC_BASIC_INFO_RECIPE,500,2025-11-01");
        System.out.println(result);
        assertNotNull(result);
    }

    /**
     * 同步报警点位数据
     */
    @Test
    void testSyncMdmAlarmtagJob() {
        ReturnT<String> result = syncDfMdmDataJob.doSyncMdmAlarmtagJob("1006,TAG_DICTIONARY,500,2022-12-01");
        System.out.println(result);
        assertNotNull(result);
    }


}
