package com.keytech.modules.pi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.post.check.controller.PostCheckRecordConfigMainController;
import com.keytech.modules.post.check.entity.PostCheckRecordConfigMain;
import com.keytech.modules.post.check.service.IPostCheckRecordConfigMainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//--story=1050142 --user=孙金伟 01-01岗位记录-表单管理 https://www.tapd.cn/63554897/s/1338036 单元测试
@WebMvcTest(PostCheckRecordConfigMainController.class)
public class PostCheckRecordPzZbTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IPostCheckRecordConfigMainService postCheckRecordPzZbService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testQueryPageList() throws Exception {
        // Mock the service response
        PostCheckRecordConfigMain postCheckRecordPzZb = new PostCheckRecordConfigMain();
        IPage<PostCheckRecordConfigMain> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(postCheckRecordPzZb));
        page.setTotal(1L);
        when(postCheckRecordPzZbService.page(any(Page.class), any(QueryWrapper.class))).thenReturn((Page) page);

        // Perform the GET request and verify the response
        mockMvc.perform(get("/post_check/postCheckRecordPzZb/queryPageList")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0]").exists());
    }
}