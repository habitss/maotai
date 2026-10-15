package com.keytech.modules.pi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.mdm3.controller.MdmShiftGroupController;
import com.keytech.modules.mdm3.entity.MdmShiftGroup;
import com.keytech.modules.mdm3.service.IMdmShiftGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MdmShiftGroupController.class)
public class MdmShiftGroupTest {
    //--story=1050132 --user=孙金伟 01-10-主数据管理-班组管理 https://www.tapd.cn/63554897/s/1337506 单元测试

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IMdmShiftGroupService mdmShiftGroupService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testQueryPageList() throws Exception {
        // Mock the service response
        MdmShiftGroup shiftGroup = new MdmShiftGroup();
        shiftGroup.setId("1");
        shiftGroup.setGroupName("Group1");

        List<MdmShiftGroup> shiftGroupList = new ArrayList<>();
        shiftGroupList.add(shiftGroup);

        Page<MdmShiftGroup> page = new Page<>(1, 10);
        page.setRecords(shiftGroupList);

        when(mdmShiftGroupService.page(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(page);

        // Perform the GET request
        mockMvc.perform(MockMvcRequestBuilders.get("/mdm/mdmShiftGroup/queryPageList")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].id").value("1"))
                .andExpect(jsonPath("$.data.records[0].groupName").value("Group1"));
    }
}