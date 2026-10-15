package com.keytech.modules.pi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.pi.controller.PiAbnormalAnalysisPointRecordController;
import com.keytech.modules.pi.entity.PiAbnormalAnalysisPointRecord;
import com.keytech.modules.pi.service.IPiAbnormalAnalysisPointRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @Description: --story=1051148 --user=孙金伟 02-43-PI日报-异常排查清单管理 https://www.tapd.cn/63554897/s/1349951 单元测试
 * @Author: sjw
 * @Date: 2025/3/7 10:17
 */
@ExtendWith(MockitoExtension.class)
public class PiAbnormalAnalysisPointRecordTest {
    private MockMvc mockMvc;

    @Mock
    private IPiAbnormalAnalysisPointRecordService piAbnormalAnalysisPointRecordService;

    @InjectMocks
    private PiAbnormalAnalysisPointRecordController piAbnormalAnalysisPointRecordController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(piAbnormalAnalysisPointRecordController).build();
    }

    @Test
    public void testQueryPageList() throws Exception {
        PiAbnormalAnalysisPointRecord record = new PiAbnormalAnalysisPointRecord();
        record.setId("1");

        Page<PiAbnormalAnalysisPointRecord> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(record));

        Mockito.when(piAbnormalAnalysisPointRecordService.page(Mockito.any(Page.class), Mockito.any(QueryWrapper.class)))
                .thenReturn(page);

        mockMvc.perform(get("/pi/piAbnormalAnalysisPointRecord/queryPageList")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("orderField", "id")
                        .param("orderType", "asc")
                        .param("selectFields", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.records[0].id").value("1"));

        Mockito.verify(piAbnormalAnalysisPointRecordService, Mockito.times(1))
                .page(Mockito.any(Page.class), Mockito.any(QueryWrapper.class));
    }
}
