package com.keytech.modules.kpi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.fivewhy.entity.FiveWhyAnalyse;
import com.keytech.modules.kpi.dto.KpiFwhyDto;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiThirdPlayData;
import com.keytech.modules.kpi.service.IKpiInfoService;
import com.keytech.modules.kpi.service.IKpiThirdPlayDataService;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.kpi.util.StringHandleUtils;
import com.keytech.modules.kpi.util.WeekUtils;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class KpiThirdPlayDataServiceImplTest {

    @InjectMocks
    private KpiThirdPlayDataServiceImpl kpiThirdPlayDataService;

    @Mock
    private IKpiInfoService iKpiInfoService;

    @Mock
    private IMdmWeekService mdmWeekService;

    @Mock
    private IKpiThirdPlayDataService thirdPlayDataService;

    @Mock
    private IActionPlanTrackService iActionPlanTrackService;

    @Mock
    private ISysUserService iSysUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetKpiDataList_KpiInfoEmpty() {
        FiveWhyAnalyse fiveWhyAnalyse = new FiveWhyAnalyse();
        fiveWhyAnalyse.setKpiCode("testKpiCode");
        Map<String, SysUser> userMap = new HashMap<>();
        String type = "1";

        when(iKpiInfoService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        KpiFwhyDto result = kpiThirdPlayDataService.getKpiDataList(fiveWhyAnalyse, userMap, type);

        assertNull(result);
    }

    @Test
    void testGetKpiDataList_KpiThirdPlayDataEmpty() {
        FiveWhyAnalyse fiveWhyAnalyse = new FiveWhyAnalyse();
        fiveWhyAnalyse.setKpiCode("testKpiCode");
        fiveWhyAnalyse.setKpiColKey("w_2023_10");
        Map<String, SysUser> userMap = new HashMap<>();
        String type = "1";

        KpiInfo kpiInfo = new KpiInfo();
        kpiInfo.setThirdName("testThirdName");
        when(iKpiInfoService.list(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(kpiInfo));

        try (MockedStatic<WeekUtils> mockedWeekUtils = mockStatic(WeekUtils.class)) {
            mockedWeekUtils.when(() -> WeekUtils.extractYear("w_2023_10")).thenReturn("2023");
            mockedWeekUtils.when(() -> WeekUtils.extractWeekNum("w_2023_10")).thenReturn(10);

            Map<Integer, String> weekMap = new HashMap<>();
            weekMap.put(10, "2023-03-06|2023-03-12");
            when(mdmWeekService.getWeekOfYear("2023")).thenReturn(weekMap);

            when(thirdPlayDataService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            KpiFwhyDto result = kpiThirdPlayDataService.getKpiDataList(fiveWhyAnalyse, userMap, type);

            assertNull(result);
        }
    }

    @Test
    void testGetKpiDataList_WeekData() {
        FiveWhyAnalyse fiveWhyAnalyse = new FiveWhyAnalyse();
        fiveWhyAnalyse.setKpiCode("testKpiCode");
        fiveWhyAnalyse.setKpiColKey("w_2023_10");
        fiveWhyAnalyse.setProblemDesc("testProblemDesc");
        fiveWhyAnalyse.setProblem("testProblem");
        fiveWhyAnalyse.setCreateBy("12345678");
        fiveWhyAnalyse.setReasonJson("[]");
        fiveWhyAnalyse.setMergeRule("[]");
        fiveWhyAnalyse.setCreateTime(new Date());
        fiveWhyAnalyse.setId("testId");

        Map<String, SysUser> userMap = new HashMap<>();
        SysUser sysUser = new SysUser();
        sysUser.setUsername("12345678");
        userMap.put("12345678", sysUser);

        String type = "1";

        KpiInfo kpiInfo = new KpiInfo();
        kpiInfo.setThirdName("testThirdName");
        when(iKpiInfoService.list(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(kpiInfo));

        try (MockedStatic<WeekUtils> mockedWeekUtils = mockStatic(WeekUtils.class);
             MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class);
             MockedStatic<StringHandleUtils> mockedStringUtils = mockStatic(StringHandleUtils.class)) {

            mockedWeekUtils.when(() -> WeekUtils.extractYear("w_2023_10")).thenReturn("2023");
            mockedWeekUtils.when(() -> WeekUtils.extractWeekNum("w_2023_10")).thenReturn(10);

            Map<Integer, String> weekMap = new HashMap<>();
            weekMap.put(10, "2023-03-06|2023-03-12");
            when(mdmWeekService.getWeekOfYear("2023")).thenReturn(weekMap);

            KpiThirdPlayData kpiThirdPlayData = new KpiThirdPlayData();
            kpiThirdPlayData.setIndocno(123);
            kpiThirdPlayData.setStitle("testStitle");
            kpiThirdPlayData.setIlibraryid("testIlibraryid");
            kpiThirdPlayData.setDatekpi(new Date());
            kpiThirdPlayData.setState("5");
            kpiThirdPlayData.setId("testThirdPlayId");
            when(thirdPlayDataService.list(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(kpiThirdPlayData));

            mockedDateUtils.when(() -> DateUtils.formatDateTime(any(Date.class))).thenReturn("2023-03-12 00:00:00");
            mockedStringUtils.when(() -> StringHandleUtils.removeAllLineBreaks(anyString())).thenReturn("test");

            when(iActionPlanTrackService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            KpiFwhyDto result = kpiThirdPlayDataService.getKpiDataList(fiveWhyAnalyse, userMap, type);

            assertNotNull(result);
            assertEquals(Long.valueOf(123), result.getKpiResultId());
            assertEquals(0, result.getIdel());
            assertEquals("testProblemDesc", result.getDescription());
            assertEquals("testProblem", result.getHeadline());
            assertEquals("12345678", result.getWorkno());
            assertEquals("1", result.getItype());
            assertEquals("testId", result.getAnalyseId());
            assertEquals("testKpiCode", result.getKpiCode());
            assertEquals("testThirdPlayId", result.getThirdPlayId());
            assertEquals("testStitle", result.getStitle());
            assertEquals("testIlibraryid", result.getIlibraryId());
            assertEquals(5, result.getState());
        }
    }

    @Test
    void testGetKpiDataList_MonthData() {
        FiveWhyAnalyse fiveWhyAnalyse = new FiveWhyAnalyse();
        fiveWhyAnalyse.setKpiCode("testKpiCode");
        fiveWhyAnalyse.setKpiColKey("2023-03");
        fiveWhyAnalyse.setProblemDesc("testProblemDesc");
        fiveWhyAnalyse.setProblem("testProblem");
        fiveWhyAnalyse.setCreateBy("12345678");
        fiveWhyAnalyse.setReasonJson("[]");
        fiveWhyAnalyse.setMergeRule("[]");
        fiveWhyAnalyse.setCreateTime(new Date());
        fiveWhyAnalyse.setId("testId");

        Map<String, SysUser> userMap = new HashMap<>();
        SysUser sysUser = new SysUser();
        sysUser.setUsername("12345678");
        userMap.put("12345678", sysUser);

        String type = "1";

        KpiInfo kpiInfo = new KpiInfo();
        kpiInfo.setThirdName("testThirdName");
        when(iKpiInfoService.list(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(kpiInfo));

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class);
             MockedStatic<StringHandleUtils> mockedStringUtils = mockStatic(StringHandleUtils.class)) {

            mockedDateUtils.when(() -> DateUtils.getLastDayOfMonth("2023-03")).thenReturn("2023-03-31");
            mockedDateUtils.when(() -> DateUtils.formatDateTime(any(Date.class))).thenReturn("2023-03-31 00:00:00");
            mockedStringUtils.when(() -> StringHandleUtils.removeAllLineBreaks(anyString())).thenReturn("test");

            KpiThirdPlayData kpiThirdPlayData = new KpiThirdPlayData();
            kpiThirdPlayData.setIndocno(123);
            kpiThirdPlayData.setStitle("testStitle");
            kpiThirdPlayData.setIlibraryid("testIlibraryid");
            kpiThirdPlayData.setDatekpi(new Date());
            kpiThirdPlayData.setState("6");
            kpiThirdPlayData.setId("testThirdPlayId");
            when(thirdPlayDataService.list(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(kpiThirdPlayData));

            when(iActionPlanTrackService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            KpiFwhyDto result = kpiThirdPlayDataService.getKpiDataList(fiveWhyAnalyse, userMap, type);

            assertNotNull(result);
            assertEquals(Long.valueOf(123), result.getKpiResultId());
            assertEquals(0, result.getIdel());
            assertEquals("testProblemDesc", result.getDescription());
            assertEquals("testProblem", result.getHeadline());
            assertEquals("12345678", result.getWorkno());
            assertEquals("1", result.getItype());
            assertEquals("testId", result.getAnalyseId());
            assertEquals("testKpiCode", result.getKpiCode());
            assertEquals("testThirdPlayId", result.getThirdPlayId());
            assertEquals("testStitle", result.getStitle());
            assertEquals("testIlibraryid", result.getIlibraryId());
            assertEquals(6, result.getState());
        }
    }
}