package com.keytech.modules.pi;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.base.qo.PageQO;
import com.keytech.modules.post.check.controller.PostCheckRecordConfigProjectController;
import com.keytech.modules.post.check.dto.PostCheckRecordConfigQueryDto;
import com.keytech.modules.post.check.entity.PostCheckRecordConfigProject;
import com.keytech.modules.post.check.entity.PostCheckRecordConfigStandard;
import com.keytech.modules.post.check.service.IPostCheckRecordConfigProjectService;
import com.keytech.modules.post.check.service.IPostCheckRecordConfigStandardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @Description: TODO
 * @Author: sjw
 * @Date: 2025/1/6 17:10
 */
//--story=1050143 --user=孙金伟 01-02岗位记录-填报条目配置 https://www.tapd.cn/63554897/s/1338990 单元测试
@WebMvcTest(PostCheckRecordConfigProjectController.class)
public class PostCheckRecordConfigProjectTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IPostCheckRecordConfigStandardService postCheckRecordPzJcbzService;

    @MockBean
    private IPostCheckRecordConfigProjectService postCheckRecordPzJcxmService;

    @InjectMocks
    private PostCheckRecordConfigProjectController postCheckRecordConfigProjectController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(postCheckRecordConfigProjectController).build();
    }

    @Test
    public void testQueryPageList() throws Exception {
        // Mock the services
        List<PostCheckRecordConfigProject> projectList = new ArrayList<>();
        PostCheckRecordConfigProject project = new PostCheckRecordConfigProject();
        project.setId("1");
        projectList.add(project);

        List<PostCheckRecordConfigStandard> standardList = new ArrayList<>();
        PostCheckRecordConfigStandard standard = new PostCheckRecordConfigStandard();
        standard.setId("1");
        standard.setJcxmId("1");
        standard.setSort(1.0);
        standardList.add(standard);

        when(postCheckRecordPzJcxmService.list((PageQO) null)).thenReturn((Page<PostCheckRecordConfigProject>) projectList);
        when(postCheckRecordPzJcbzService.list((PageQO) null)).thenReturn((Page<PostCheckRecordConfigStandard>) standardList);

        // Perform the request and verify the response
        mockMvc.perform(get("/post_check/postCheckRecordConfigProject/queryList")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<PostCheckRecordConfigQueryDto> responseContent = objectMapper.readValue(result.getResponse().getContentAsString(), List.class);
                    assert responseContent.size() == 1;
                    assert responseContent.get(0).getBzid().equals("1");
                });
    }
}
