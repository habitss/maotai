package com.keytech.modules.pi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.mdm3.controller.MdmShiftTypeController;
import com.keytech.modules.mdm3.entity.MdmShiftType;
import com.keytech.modules.mdm3.service.IMdmShiftTypeService;
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
public class MdmShiftTypeTest {

    private MockMvc mockMvc;

    @MockBean
    private IMdmShiftTypeService mdmShiftTypeService;

    @InjectMocks
    private MdmShiftTypeController mdmShiftTypeController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(mdmShiftTypeController).build();
    }

    //---story=1050133 --user=孙金伟 01-11-主数据管理-班次类别维护 https://www.tapd.cn/63554897/s/1336744    单元测试
    @Test
    public void testQueryPageList() throws Exception {
        // Mock the service response
        List<MdmShiftType> shiftTypeList = new ArrayList<>();
        MdmShiftType shiftType = new MdmShiftType();
        shiftType.setShiftTypeName("测试班次");
        shiftTypeList.add(shiftType);

        Page<MdmShiftType> page = new Page<>(1, 10);
        page.setRecords(shiftTypeList);
        page.setTotal(1);

        // Mock the baseService.page method
        when(mdmShiftTypeService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        // Perform the GET request
        mockMvc.perform(get("/mes3_mdm/shiftType/queryPageList")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("shiftTypeName", "testDevice")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000))
                .andExpect(jsonPath("$.data.records[0].shiftTypeName").value("testDevice"));
    }
}