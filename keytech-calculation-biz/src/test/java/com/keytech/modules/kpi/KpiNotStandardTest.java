package com.keytech.modules.kpi;

import com.keytech.modules.fivewhy.entity.FiveWhyAnalyse;
import com.keytech.modules.kpi.service.IKpiThirdPlayDataService;
import com.keytech.modules.kpi.service.impl.KpiThirdPlayDataServiceImpl;
import org.jeecg.modules.system.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @FileName KpiNotStandardTest
 * @Description 不合格推送测试
 * @Author HQ
 * @date 2025-07-31
 **/
public class KpiNotStandardTest {
    @Autowired
    private IKpiThirdPlayDataService kpiThirdPlayDataService;

    //--story=1055162 --user=贺强 21-推送程序开发 https://www.tapd.cn/63554897/s/1384694
    @Test
    public void push() {
        //推送程序
        Long analyseId = Long.parseLong("1384694213232132132");
        List<Long> list = new ArrayList<>();
        list.add(analyseId);
        kpiThirdPlayDataService.kpiNotStandard(list);

    }

    //--story=1055164 --user=贺强 21-推送报表开发 https://www.tapd.cn/63554897/s/1384696
    @Test
    public void pushJob() {
        //定时推送
        kpiThirdPlayDataService.notStandardKpiFwhyJob();
    }

    //--story=1055161 --user=贺强 21-5why标准对接 https://www.tapd.cn/63554897/s/1384700
    @Test
    public void docking() {
        //获取标准集合
        FiveWhyAnalyse fiveWhyAnalyse = new FiveWhyAnalyse();
        Map<String, SysUser > userMap = new HashMap<>();
        new KpiThirdPlayDataServiceImpl().getKpiDataList(fiveWhyAnalyse,  userMap, "1");
    }

}
