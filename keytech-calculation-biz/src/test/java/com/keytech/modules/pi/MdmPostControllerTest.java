package com.keytech.modules.pi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.mdm3.controller.MdmPostController;
import com.keytech.modules.mdm3.entity.MdmPost;
import com.keytech.modules.mdm3.service.IMdmPostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class MdmPostControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private IMdmPostService mdmPostService;

    @InjectMocks
    private MdmPostController mdmPostController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(mdmPostController).build();
    }

    //--story=1050130 --user=孙金伟 01-08-主数据管理-岗位管理 https://www.tapd.cn/63554897/s/1334981    单元测试
    @Test
    public void testQueryPageList() throws Exception {
        // Mock the service response
        List<MdmPost> postList = new ArrayList<>();
        MdmPost post = new MdmPost();
        post.setPostName("TestPost");
        postList.add(post);

        Page<MdmPost> page = new Page<>(1, 10);
        page.setRecords(postList);
        page.setTotal(1);

        // Mock the baseService.page method
        when(mdmPostService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        // Perform the GET request
        mockMvc.perform(get("/mes3_mdm/post/queryPageList")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("postName", "testPost")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000))
                .andExpect(jsonPath("$.data.records[0].postName").value("testPost"));
    }
}