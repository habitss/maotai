package com.keytech.modules.action.plan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.modules.action.plan.dto.ActionPlanSummaryChartDto;
import com.keytech.modules.action.plan.dto.ActionPlanSummaryListDto;
import com.keytech.modules.action.plan.dto.ActionPlanSummaryQueryDto;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.action.plan.mapper.ActionPlanTrackMapper;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActionPlanTrackServiceImplTest {

    @InjectMocks
    private ActionPlanTrackServiceImpl actionPlanTrackService;

    @Mock
    private ISysUserService sysUserService;

    @Mock
    private ISysDepartService sysDepartService;

    @Mock
    private ActionPlanTrackMapper actionPlanTrackMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testQueryIndexSummaryData_withValidDto() {
        // Arrange
        WSActionStatisticsDto dto = new WSActionStatisticsDto();
        dto.setWorkShopCode("WS001");
        dto.setFactory("F001");
        dto.setDutUserId("U001");
        dto.setDeptId("D001");
        dto.setFilterTime("2024-01-01");
        dto.setOpTime("2024-01-01");
        dto.setEdTime("2024-01-31");

        // Mock allUserMap
        SysUser user = new SysUser();
        user.setId("U001");
        user.setRealname("张三");
        when(sysUserService.list(any(QueryWrapper.class))).thenReturn(Collections.singletonList(user));

        // Mock allDeptMap
        SysDepart dept = new SysDepart();
        dept.setId("D001");
        dept.setDepartName("研发部");
        when(sysDepartService.list()).thenReturn(Collections.singletonList(dept));

        // Mock department tree
        SysDepartTreeModel treeModel = new SysDepartTreeModel();
        treeModel.setId("D001");
        treeModel.setTitle("研发部");
        treeModel.setOrgCode("OC001");
        when(sysDepartService.queryTreeList()).thenReturn(Collections.singletonList(treeModel));

        // Mock processingSummaryQuery
        List<ActionPlanTrack> trackList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ActionPlanTrack track = new ActionPlanTrack();
            track.setId(i + 1);
            track.setFinishState("1"); // 已完成
            track.setDutyDeptId("D001");
            trackList.add(track);
        }
        doReturn(trackList).when(actionPlanTrackService).processingSummaryQuery(any(ActionPlanSummaryQueryDto.class));

        // Mock getSummaryListData
        ActionPlanSummaryListDto listDto = new ActionPlanSummaryListDto();
        listDto.setActionTotal(5);
        listDto.setFinishedStateTotal(5);
        listDto.setFinishedStateRatio(100.0);
        listDto.setDelayStateTotal(0);
        listDto.setProgressStateTotal(0);
        listDto.setCloseStateTotal(0);
        listDto.setDelayFinishStateTotal(0);
        listDto.setOneDelayTotal(0);
        listDto.setTwoDelayTotal(0);
        listDto.setThreeDelayTotal(0);
        listDto.setDelayTotal(0);
        listDto.setDelayRatio(0.0);
        listDto.setPutKindSum("0");
        listDto.setDutKindSum("0");

        ActionPlanSummaryListDto childDto = new ActionPlanSummaryListDto();
        childDto.setId("D001");
        childDto.setTitle("研发部");
        childDto.setActionTotal(5);
        childDto.setFinishedStateTotal(5);
        childDto.setFinishedStateRatio(100.0);
        childDto.setDelayStateTotal(0);
        childDto.setProgressStateTotal(0);
        childDto.setCloseStateTotal(0);
        childDto.setDelayFinishStateTotal(0);
        childDto.setOneDelayTotal(0);
        childDto.setTwoDelayTotal(0);
        childDto.setThreeDelayTotal(0);
        childDto.setDelayTotal(0);
        childDto.setDelayRatio(0.0);
        childDto.setPutKindSum("0");
        childDto.setDutKindSum("0");

        listDto.setChildren(Collections.singletonList(childDto));
        doReturn(listDto).when(actionPlanTrackService).getSummaryListData(anyInt(), anyList(), any(ActionPlanSummaryQueryDto.class), anyMap(), anyMap());

        // Mock getSummaryChartData
        ActionPlanSummaryChartDto chartDto = new ActionPlanSummaryChartDto();
        chartDto.setFinishedTotal(5);
        chartDto.setProgressTotal(0);
        chartDto.setDelayTotal(0);
        doReturn(chartDto).when(actionPlanTrackService).getSummaryChartData(anyList(), any(ActionPlanSummaryQueryDto.class), anyMap(), anyMap());

        // Act
        Map<String, Object> result = actionPlanTrackService.queryIndexSummaryData(dto);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("planCompletion"));
        assertTrue(result.containsKey("行动计划"));
        assertTrue(result.containsKey("individualPlan"));

        // Verify
        verify(sysUserService, times(1)).list(any(QueryWrapper.class));
        verify(sysDepartService, times(1)).list();
        verify(sysDepartService, times(1)).queryTreeList();
    }

    @Test
    void testQueryIndexSummaryData_withNullDto() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            actionPlanTrackService.queryIndexSummaryData(null);
        });
    }

    @Test
    void testQueryIndexSummaryData_withEmptyActionPlanTrackList() {
        // Arrange
        WSActionStatisticsDto dto = new WSActionStatisticsDto();
        dto.setWorkShopCode("WS001");
        dto.setFactory("F001");
        dto.setDutUserId("U001");
        dto.setDeptId("D001");
        dto.setFilterTime("2024-01-01");
        dto.setOpTime("2024-01-01");
        dto.setEdTime("2024-01-31");

        // Mock empty list
        doReturn(Collections.emptyList()).when(actionPlanTrackService).processingSummaryQuery(any(ActionPlanSummaryQueryDto.class));

        // Act
        Map<String, Object> result = actionPlanTrackService.queryIndexSummaryData(dto);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("planCompletion"));
        assertTrue(result.containsKey("行动计划"));
        assertTrue(result.containsKey("individualPlan"));
    }
}
