package com.keytech.modules.pi;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.mdm3.controller.MdmDeviceTypeController;
import com.keytech.modules.mdm3.entity.MdmDeviceType;
import com.keytech.modules.mdm3.service.IMdmDeviceTypeService;
import com.keytech.modules.mdm3.service.IMdmDictService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MdmDeviceTypeController.class)
public class MdmDeviceTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IMdmDeviceTypeService deviceTypeService;

    @InjectMocks
    private MdmDeviceTypeController mdmDeviceTypeController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(mdmDeviceTypeController).build();
    }

    //--story=1050129 --user=孙金伟 01-07-主数据管理-设备类型 https://www.tapd.cn/63554897/s/1334636 单元测试
    @Test
    public void testQueryPageList() throws Exception {
        // Mock the service response
        List<MdmDeviceType> deviceTypeList = new ArrayList<>();
        MdmDeviceType deviceType = new MdmDeviceType();
        deviceType.setDeviceTypeName("TestDevice");
        deviceTypeList.add(deviceType);

        Page<MdmDeviceType> page = new Page<>(1, 10);
        page.setRecords(deviceTypeList);
        page.setTotal(1);

        when(deviceTypeService.page(any(Page.class), any())).thenReturn(page);

        // Perform the request
        mockMvc.perform(MockMvcRequestBuilders.get("/mes3_mdm/deviceType/queryPageList")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("deviceTypeName", "TestDevice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].deviceTypeName").value("TestDevice"));
    }
}