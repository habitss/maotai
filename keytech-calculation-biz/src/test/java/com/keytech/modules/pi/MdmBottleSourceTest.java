package com.keytech.modules.pi;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.mdm3.controller.MdmBottleSourceController;
import com.keytech.modules.mdm3.entity.MdmBottleSource;
import com.keytech.modules.mdm3.service.IMdmBottleSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//--story=1050128 --user=孙金伟 01-06-主数据管理-瓶源管理 https://www.tapd.cn/63554897/s/1335100 单元测试
@WebMvcTest(MdmBottleSourceController.class)
public class MdmBottleSourceTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IMdmBottleSourceService bottleSourceService;

    @BeforeEach
    void setUp() {
        // 设置mock行为，如果需要的话
        given(bottleSourceService.page(any(Page.class), any())).willReturn(new Page<>());
    }

    @Test
    void shouldReturnDefaultMessage() throws Exception {
        // 给定条件
        given(bottleSourceService.page(any(Page.class), any()))
                .willReturn(new Page<MdmBottleSource>(1, 10));

        // 行为
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/mes3_mdm/bottleSource/queryPageList")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void testExportXls() throws Exception {
        // 测试导出Excel功能，这里可以模拟返回一个文件流等
        mockMvc.perform(MockMvcRequestBuilders.get("/mes3_mdm/bottleSource/exportXls"))
                .andExpect(status().isOk());
    }

    @Test
    void testImportExcel() throws Exception {
        // 测试导入Excel功能，这里可以上传一个文件并检查响应
        mockMvc.perform(MockMvcRequestBuilders.multipart("/mes3_mdm/bottleSource/importExcel")
                        .file("file", "test.xlsx".getBytes()))
                .andExpect(status().isOk());
    }

    @Test
    void testSaveOrUpdate() throws Exception {
        // 测试保存或更新瓶源管理数据
        String jsonContent = "{ \"bottleSourceName\": \"test\" }";
        given(bottleSourceService.saveOrUpdate(any(MdmBottleSource.class))).willReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/mes3_mdm/bottleSource/saveOrUpdate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk());
    }
}