package com.keytech.modules.pi;


import com.keytech.modules.action.plan.controller.ActionPlanTrackController;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.api.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

//--story=1050397 --user=孙金伟 03-行动计划-01-行动计划管理 https://www.tapd.cn/63554897/s/1339602 单元测试
//--story=1050398 --user=孙金伟 03-行动计划-02-行动计划跟踪 https://www.tapd.cn/63554897/s/1340245 单元测试
@WebMvcTest(ActionPlanTrackController.class)
public class ActionPlanTrackTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IActionPlanTrackService actionPlanTrackService;

    @InjectMocks
    private ActionPlanTrackController actionPlanTrackController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testQueryPageList() throws Exception {
        // Arrange
        ActionPlanTrack actionPlanTrack = new ActionPlanTrack();
        actionPlanTrack.setId(1);
        actionPlanTrack.setActionTitle("Test Action Title");

        Page<ActionPlanTrack> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(actionPlanTrack));

        when(actionPlanTrackService.page(any(Page.class), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/action/plan/queryPageList")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("orderField", "id")
                        .param("orderType", "desc")
                        .param("selectFields", "id,actionTitle")
                )
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].actionTitle").value("Test Action Title"));
    }
}
