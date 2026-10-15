package com.keytech.modules.pi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.mdm3.controller.MdmDeviceController;
import com.keytech.modules.mdm3.entity.MdmDevice;
import com.keytech.modules.mdm3.entity.MdmDevice;
import com.keytech.modules.mdm3.service.IMdmDeviceService;
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
public class MdmDeviceControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private IMdmDeviceService mdmDeviceService;

    @InjectMocks
    private MdmDeviceController mdmDeviceController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(mdmDeviceController).build();
    }

    //--story=1050090 --user=孙金伟 01-03-主数据管理-设备管理 https://www.tapd.cn/63554897/s/1334820    单元测试
    @Test
    public void testQueryPageList() throws Exception {
        // Mock the service response
        List<MdmDevice> deviceList = new ArrayList<>();
        MdmDevice device = new MdmDevice();
        device.setDeviceName("TestDevice");
        deviceList.add(device);

        Page<MdmDevice> page = new Page<>(1, 10);
        page.setRecords(deviceList);
        page.setTotal(1);

        // Mock the baseService.page method
        when(mdmDeviceService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        // Perform the GET request
        mockMvc.perform(get("/mes3_mdm/device/queryPageList")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("deviceName", "testDevice")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000))
                .andExpect(jsonPath("$.data.records[0].deviceName").value("testDevice"));
    }
}