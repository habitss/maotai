package com.keytech.modules.pi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.pi.controller.PiAbnormalAnalysisEvaluationController;
import com.keytech.modules.pi.entity.PiAbnormalAnalysisEvaluation;
import com.keytech.modules.pi.service.IPiAbnormalAnalysisEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @Description: --story=1051147 --user=孙金伟 02-42-PI日报-异常分析整合到5why https://www.tapd.cn/63554897/s/1349218 单元测试
 * @Author: sjw
 * @Date: 2025/3/3 17:03
 */
public class PiAbnormalAnalysisEvaluationTest {

    @Mock
    private IPiAbnormalAnalysisEvaluationService piAbnormalAnalysisEvaluationService;

    @InjectMocks
    private PiAbnormalAnalysisEvaluationController piAbnormalAnalysisEvaluationController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(piAbnormalAnalysisEvaluationController).build();
    }

    @Test
    public void testQueryPageList() throws Exception {
        PiAbnormalAnalysisEvaluation evaluation = new PiAbnormalAnalysisEvaluation();
        evaluation.setId("1");

        Page<PiAbnormalAnalysisEvaluation> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(evaluation));
        page.setTotal(1);

        when(piAbnormalAnalysisEvaluationService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        mockMvc.perform(get("/pi/piAbnormalAnalysisEvaluation/queryPageList")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("orderField", "id")
                        .param("orderType", "asc")
                        .param("selectFields", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.records[0].id").value("1"))
                .andExpect(jsonPath("$.result.total").value(1));
    }
}
