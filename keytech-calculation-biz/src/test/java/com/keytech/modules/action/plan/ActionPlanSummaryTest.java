package com.keytech.modules.action.plan;

import cn.hutool.core.date.DateUtil;
import com.keytech.modules.action.plan.dto.ActionPlanSummaryChartDto;
import com.keytech.modules.action.plan.dto.ActionPlanSummaryListDto;
import com.keytech.modules.action.plan.dto.ActionPlanSummaryQueryDto;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.action.plan.service.impl.ActionPlanTrackServiceImpl;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @Description: --story=1050401 --user=孙金伟 03-行动计划-05-车间行动计划汇总 https://www.tapd.cn/63554897/s/1343753 单元测试
 * @Author: sjw
 * @Date: 2025/2/8 11:43
 */
@ExtendWith(MockitoExtension.class)
public class ActionPlanSummaryTest {

    @Mock
    private ISysDepartService sysDepartService;

    @Mock
    private ISysUserService sysUserService;

    @InjectMocks
    private ActionPlanTrackServiceImpl actionPlanTrackService;

    private ActionPlanSummaryQueryDto queryDto;

    @BeforeEach
    public void setUp() {
        queryDto = new ActionPlanSummaryQueryDto();
        queryDto.setDateType("1");
        queryDto.setMonthField(DateUtil.format(new Date(), "yyyy-MM"));
        queryDto.setDeptIds("1,2,3");
        queryDto.setProblemTypeIds("1,2,3");
        queryDto.setUserOrDept("1");
        queryDto.setDutyOrPut("1");
    }

    @Test
    public void testGetSummaryData_withValidData() {
        // Mock the data
        List<ActionPlanTrack> actionPlanTrackList = new ArrayList<>();
        ActionPlanTrack track1 = new ActionPlanTrack();
        track1.setFinishState("0");
        track1.setDutyDeptId("1");
        track1.setPutDeptId("1");
        track1.setProblemTypeId("1");
        actionPlanTrackList.add(track1);

        ActionPlanSummaryChartDto chartDto = new ActionPlanSummaryChartDto();
        ActionPlanSummaryListDto listDto = new ActionPlanSummaryListDto();
        List<ActionPlanSummaryListDto> children = new ArrayList<>();
        ActionPlanSummaryListDto childDto = new ActionPlanSummaryListDto();
        childDto.setTitle("Dept 1");
        children.add(childDto);
        listDto.setChildren(children);

        chartDto.setObjectList(Collections.singletonList(Collections.singletonMap("Dept 1", chartDto)));

        Map<String, Object> result = new HashMap<>();
        result.put("chartData", chartDto);
        result.put("summaryListData", listDto);

        // Mock the behavior
        when(sysDepartService.queryTreeList()).thenReturn(Collections.singletonList(new SysDepartTreeModel()));
        when(sysDepartService.queryTreeList().get(0).getChildren().get(0).getChildren().get(0).getChildren()).thenReturn(Collections.singletonList(new SysDepartTreeModel()));
        when(sysUserService.list()).thenReturn(Collections.singletonList(new SysUser()));
        when(actionPlanTrackService.list()).thenReturn(actionPlanTrackList);
        when(actionPlanTrackService.processingSummaryQuery(queryDto)).thenReturn(actionPlanTrackList);
        when(actionPlanTrackService.getSummaryChartData(actionPlanTrackList, queryDto, null, null)).thenReturn(chartDto);
        when(actionPlanTrackService.getSummaryListData(1, actionPlanTrackList, queryDto, null, null)).thenReturn(listDto);

        // Execute the method
        Map<String, Object> resultMap = actionPlanTrackService.getSummaryData(queryDto);

        // Verify the results
        assertNotNull(resultMap);
        assertEquals(result, resultMap);
    }

    @Test
    public void testGetSummaryData_withEmptyData() {
        // Mock the data
        List<ActionPlanTrack> actionPlanTrackList = new ArrayList<>();

        // Mock the behavior
        when(actionPlanTrackService.list()).thenReturn(actionPlanTrackList);
        when(actionPlanTrackService.processingSummaryQuery(queryDto)).thenReturn(actionPlanTrackList);

        // Execute the method
        Map<String, Object> resultMap = actionPlanTrackService.getSummaryData(queryDto);

        // Verify the results
        assertNull(resultMap);
    }
}
