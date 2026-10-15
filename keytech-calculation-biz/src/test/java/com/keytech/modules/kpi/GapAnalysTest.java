package com.keytech.modules.kpi;

import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.fivewhy.entity.FiveWhyDetail;
import com.keytech.modules.fivewhy.service.IFiveWhyDetailService;
import com.keytech.modules.kpi.entity.GapAnalysis;
import com.keytech.modules.kpi.service.impl.GapAnalysisServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;
@SpringBootTest
@AutoConfigureMockMvc
public class GapAnalysTest {

    @Mock
    private IActionPlanTrackService actionPlanTrackService;

    @Mock
    private IFiveWhyDetailService fiveWhyDetailService;

    @InjectMocks
    private GapAnalysisServiceImpl gapAnalysisService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //--story=1050411 --user=孙金伟 02-KPI管理-10-差距分析 https://www.tapd.cn/63554897/s/1348450 单元测试
    @Test
    public void testAdd() {
        // Arrange
        GapAnalysis gapAnalysis = new GapAnalysis();
        gapAnalysis.setId(null);

        List<FiveWhyDetail> detailList = new ArrayList<>();
        FiveWhyDetail detail = new FiveWhyDetail();
        detail.setId("temp_123");
        detail.setActionType(true);
        detail.setSolveProblem("Solve Problem");
        detail.setReason("Reason");
        detail.setPlanFinishDate(new Date());
        detailList.add(detail);
        gapAnalysis.setDetailList(detailList);

        // Act
        gapAnalysisService.add(gapAnalysis);

        // Assert
        verify(fiveWhyDetailService, times(1)).saveBatch(anyList());
        verify(actionPlanTrackService, times(1)).saveBatch(anyList());
        verify(gapAnalysisService, times(1)).saveOrUpdate(gapAnalysis);
    }
}