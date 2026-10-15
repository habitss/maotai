package com.keytech.modules.action.meeting;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.meeting.controller.MeetingConfigController;
import com.keytech.modules.meeting.entity.MeetingConfig;
import com.keytech.modules.meeting.service.IMeetingConfigService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.GlobalErrorEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @description: --story=1050920 --user=齐鹏波 05-有效会议-02-会议类型管理 https://www.tapd.cn/63554897/s/1352634 单元测试类
 */
@ExtendWith(MockitoExtension.class)
public class MeetingConfigControllerTest {

    @Mock
    private IMeetingConfigService meetingConfigService;

    @InjectMocks
    private MeetingConfigController meetingConfigController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(meetingConfigController).build();
    }

    @Test
    // 测试分页列表查询的正常情况
    public void testQueryPageList_HappyPath() throws Exception {
        MeetingConfig meetingConfig = new MeetingConfig();
        IPage<MeetingConfig> pageList = new Page<>();
        pageList.setRecords(Collections.singletonList(meetingConfig));


        mockMvc.perform(MockMvcRequestBuilders.get("/meeting/meetingConfig/queryPageList")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.result.total").value(1));
    }

    @Test
    // 测试分页列表查询的边界情况，pageNo和pageSize为最小值
    public void testQueryPageList_EdgeCase_MinPageNoPageSize() throws Exception {
        MeetingConfig meetingConfig = new MeetingConfig();
        IPage<MeetingConfig> pageList = new Page<>();
        pageList.setRecords(Collections.singletonList(meetingConfig));


        mockMvc.perform(MockMvcRequestBuilders.get("/meeting/meetingConfig/queryPageList")
                        .param("pageNo", "1")
                        .param("pageSize", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.result.total").value(1));
    }

    @Test
    // 测试分页列表查询的边界情况，pageNo和pageSize为最大值
    public void testQueryPageList_EdgeCase_MaxPageNoPageSize() throws Exception {
        MeetingConfig meetingConfig = new MeetingConfig();
        IPage<MeetingConfig> pageList = new Page<>();
        pageList.setRecords(Collections.singletonList(meetingConfig));


        mockMvc.perform(MockMvcRequestBuilders.get("/meeting/meetingConfig/queryPageList")
                        .param("pageNo", "10000")
                        .param("pageSize", "10000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.result.total").value(1));
    }

    @Test
    // 测试添加会议配置的正常情况
    public void testAdd_HappyPath() throws Exception {
        MeetingConfig meetingConfig = new MeetingConfig();

        mockMvc.perform(MockMvcRequestBuilders.post("/meeting/meetingConfig/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"1\", \"configName\":\"testConfig\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.SUCCESS.getCode()));

        verify(meetingConfigService, times(1)).save(meetingConfig);
    }

    @Test
    // 测试添加会议配置的边界情况，空配置
    public void testAdd_EdgeCase_EmptyConfig() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/meeting/meetingConfig/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.SUCCESS.getCode()));

        verify(meetingConfigService, times(1)).save(any(MeetingConfig.class));
    }

    @Test
    // 测试编辑会议配置的正常情况
    public void testEdit_HappyPath() throws Exception {
        MeetingConfig meetingConfig = new MeetingConfig();

        mockMvc.perform(MockMvcRequestBuilders.put("/meeting/meetingConfig/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"1\", \"configName\":\"updatedConfig\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.SUCCESS.getCode()));

        verify(meetingConfigService, times(1)).updateById(meetingConfig);
    }

    @Test
    // 测试编辑会议配置的边界情况，空配置
    public void testEdit_EdgeCase_EmptyConfig() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/meeting/meetingConfig/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.SUCCESS.getCode()));

        verify(meetingConfigService, times(1)).updateById(any(MeetingConfig.class));
    }

    @Test
    // 测试通过id删除会议配置的正常情况
    public void testDelete_HappyPath() throws Exception {
        doNothing().when(meetingConfigService).removeById("1");

        mockMvc.perform(MockMvcRequestBuilders.delete("/meeting/meetingConfig/delete")
                        .param("id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.SUCCESS.getCode()));

        verify(meetingConfigService, times(1)).removeById("1");
    }

    @Test
    // 测试通过id删除会议配置的边界情况，空id
    public void testDelete_EdgeCase_EmptyId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/meeting/meetingConfig/delete")
                        .param("id", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(meetingConfigService, times(0)).removeById(anyString());
    }

    @Test
    // 测试批量删除会议配置的正常情况
    public void testDeletionBatch_HappyPath() throws Exception {
        doNothing().when(meetingConfigService).removeByIds(Arrays.asList("1", "2", "3"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/meeting/meetingConfig/deletionBatch")
                        .param("ids", "1,2,3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.SUCCESS.getCode()));

        verify(meetingConfigService, times(1)).removeByIds(Arrays.asList("1", "2", "3"));
    }

    @Test
    // 测试批量删除会议配置的边界情况，空ids
    public void testDeletionBatch_EdgeCase_EmptyIds() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/meeting/meetingConfig/deletionBatch")
                        .param("ids", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(meetingConfigService, times(0)).removeByIds(anyList());
    }

    @Test
    // 测试通过id查询会议配置的正常情况
    public void testQueryById_HappyPath() throws Exception {
        MeetingConfig meetingConfig = new MeetingConfig();
        meetingConfig.setId("1");
        meetingConfig.setMeetingType("testConfig");

        when(meetingConfigService.getById("1")).thenReturn(meetingConfig);

        mockMvc.perform(MockMvcRequestBuilders.get("/meeting/meetingConfig/queryById")
                        .param("id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.result.configName").value("testConfig"));

        verify(meetingConfigService, times(1)).getById("1");
    }

    @Test
    // 测试通过id查询会议配置的边界情况，不存在的id
    public void testQueryById_EdgeCase_NotFound() throws Exception {
        when(meetingConfigService.getById("999")).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/meeting/meetingConfig/queryById")
                        .param("id", "999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(GlobalErrorEnum.NO_CORRESPONDING_DATA_FOUND.getCode()));

        verify(meetingConfigService, times(1)).getById("999");
    }

    @Test
    // 测试通过id查询会议配置的边界情况，空id
    public void testQueryById_EdgeCase_EmptyId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/meeting/meetingConfig/queryById")
                        .param("id", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(meetingConfigService, times(0)).getById(anyString());
    }

    @Test
    // 测试导出excel的正常情况
    public void testExportXls_HappyPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        ModelAndView modelAndView = meetingConfigController.exportXls(request, new MeetingConfig());

        verify(meetingConfigService, times(0)).removeById(anyString());
        verify(meetingConfigService, times(0)).removeByIds(anyList());
        verify(meetingConfigService, times(0)).save(any(MeetingConfig.class));
        verify(meetingConfigService, times(0)).updateById(any(MeetingConfig.class));
        verify(meetingConfigService, times(1)).page(any(Page.class), any(QueryWrapper.class));

        assert modelAndView != null;
    }

    @Test
    // 测试导入excel的正常情况
    public void testImportExcel_HappyPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Result<?> result = meetingConfigController.importExcel(request, response);

        verify(meetingConfigService, times(0)).removeById(anyString());
        verify(meetingConfigService, times(0)).removeByIds(anyList());
        verify(meetingConfigService, times(0)).save(any(MeetingConfig.class));
        verify(meetingConfigService, times(0)).updateById(any(MeetingConfig.class));

        assert result.getCode().equals(GlobalErrorEnum.SUCCESS.getCode());
    }
}

