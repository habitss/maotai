package com.keytech.modules.pi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.pi.controller.PiAbnormalAnalysisPointController;
import com.keytech.modules.pi.entity.PiAbnormalAnalysisPoint;
import com.keytech.modules.pi.service.IPiAbnormalAnalysisPointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @Description: --story=1050419 --user=孙金伟 02-KPI管理-18-异常清单 https://www.tapd.cn/63554897/s/1349950 单元测试
 * @Author: sjw
 * @Date: 2025/3/7 10:17
 */
@ExtendWith(MockitoExtension.class)
public class PiAbnormalAnalysisPointTest {
    @Mock
    private IPiAbnormalAnalysisPointService piAbnormalAnalysisPointService;

    @InjectMocks
    private PiAbnormalAnalysisPointController piAbnormalAnalysisPointController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(piAbnormalAnalysisPointController).build();
    }

    @Test
    public void testQueryPageListWithHasQuery() throws Exception {
        PiAbnormalAnalysisPoint piAbnormalAnalysisPoint = new PiAbnormalAnalysisPoint();
        piAbnormalAnalysisPoint.setParentId("0");

        QueryWrapper<PiAbnormalAnalysisPoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", "0");

        when(piAbnormalAnalysisPointService.queryTreeListNoPage(any(QueryWrapper.class)))
                .thenReturn(Collections.singletonList(piAbnormalAnalysisPoint));

        mockMvc.perform(get("/pi/piAbnormalAnalysisPoint/rootList")
                        .param("hasQuery", "true")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk());
    }

    @Test
    public void testQueryPageListWithoutHasQuery() throws Exception {
        PiAbnormalAnalysisPoint piAbnormalAnalysisPoint = new PiAbnormalAnalysisPoint();
        piAbnormalAnalysisPoint.setParentId("0");

        QueryWrapper<PiAbnormalAnalysisPoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", "0");

        IPage<PiAbnormalAnalysisPoint> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(piAbnormalAnalysisPoint));

        when(piAbnormalAnalysisPointService.page(any(Page.class), any(QueryWrapper.class)))
                .thenReturn((Page) page);

        mockMvc.perform(get("/pi/piAbnormalAnalysisPoint/rootList")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk());
    }

    @Test
    public void testQueryPageListWithEmptyParentId() throws Exception {
        PiAbnormalAnalysisPoint piAbnormalAnalysisPoint = new PiAbnormalAnalysisPoint();
        piAbnormalAnalysisPoint.setParentId(null);

        QueryWrapper<PiAbnormalAnalysisPoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", "0");

        IPage<PiAbnormalAnalysisPoint> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(piAbnormalAnalysisPoint));

        when(piAbnormalAnalysisPointService.page(any(Page.class), any(QueryWrapper.class)))
                .thenReturn((Page) page);

        mockMvc.perform(get("/pi/piAbnormalAnalysisPoint/rootList")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk());
    }
}
