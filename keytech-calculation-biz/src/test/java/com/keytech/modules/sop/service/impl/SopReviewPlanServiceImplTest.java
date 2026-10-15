package com.keytech.modules.sop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.sop.mapper.SopReviewPlanMapper;
import com.keytech.modules.sop.service.ISopStatisticDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class SopReviewPlanServiceImplTest {

    @InjectMocks
    private SopReviewPlanServiceImpl sopReviewPlanService;

    @Mock
    private ISopStatisticDataService sopStatisticDataService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //--story=1055311 --user=孙金伟 23-岗位记录、检查清单优化处理 https://www.tapd.cn/63554897/s/1383210 单元测试
    @Test
    void testSopSubmit_IdIsNull() {
        // Arrange
        List<JSONObject> input = new ArrayList<>();
        JSONObject data = new JSONObject();
        data.put("year", 2024);
        input.add(data);

        List<MdmWeek> weekList = new ArrayList<>();
        MdmWeek week = new MdmWeek();
        week.setWeekRangeStr("2024-01");
        weekList.add(week);

        when(sopStatisticDataService.getWeek(2024)).thenReturn(weekList);

        // Act
        sopReviewPlanService.sopSubmit(input);

        // Assert
        JSONObject resultData = input.get(0).getJSONObject("resultData");
        assertNotNull(resultData);
        assertTrue(resultData.containsKey("2024-01"));
        assertEquals(0, resultData.getIntValue("2024-01"));
    }

}
